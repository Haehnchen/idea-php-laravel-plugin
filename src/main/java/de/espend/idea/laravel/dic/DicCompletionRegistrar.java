package de.espend.idea.laravel.dic;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.LaravelIcons;
import de.espend.idea.laravel.LaravelProjectComponent;
import de.espend.idea.laravel.dic.utils.LaravelDicUtil;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DicCompletionRegistrar implements GotoCompletionRegistrar {

    private static MethodMatcher.CallToSignature[] DIC = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\App", "make"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Contracts\\Container\\Container", "make"),
    };

    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {
        registrar.register(PlatformPatterns.psiElement(), psiElement -> {
            if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                return null;
            }

            PsiElement parent = psiElement.getParent();
            if(parent != null && (
                MethodMatcher.getMatchedSignatureWithDepth(parent, DIC) != null ||
                PhpElementsUtil.isFunctionReference(parent, 0, "app")
            )) {
                return new DicGotoCompletionProvider(parent);
            }

            return null;

        });
    }

    private static class DicGotoCompletionProvider extends GotoCompletionProvider {

        public DicGotoCompletionProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {
            Collection<LookupElement> lookupElements = new ArrayList<>();
            for (Map.Entry<String, Collection<String>> entry : LaravelDicUtil.getDicMap(getProject()).entrySet()) {
                lookupElements.add(LookupElementBuilder.create(entry.getKey()).withIcon(LaravelIcons.LARAVEL).withTypeText(StringUtils.join(entry.getValue(), ", "), true));
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

            return LaravelDicUtil.getDicTargets(getProject(), contents);
        }
    }
}
