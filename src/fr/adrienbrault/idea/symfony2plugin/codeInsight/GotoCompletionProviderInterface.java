package fr.adrienbrault.idea.symfony2plugin.codeInsight;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.completion.CompletionContributorParameter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface GotoCompletionProviderInterface {
    void getLookupElements(CompletionContributorParameter parameter);

    @NotNull
    Collection<LookupElement> getLookupElements();

    @NotNull
    Collection<PsiElement> getPsiTargets(StringLiteralExpression element);

    @NotNull
    Collection<PsiElement> getPsiTargets(PsiElement element);
}
