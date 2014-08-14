package de.espend.idea.laravel.controller;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.LaravelProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.*;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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

    private static MethodMatcher.CallToSignature[] REDIRECT = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\Redirector", "action"),
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
                if(parent == null) {
                    return null;
                }

                if (MethodMatcher.getMatchedSignatureWithDepth(parent, ROUTE, 1) != null) {
                    return new ControllerRoute(parent);
                }

                if (MethodMatcher.getMatchedSignatureWithDepth(parent, REDIRECT) != null) {
                    return new ControllerRoute(parent);
                }

                return null;

            }
        });
    }

    private class ControllerRoute extends GotoCompletionProvider {

        public ControllerRoute(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {
            final Collection<LookupElement> lookupElements = new ArrayList<LookupElement>();

            ControllerCollector.visitController(getProject(), new ControllerCollector.ControllerVisitor() {
                @Override
                public void visit(@NotNull Method method, String name) {
                    lookupElements.add(LookupElementBuilder.create(name).withIcon(method.getIcon()));
                }
            });

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

            ControllerCollector.visitController(getProject(), new ControllerCollector.ControllerVisitor() {
                @Override
                public void visit(@NotNull Method method, String name) {
                    if (content.equalsIgnoreCase(name)) {
                        targets.add(method);
                    }

                }
            });

            return targets;

        }
    }

}
