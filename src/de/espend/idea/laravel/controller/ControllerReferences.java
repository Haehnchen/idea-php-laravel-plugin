package de.espend.idea.laravel.controller;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Condition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpPresentationUtil;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.laravel.LaravelIcons;
import de.espend.idea.laravel.LaravelProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionContributor;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ControllerReferences implements GotoCompletionRegistrar {

    private static MethodMatcher.CallToSignature[] ROUTE = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\Router", "get"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\Router", "post"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\Router", "put"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\Router", "patch"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\Router", "delete"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\Router", "options"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\Router", "any"),
    };

    private static MethodMatcher.CallToSignature[] CONTROLLERS = new MethodMatcher.CallToSignature[] {
            new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\Router", "controllers"),
    };

    private static MethodMatcher.CallToSignature[] CONTROLLER = new MethodMatcher.CallToSignature[] {
            new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\Router", "controller"),
    };

    private static MethodMatcher.CallToSignature[] ACTIONS = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\Redirector", "action"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Html\\HtmlBuilder", "linkAction"),
    };

    private static MethodMatcher.CallToSignature[] ROUTE_RESOURCE = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\Router", "resource"),
    };

    private static MethodMatcher.CallToSignature[] ROUTE_GROUP = new MethodMatcher.CallToSignature[] {
            new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\Router", "group"),
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
                if(parent != null && MethodMatcher.getMatchedSignatureWithDepth(parent, ROUTE_RESOURCE, 1) != null) {
                    return createResourceCompletion(parent);
                }

                return null;

            }
        });

        registrar.register(PlatformPatterns.psiElement(), new GotoCompletionContributor() {
            @Nullable
            @Override
            public GotoCompletionProvider getProvider(@Nullable PsiElement psiElement) {

                if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                    return null;
                }

                PsiElement parent = psiElement.getParent();
                if(parent == null) {
                    return null;
                }

                /*
                @TODO: Use "dev/storage/framework/routes.php"
                if(PsiElementUtils.isFunctionReference(parent, "route", 0)) {
                    return new ControllerRoute(parent);
                }*/

                if (MethodMatcher.getMatchedSignatureWithDepth(parent, ROUTE, 1) != null) {
                    return createRouteCompletion(parent);
                }

                if (MethodMatcher.getMatchedSignatureWithDepth(parent, ACTIONS) != null ||
                    PhpElementsUtil.isFunctionReference(psiElement.getParent(), 0, "link_to_action", "action")
                    ) {

                    // Simple completion. Without searching parent Route::group's
                    return new ControllerRoute(parent);
                }

                /*
                Route::get('user/profile', ['uses' => 'UserController@showProfile']);
                */
                PsiElement uses = getUsesArrayMethodParameter(parent);
                if (uses != null && MethodMatcher.getMatchedSignatureWithDepth(uses, ROUTE, 1) != null) {
                    return createRouteCompletion(parent);
                }

                return null;

            }

            @Nullable
            private PsiElement getUsesArrayMethodParameter(@NotNull PsiElement psiElement) {

                PsiElement arrayValue = psiElement.getParent();
                if(arrayValue != null && arrayValue.getNode().getElementType() == PhpElementTypes.ARRAY_VALUE) {
                    PsiElement arrayHashElement = arrayValue.getParent();
                    if(arrayHashElement instanceof ArrayHashElement) {
                        PhpPsiElement key = ((ArrayHashElement) arrayHashElement).getKey();
                        if(key instanceof StringLiteralExpression) {
                            String contents = ((StringLiteralExpression) key).getContents();
                            if(contents.equals("uses")) {
                                PsiElement arrayCreation = arrayHashElement.getParent();
                                if(arrayCreation instanceof ArrayCreationExpression) {
                                    return arrayCreation;
                                }
                            }
                        }
                    }
                }

                return null;
            }

        });


        /*
        Route::controllers([
	        'auth' => 'Auth\AuthController',
	        'password' => 'Auth\PasswordController',
        ]);
        */
        registrar.register(PlatformPatterns.psiElement(), new GotoCompletionContributor() {
            @Nullable
            @Override
            public GotoCompletionProvider getProvider(@Nullable PsiElement psiElement) {

                if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                    return null;
                }

                PsiElement parent = psiElement.getParent();
                if(parent == null) {
                    return null;
                }

                PsiElement controllerParameter = getArrayMethodParameter(parent);
                if(controllerParameter == null) {
                    return null;
                }

                MethodMatcher.MethodMatchParameter matchedSignatureWithDepth = MethodMatcher.getMatchedSignatureWithDepth(controllerParameter, CONTROLLERS);
                if(matchedSignatureWithDepth == null) {
                    return null;
                }

                return createResourceCompletion(parent);
            }

            @Nullable
            private PsiElement getArrayMethodParameter(@NotNull PsiElement psiElement) {

                PsiElement arrayValue = psiElement.getParent();
                if (arrayValue.getNode().getElementType() == PhpElementTypes.ARRAY_VALUE) {
                    PsiElement arrayHashElement = arrayValue.getParent();
                    if (arrayHashElement instanceof ArrayHashElement) {

                        PsiElement arrayCreation = arrayHashElement.getParent();
                        if (arrayCreation instanceof ArrayCreationExpression) {
                            return arrayCreation;
                        }
                    }
                }

                return null;
            }

        });

        /**
         * Route::controller('users', 'UserController');
         */
        registrar.register(PlatformPatterns.psiElement(), new GotoCompletionContributor() {
            @Nullable
            @Override
            public GotoCompletionProvider getProvider(@Nullable PsiElement psiElement) {

                if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                    return null;
                }

                PsiElement parent = psiElement.getParent();
                if(parent == null) {
                    return null;
                }
                MethodMatcher.MethodMatchParameter matchedSignatureWithDepth = MethodMatcher.getMatchedSignatureWithDepth(parent, CONTROLLER, 1);
                if(matchedSignatureWithDepth == null) {
                    return null;
                }

                return createResourceCompletion(parent);
            }

        });

    }

    private ControllerResource createResourceCompletion(@NotNull PsiElement element) {
        return new ControllerResource(element, getControllerGroupPrefix(element));
    }

    private ControllerRoute createRouteCompletion(@NotNull PsiElement element) {
        return new ControllerRoute(element, getControllerGroupPrefix(element));
    }

    @Nullable
    private String getControllerGroupPrefix(@NotNull PsiElement element) {

        List<String> groupNamespaces = new ArrayList<String>();

        PsiElement routeGroup = PsiTreeUtil.findFirstParent(element, true, new Condition<PsiElement>() {
            @Override
            public boolean value(PsiElement psiElement) {
                return MethodMatcher.getMatchedSignatureWithDepth(psiElement, ROUTE_GROUP, 1) != null;
            }
        });

        while (routeGroup != null) {
            ArrayCreationExpression arrayCreation = PsiTreeUtil.getChildOfType(routeGroup.getParent(), ArrayCreationExpression.class);

            if (arrayCreation != null) {
                for (ArrayHashElement hashElement : arrayCreation.getHashElements()) {
                    if (hashElement.getKey() instanceof StringLiteralExpression) {
                        if ("namespace".equals(((StringLiteralExpression) hashElement.getKey()).getContents())) {
                            if (hashElement.getValue() instanceof StringLiteralExpression) {
                                groupNamespaces.add(((StringLiteralExpression) hashElement.getValue()).getContents());
                            }
                            break;
                        }
                    }
                }
            }


            routeGroup = PsiTreeUtil.findFirstParent(routeGroup, true, new Condition<PsiElement>() {
                @Override
                public boolean value(PsiElement psiElement) {
                    return MethodMatcher.getMatchedSignatureWithDepth(psiElement, ROUTE_GROUP, 1) != null;
                }
            });
        }

        if(groupNamespaces.size() > 0) {
            return StringUtils.stripStart(StringUtils.join(Lists.reverse(groupNamespaces), "\\"), "\\");
        } else {
            return null;
        }
    }

    private class ControllerRoute extends GotoCompletionProvider {

        private String prefix;

        public ControllerRoute(PsiElement element) {
            super(element);
        }

        public ControllerRoute(PsiElement element, String prefix) {
            super(element);

            this.prefix = prefix;
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {
            final Collection<LookupElement> lookupElements = new ArrayList<LookupElement>();

            ControllerCollector.visitControllerActions(getProject(), new ControllerCollector.ControllerActionVisitor() {
                @Override
                public void visit(@NotNull PhpClass phpClass, @NotNull Method method, @NotNull String name, boolean prioritised) {
                    LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(name)
                        .withIcon(LaravelIcons.ROUTE)
                        .withTypeText(phpClass.getPresentableFQN(), true);

                    Parameter[] parameters = method.getParameters();
                    if(parameters.length > 0) {
                        lookupElementBuilder = lookupElementBuilder.withTailText(PhpPresentationUtil.formatParameters(null, parameters).toString());
                    }

                    LookupElement lookupElement = lookupElementBuilder;
                    if(prioritised) {
                        lookupElement = PrioritizedLookupElement.withPriority(lookupElementBuilder, 10);
                    }

                    lookupElements.add(lookupElement);
                }
            }, prefix);

            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(final StringLiteralExpression element) {

            final String content = element.getContents();
            if(StringUtils.isBlank(content)) {
                return Collections.EMPTY_LIST;
            }

            final Collection<PsiElement> targets = new ArrayList<PsiElement>();

            ControllerCollector.visitControllerActions(getProject(), new ControllerCollector.ControllerActionVisitor() {
                @Override
                public void visit(@NotNull PhpClass phpClass, @NotNull Method method, @NotNull String name, boolean prioritised) {
                    if (content.equalsIgnoreCase(name)) {
                        targets.add(method);
                    }

                }
            }, prefix);

            return targets;

        }
    }

    private class ControllerResource extends GotoCompletionProvider {

        private String prefix;

        public ControllerResource(PsiElement element) {
            super(element);
        }

        public ControllerResource(PsiElement element, String prefix) {
            super(element);

            this.prefix = prefix;
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {

            final Collection<LookupElement> lookupElements = new ArrayList<LookupElement>();

            ControllerCollector.visitController(getProject(), new ControllerCollector.ControllerVisitor() {
                @Override
                public void visit(@NotNull PhpClass method, @NotNull String name, boolean prioritised) {
                    LookupElement lookupElement = LookupElementBuilder.create(name).withIcon(LaravelIcons.ROUTE);

                    if(prioritised) {
                        lookupElement = PrioritizedLookupElement.withPriority(lookupElement, 10);
                    }

                    lookupElements.add(lookupElement);
                }
            }, prefix);

            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(final StringLiteralExpression element) {

            final String content = element.getContents();
            if(StringUtils.isBlank(content)) {
                return Collections.emptyList();
            }

            final Collection<PsiElement> targets = new ArrayList<PsiElement>();

            ControllerCollector.visitController(getProject(), new ControllerCollector.ControllerVisitor() {
                @Override
                public void visit(@NotNull PhpClass phpClass, @NotNull String name, boolean prioritised) {
                    if(name.equals(content)) {
                        targets.add(phpClass);
                    }
                }
            }, prefix);

            return targets;

        }
    }

}
