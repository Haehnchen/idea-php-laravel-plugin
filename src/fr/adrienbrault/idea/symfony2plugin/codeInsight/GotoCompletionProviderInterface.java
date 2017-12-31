package fr.adrienbrault.idea.symfony2plugin.codeInsight;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.completion.CompletionContributorParameter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public interface GotoCompletionProviderInterface {
    void getLookupElements(CompletionContributorParameter parameter);

    @NotNull
    Collection<LookupElement> getLookupElements();

    @NotNull
    default Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {
        return Collections.emptyList();
    }

    @NotNull
    Collection<PsiElement> getPsiTargets(PsiElement element);

    @NotNull
    Collection<? extends PsiElement> getPsiTargets(@NotNull PsiElement psiElement, int offset, @NotNull Editor editor);
}
