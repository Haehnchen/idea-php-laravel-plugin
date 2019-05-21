package de.espend.idea.laravel.routing.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.*;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.laravel.stub.RouteIndexExtension;
import de.espend.idea.laravel.stub.processor.CollectProjectUniqueKeys;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class RoutingUtil {

    static final String[] HTTP_METHODS = new String[]{"get", "post", "put", "delete", "patch", "delete", "options", "any", "match"};

    private static final Key<CachedValue<Collection<String>>> ROUTE_NAMES = new Key<>("LaravelRoutingUtilNames");

    static final String[] REST_METHODS = new String[]{"index", "create", "store", "show", "edit", "update", "destroy"};

    public static Collection<PsiElement> getRoutesAsTargets(@NotNull PsiFile psiFile, final @NotNull String routeName) {
        final Set<PsiElement> names = new HashSet<>();

        visitRoutesForAs(psiFile, (psiElement, name) -> {
            if(name.equals(routeName)) {
                names.add(psiElement);
            }
        });

        return names;
    }

    public static Collection<PsiElement> getRoutesAsTargets(@NotNull Project project, @NotNull String routeName) {
        Set<PsiElement> targets = new HashSet<>();

        Set<VirtualFile> virtualFiles = new HashSet<>();

        // find files with route name
        FileBasedIndex.getInstance().getFilesWithKey(RouteIndexExtension.KEY, Collections.singleton(routeName), virtualFile -> {
            virtualFiles.add(virtualFile);
            return true;
        }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(project), PhpFileType.INSTANCE));

        // resolve virtual files and collect
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
            if(file != null) {
                targets.addAll(getRoutesAsTargets(file, routeName));
            }
        }

        return targets;
    }

    @NotNull
    public static Collection<String> getRoutesAsNames(final @NotNull Project project) {
        CachedValue<Collection<String>> cache = project.getUserData(ROUTE_NAMES);

        if(cache == null) {
            cache = CachedValuesManager.getManager(project).createCachedValue(() -> {
                Collection<String> names = new HashSet<>(
                    CollectProjectUniqueKeys.collect(project, RouteIndexExtension.KEY)
                );

                return CachedValueProvider.Result.create(names, PsiModificationTracker.MODIFICATION_COUNT);
            }, false);

            project.putUserData(ROUTE_NAMES, cache);
        }

        return cache.getValue();
    }

    public static Collection<String> getRoutesAsNames(@NotNull PsiFile psiFile) {
        final Set<String> names = new HashSet<>();

        visitRoutesForAs(psiFile, (psiElement, name) ->
            names.add(name)
        );

        return names;
    }

    public static void visitRoutesForAs(@NotNull PsiFile psiFile, @NotNull RouteAsNameVisitor visitor) {
        psiFile.acceptChildren(new RouteNamePsiRecursiveElementVisitor(visitor));
    }

    public interface RouteAsNameVisitor {
        void visit(@NotNull PsiElement psiElement, @NotNull String name);
    }

    private static class RouteNamePsiRecursiveElementVisitor extends PsiRecursiveElementVisitor {

        @NotNull
        private final RouteAsNameVisitor visitor;

        public RouteNamePsiRecursiveElementVisitor(@NotNull RouteAsNameVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void visitElement(PsiElement element) {
            if(element instanceof MethodReference) {
                if("name".equals(((MethodReference) element).getName())) {
                    // Route::get('foo')->name('foo')
                    for (MethodReference methodReference : PsiTreeUtil.getChildrenOfTypeAsList(element, MethodReference.class)) {
                        PhpPsiElement classReference = methodReference.getFirstPsiChild();
                        if(classReference instanceof ClassReference) {
                            if("Route".equalsIgnoreCase(classReference.getName())) {
                                visitName((MethodReference) element, this.getRouteNamePrefix(element));
                                return;
                            }
                        }
                    }

                } else if(ArrayUtils.contains(HTTP_METHODS, ((MethodReference) element).getName())) {

                    // Route::get('foo', ['as' => ...])
                    PhpPsiElement classReference = ((MethodReference) element).getFirstPsiChild();
                    if(classReference instanceof ClassReference) {
                        if("Route".equalsIgnoreCase(classReference.getName())) {
                            visitAs((MethodReference) element, this.getRouteNamePrefix(element));
                        }
                    }
                } else if("resource".equals(((MethodReference) element).getName())) {

                    // Route::resource('foo', 'FooController', [...])
                    PhpPsiElement classReference = ((MethodReference) element).getFirstPsiChild();
                    if(classReference instanceof ClassReference) {
                        if("Route".equalsIgnoreCase(classReference.getName())) {
                            visitResource((MethodReference) element, this.getRouteNamePrefix(element));
                        }
                    }
                }
            }

            super.visitElement(element);
        }

        /**
         * Returns route name prefix, based on Route::group(['as' => values
         */
        @NotNull
        private String getRouteNamePrefix(PsiElement element) {
            return StringUtils.join(RouteGroupUtil.getRouteGroupPropertiesCollection(element, "as"), "");
        }

        private void visitAs(@NotNull MethodReference methodReference, @NotNull String prefix) {

            PsiElement[] parameters = methodReference.getParameters();
            int indexParameter = 1;

            if("match".equals(methodReference.getName())){
                indexParameter = 2;
            }

            if(parameters.length < (1+indexParameter) || !(parameters[indexParameter] instanceof ArrayCreationExpression)) {
                return;
            }

            PhpPsiElement arrayValue = PhpElementsUtil.getArrayValue((ArrayCreationExpression) parameters[indexParameter], "as");
            if(!(arrayValue instanceof StringLiteralExpression)) {
                return;
            }

            String contents = ((StringLiteralExpression) arrayValue).getContents();
            if(StringUtils.isBlank(contents)) {
                return;
            }

            this.visitor.visit(arrayValue, prefix + contents);
        }

        private void visitName(@NotNull MethodReference methodReference, @NotNull String prefix) {
            PsiElement[] parameters = methodReference.getParameters();
            if(parameters.length < 1 || !(parameters[0] instanceof StringLiteralExpression)) {
                return;
            }

            String contents = ((StringLiteralExpression) parameters[0]).getContents();
            if(StringUtils.isBlank(contents)) {
                return;
            }

            this.visitor.visit(parameters[0], prefix + contents);
        }

        /**
         * Visiting Route::resource('foo', 'FooController', [...])
         *
         * @param methodReference Route::resource element
         * @param prefix          Prefix got from ['as' => ...] values from parent Route::group elements
         */
        private void visitResource(@NotNull MethodReference methodReference, @NotNull String prefix) {
            PsiElement[] parameters = methodReference.getParameters();
            if(parameters.length < 2 || !(parameters[0] instanceof StringLiteralExpression)) {
                return;
            }

            String routeUrl = ((StringLiteralExpression) parameters[0]).getContents();
            if(StringUtils.isBlank(routeUrl)) {
                return;
            }

            String[] routeUrlParts = routeUrl.replace('/', '.').split("\\.");

            if(routeUrlParts.length == 0) {
                return;
            }

            String baseNamesPrefix = routeUrlParts[routeUrlParts.length - 1]; // Only last part of the url goes to route name
            for(String routeName : getResourceRouteNames(parameters, prefix + baseNamesPrefix + ".")) {
                this.visitor.visit(parameters[0], routeName);
            }
        }

        /**
         * @param parameters Route::resource method reference parameters
         * @return Collection of full Route::resource names, like ["users.index", "users.show"]
         */
        private Collection<String> getResourceRouteNames(@NotNull PsiElement[] parameters, @NotNull String prefix) {
            Map<String, String> restMethods = new HashMap<>();
            for(String method : REST_METHODS) {
                restMethods.put(method, prefix + method);
            }

            if(parameters.length < 3 || !(parameters[2] instanceof ArrayCreationExpression)) {
                return restMethods.values();
            }

            // Route::resource(..., ['only' => []])
            PhpPsiElement onlyValue = PhpElementsUtil.getArrayValue((ArrayCreationExpression) parameters[2], "only");

            Map<String, String> resultMethods;
            if(onlyValue instanceof ArrayCreationExpression) {
                resultMethods = new HashMap<>();
                for(String method : PhpElementsUtil.getArrayValuesAsString(((ArrayCreationExpression) onlyValue))) {
                    if(restMethods.containsKey(method)) {
                        resultMethods.put(method, prefix + method);
                    }
                }
            } else {
                // Route::resource(..., ['except' => []])
                PhpPsiElement exceptValue = PhpElementsUtil.getArrayValue((ArrayCreationExpression) parameters[2], "except");

                if(exceptValue instanceof ArrayCreationExpression) {
                    resultMethods = restMethods;
                    for(String method : PhpElementsUtil.getArrayValuesAsString(((ArrayCreationExpression) exceptValue))) {
                        resultMethods.remove(method);
                    }
                } else {
                    resultMethods = restMethods;
                }
            }

            // Route::resource(..., ['values' => []]) it overrides standard route names
            PhpPsiElement namesValue = PhpElementsUtil.getArrayValue((ArrayCreationExpression) parameters[2], "names");

            if(namesValue instanceof ArrayCreationExpression) {
                resultMethods = restMethods;
                for(Map.Entry<String, PsiElement> entry : PhpElementsUtil.getArrayValueMap(((ArrayCreationExpression) namesValue)).entrySet()) {

                    if(entry.getValue() instanceof StringLiteralExpression && resultMethods.containsKey(entry.getKey())) {
                        resultMethods.replace(entry.getKey(), ((StringLiteralExpression) entry.getValue()).getContents());
                    }
                }
            }

            return resultMethods.values();
        }
    }
}

