package de.espend.idea.laravel.config;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.LaravelIcons;
import de.espend.idea.laravel.LaravelProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionContributor;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AppConfigReferences implements GotoCompletionRegistrar {

    private static MethodMatcher.CallToSignature[] CONFIG = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Illuminate\\Config\\Repository", "get"),
    };

    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {

        registrar.register(PlatformPatterns.psiElement(), new GotoCompletionContributor() {

            @Nullable
            @Override
            public GotoCompletionProvider getProvider(@Nullable PsiElement psiElement) {

                if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                    return null;
                }

                PsiElement parent = psiElement.getParent();
                if(parent != null && MethodMatcher.getMatchedSignatureWithDepth(parent, CONFIG) != null) {
                    return new ControllerRoute(parent);
                }

                return null;

            }

        });

    }

    private static class ControllerRoute extends GotoCompletionProvider {

        public ControllerRoute(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {

            final Collection<LookupElement> lookupElements = new ArrayList<LookupElement>();

            visitConfigs(new ConfigVisitor() {
                @Override
                public void visitConfig(String key, PsiElement psiKey, boolean isRootElement) {

                    LookupElementBuilder lookup = LookupElementBuilder.create(key)
                        .withTypeText(psiKey.getContainingFile().getName(), true)
                        .withIcon(LaravelIcons.LARAVEL);

                    if (isRootElement) {
                        lookup.withTypeText("(root)", true);
                    }

                    lookupElements.add(lookup);
                }
            });

            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {

            final String text = element.getContents();
            if(StringUtils.isBlank(text)) {
                return Collections.emptyList();
            }

            final Set<PsiElement> targets = new HashSet<PsiElement>();

            visitConfigs(new ConfigVisitor() {
                @Override
                public void visitConfig(String key, PsiElement psiKey, boolean isRootElement) {
                    if(text.equalsIgnoreCase(key)) {
                        targets.add(psiKey);
                    }
                }
            });

            return targets;
        }

        private void visitConfigs(ConfigVisitor configVisitor) {

            VirtualFile appConfig = VfsUtil.findRelativeFile(getProject().getBaseDir(), "app", "config");
            if(appConfig == null) {
                return;
            }

            VfsUtil.visitChildrenRecursively(appConfig, new MyVirtualFileVisitor(PsiManager.getInstance(getProject()), configVisitor));
        }
    }

    public static void collectConfigKeys(ArrayCreationExpression creationExpression, ConfigVisitor configVisitor) {
        collectConfigKeys(creationExpression, configVisitor, new ArrayList<String>());
    }

    public static void collectConfigKeys(ArrayCreationExpression creationExpression, ConfigVisitor configVisitor, List<String> context) {

        for(ArrayHashElement hashElement: PsiTreeUtil.getChildrenOfTypeAsList(creationExpression, ArrayHashElement.class)) {

            PsiElement arrayKey = hashElement.getKey();
            PsiElement arrayValue = hashElement.getValue();

            if(arrayKey instanceof StringLiteralExpression) {

                List<String> myContext = new ArrayList<String>(context);
                myContext.add(((StringLiteralExpression) arrayKey).getContents());
                String keyName = StringUtils.join(myContext, ".");

                if(arrayValue instanceof ArrayCreationExpression) {
                    configVisitor.visitConfig(keyName, arrayKey, true);
                    collectConfigKeys((ArrayCreationExpression) arrayValue, configVisitor, myContext);
                } else {
                    configVisitor.visitConfig(keyName, arrayKey, false);
                }

            }
        }

    }

    public interface ConfigVisitor {
        public void visitConfig(String key, PsiElement psiKey, boolean isRootElement);
    }

    private static class MyVirtualFileVisitor extends VirtualFileVisitor {

        private final PsiManager psiManager;
        private final ConfigVisitor configVisitor;

        public MyVirtualFileVisitor(PsiManager psiManager, ConfigVisitor configVisitor) {
            this.psiManager = psiManager;
            this.configVisitor = configVisitor;
        }

        @Override
        public boolean visitFile(@NotNull VirtualFile virtualFile) {

            if(virtualFile.getFileType() == PhpFileType.INSTANCE) {
                PsiFile psiFile = psiManager.findFile(virtualFile);
                if(psiFile != null) {
                    psiFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                        @Override
                        public void visitElement(PsiElement element) {

                            if(element instanceof PhpReturn) {
                                visitPhpReturn((PhpReturn) element);
                            }

                            super.visitElement(element);
                        }

                        public void visitPhpReturn(PhpReturn phpReturn) {
                            PsiElement arrayCreation = phpReturn.getFirstPsiChild();
                            if(arrayCreation instanceof ArrayCreationExpression) {
                                collectConfigKeys((ArrayCreationExpression) arrayCreation, configVisitor);
                            }
                        }

                    });

                }
            }

            return super.visitFile(virtualFile);
        }
    }
}
