package fr.adrienbrault.idea.symfony2plugin.codeInsight;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.completion.CompletionContributorParameter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public abstract class GotoCompletionProvider implements GotoCompletionProviderInterface {

    private final PsiElement element;

    public GotoCompletionProvider(PsiElement element) {
        this.element = element;
    }

    protected Project getProject() {
        return this.element.getProject();
    }

    protected PsiElement getElement() {
        return this.element;
    }

    @NotNull
    public Collection<PsiElement> getPsiTargets(PsiElement element) {
        return Collections.emptyList();
    }

    public void getLookupElements(CompletionContributorParameter parameter) {}
}
