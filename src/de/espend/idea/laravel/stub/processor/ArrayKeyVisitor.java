package de.espend.idea.laravel.stub.processor;

import com.intellij.psi.PsiElement;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public interface ArrayKeyVisitor {
    public void visit(String key, PsiElement psiKey, boolean isRootElement);
}
