package de.espend.idea.laravel.stub.processor;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class BladeCustomDirectivesVisitor extends PsiRecursiveElementVisitor {

    private static Set<String> availableClasses = new HashSet<>(Arrays.asList("\\Illuminate\\Support\\Facades\\Blade",
            "\\Blade"));

    private Consumer<Pair<PsiElement, String>> consumer;

    public BladeCustomDirectivesVisitor(@NotNull Consumer<Pair<PsiElement, String>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void visitElement(PsiElement element) {

        if (element instanceof MethodReference) {
            visitMethodReference((MethodReference) element);
        }

        super.visitElement(element);
    }

    private void visitMethodReference(MethodReference methodReference) {

        if (!"directive".equals(methodReference.getName())) {
            return;
        }

        PsiElement[] parameters = methodReference.getParameters();

        if (parameters.length == 0) {
            return;
        }

        if (!(parameters[0] instanceof StringLiteralExpression)) {
            return;
        }

        checkClassName(methodReference, ((StringLiteralExpression) parameters[0]).getContents());
    }

    private void checkClassName(MethodReference methodReference, String directiveName) {

        if(methodReference.getClassReference() == null || methodReference.getClassReference().getText() == null) {
            return;
        }

        if(availableClasses.contains(PhpElementsUtil.getFullClassName(methodReference, methodReference.getClassReference().getText()))) {
            consumer.accept(Pair.create(methodReference, directiveName));
        }
    }
}