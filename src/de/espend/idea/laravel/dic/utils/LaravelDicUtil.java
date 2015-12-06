package de.espend.idea.laravel.dic.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.util.*;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class LaravelDicUtil {

    private static final Key<CachedValue<Map<String, Collection<String>>>> DIC_CACHE = new Key<CachedValue<Map<String, Collection<String>>>>("LaravelDicUtilMap");

    synchronized public static Map<String, Collection<String>> getDicMap(@NotNull final Project project) {
        CachedValue<Map<String, Collection<String>>> cache = project.getUserData(DIC_CACHE);

        if(cache == null) {
            cache = CachedValuesManager.getManager(project).createCachedValue(new CachedValueProvider<Map<String, Collection<String>>>() {
                @Nullable
                @Override
                public Result<Map<String, Collection<String>>> compute() {
                    Map<String, Collection<String>> coreAliasMap = getCoreAliasMap(project);

                    for (Map.Entry<String, Collection<String>> entry : getServiceProviderMap(project).entrySet()) {
                        if(coreAliasMap.containsKey(entry.getKey())) {
                            coreAliasMap.get(entry.getKey()).addAll(entry.getValue());
                            continue;
                        }

                        coreAliasMap.put(entry.getKey(), entry.getValue());
                    }


                    return Result.create(coreAliasMap, PsiModificationTracker.MODIFICATION_COUNT);
                }
            }, false);

            project.putUserData(DIC_CACHE, cache);
        }

        return cache.getValue();
    }

    public static Collection<PsiElement> getDicTargets(@NotNull final Project project, @NotNull String dicName) {
        Map<String, Collection<String>> dicMap = getDicMap(project);
        if(!dicMap.containsKey(dicName)) {
            return Collections.emptyList();
        }

        Collection<PsiElement> targets = new ArrayList<PsiElement>();
        for (String s : dicMap.get(dicName)) {
            targets.addAll(PhpElementsUtil.getClassesOrInterfaces(project, s));
        }

        return targets;
    }

    public static Map<String, Collection<String>> getCoreAliasMap(@NotNull Project project) {
        final Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();

        visitRegisterCoreContainerAliases(project, new DicAliasVisitor() {
            @Override
            public void visit(@NotNull PsiElement value, @NotNull String keyName) {

                Collection<String> values = new ArrayList<String>();

                if(value instanceof StringLiteralExpression) {
                    String contents = ((StringLiteralExpression) value).getContents();
                    if(StringUtils.isNotBlank(contents)) {
                        values.add(StringUtils.stripStart(contents, "\\"));
                    }
                } else if(value instanceof ArrayCreationExpression) {
                    values.addAll(ContainerUtil.map(PhpElementsUtil.getArrayValuesAsString((ArrayCreationExpression) value), new Function<String, String>() {
                        @Override
                        public String fun(String s) {
                            return StringUtils.stripStart(s, "\\");
                        }
                    }));
                }

                map.put(keyName, values);
            }
        });

        return map;
    }

    public static Collection<String> getCoreAliases(@NotNull Project project) {
        final Collection<String> aliases = new HashSet<String>();

        visitRegisterCoreContainerAliases(project, new DicAliasVisitor() {
            @Override
            public void visit(@NotNull PsiElement value, @NotNull String keyName) {
                aliases.add(keyName);
            }
        });

        return aliases;
    }

    public static void visitRegisterCoreContainerAliases(@NotNull Project project, @NotNull DicAliasVisitor visitor) {
        for (PhpClass phpClass : PhpElementsUtil.getClassesOrInterfaces(project, "Illuminate\\Foundation\\Application")) {
            Method registerMethod = phpClass.findMethodByName("registerCoreContainerAliases");
            if(registerMethod == null) {
                continue;
            }

            final Collection<Variable> aliases = new HashSet<Variable>();
            registerMethod.acceptChildren(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    if(element instanceof Variable && ((Variable) element).isDeclaration() && "aliases".equals(((Variable) element).getName())) {
                        aliases.add((Variable) element);
                    }
                    super.visitElement(element);
                }
            });

            if(aliases.size() == 0) {
                continue;
            }

            for (Variable alias : aliases) {
                ArrayCreationExpression arrayCreation = PsiTreeUtil.getNextSiblingOfType(alias, ArrayCreationExpression.class);
                if(arrayCreation == null) {
                    continue;
                }

                Map<String, PsiElement> arrayCreationKeyMap = PhpElementsUtil.getArrayValueMap(arrayCreation);
                for (Map.Entry<String, PsiElement> entry : arrayCreationKeyMap.entrySet()) {
                    PsiElement value = entry.getValue();
                    visitor.visit(value, entry.getKey());
                }

            }

        }
    }

    public static Map<String, Collection<String>> getServiceProviderMap(@NotNull Project project) {

        Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();

        for (PhpClass phpClass : PhpIndex.getInstance(project).getAllSubclasses("\\Illuminate\\Support\\ServiceProvider")) {

            Collection<MethodReference> methodReferences = new ArrayList<MethodReference>();

            for (Method method : phpClass.getMethods()) {
                method.acceptChildren(new AppDicRecursiveElementVisitor(methodReferences));
            }

            if(methodReferences.size() == 0) {
                continue;
            }

            for (MethodReference methodReference : methodReferences) {

                PsiElement[] parameters = methodReference.getParameters();
                if(parameters.length < 2 || !(parameters[0] instanceof StringLiteralExpression) || parameters[1].getNode().getElementType() != PhpElementTypes.CLOSURE) {
                    continue;
                }

                String dicName = ((StringLiteralExpression) parameters[0]).getContents();
                if(StringUtils.isBlank(dicName)) {
                    continue;
                }

                final Set<String> types = new HashSet<String>();

                parameters[1].acceptChildren(new PsiRecursiveElementVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
                        if(element instanceof PhpReturn) {
                            PhpPsiElement firstPsiChild = ((PhpReturn) element).getFirstPsiChild();
                            if(firstPsiChild instanceof PhpTypedElement) {
                                for (String s : ((PhpTypedElement) firstPsiChild).getType().getTypes()) {
                                    if(s.startsWith("#")) {
                                        continue;
                                    }
                                    types.add(StringUtils.stripStart(s, "\\"));
                                }
                            }
                        }
                        super.visitElement(element);
                    }
                });

                map.put(dicName, types);
            }

        }

        return map;
    }

    interface DicAliasVisitor {
        void visit(@NotNull PsiElement value, @NotNull String keyName);
    }

    private static class AppDicRecursiveElementVisitor extends PsiRecursiveElementVisitor {

        private final Collection<MethodReference> methodReferences;

        public AppDicRecursiveElementVisitor(Collection<MethodReference> methodReferences) {
            this.methodReferences = methodReferences;
        }

        @Override
        public void visitElement(PsiElement element) {
            if(element instanceof MethodReference && "singleton".equals(((MethodReference) element).getName())) {
                this.methodReferences.add((MethodReference) element);
            }

            super.visitElement(element);
        }
    }
}
