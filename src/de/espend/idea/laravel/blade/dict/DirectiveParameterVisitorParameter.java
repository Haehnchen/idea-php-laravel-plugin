package de.espend.idea.laravel.blade.dict;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DirectiveParameterVisitorParameter {

    private final PsiElement psiElement;
    private final String content;
    private final IElementType elementType;

    public DirectiveParameterVisitorParameter(@NotNull PsiElement psiElement, @NotNull String content, @NotNull IElementType elementType) {

        this.psiElement = psiElement;
        this.content = content;
        this.elementType = elementType;
    }

    @NotNull
    public PsiElement getPsiElement() {
        return psiElement;
    }

    @NotNull
    public String getContent() {
        return content;
    }

    @NotNull
    public IElementType getElementType() {
        return elementType;
    }

}
