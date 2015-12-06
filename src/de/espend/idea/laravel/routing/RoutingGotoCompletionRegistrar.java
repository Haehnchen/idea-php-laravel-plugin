package de.espend.idea.laravel.routing;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.LaravelIcons;
import de.espend.idea.laravel.LaravelProjectComponent;
import de.espend.idea.laravel.routing.utils.RoutingUtil;
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

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class RoutingGotoCompletionRegistrar implements GotoCompletionRegistrar {

    private static MethodMatcher.CallToSignature[] URL_GENERATOR = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\UrlGenerator", "route"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Contracts\\Routing\\UrlGenerator", "route"),
        new MethodMatcher.CallToSignature("\\Collective\\Html\\HtmlBuilder", "linkRoute"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\Redirector", "route"),
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
                if(parent != null && (
                    MethodMatcher.getMatchedSignatureWithDepth(parent, URL_GENERATOR) != null ||
                    PhpElementsUtil.isFunctionReference(parent, 0, "route") ||
                    PhpElementsUtil.isFunctionReference(parent, 0, "link_to_route")
                )) {
                    return new RouteNameGotoCompletionProvider(parent);
                }

                return null;

            }

        });
    }

    private static class RouteNameGotoCompletionProvider extends GotoCompletionProvider {

        public RouteNameGotoCompletionProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {
            Collection<LookupElement> lookupElements = new ArrayList<LookupElement>();
            for (String s : RoutingUtil.getRoutesAsNames(getElement().getProject())) {
                lookupElements.add(LookupElementBuilder.create(s).withIcon(LaravelIcons.ROUTE));
            }
            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {
            String contents = element.getContents();
            if(StringUtils.isBlank(contents)) {
                return Collections.emptyList();
            }

            return RoutingUtil.getRoutesAsTargets(element.getProject(), contents);
        }
    }
}
