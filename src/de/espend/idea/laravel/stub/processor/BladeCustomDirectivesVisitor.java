package de.espend.idea.laravel.stub.processor;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class BladeCustomDirectivesVisitor extends PsiRecursiveElementVisitor {

    private MethodMatcher.CallToSignature[] signatures = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\Illuminate\\Support\\Facades\\Blade", "directive"),
            new MethodMatcher.CallToSignature("\\Blade", "directive"),
            new MethodMatcher.CallToSignature("Blade", "directive")
    };

    private Consumer<Pair<PsiElement, String>> consumer;

    public BladeCustomDirectivesVisitor(@NotNull Consumer<Pair<PsiElement, String>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void visitElement(PsiElement element) {
        MethodMatcher.MethodMatchParameter result = MethodMatcher.getMatchedSignatureWithDepth(element, signatures);

        if(result != null) {
            if(result.getParameters().length >= 2) {
                PsiElement directiveNameElement = result.getParameters()[0];

                if(directiveNameElement instanceof StringLiteralExpression) {
                    consumer.accept(Pair.create(directiveNameElement, ((StringLiteralExpression)directiveNameElement).getContents()));
                }
            }
        }

        super.visitElement(element);
    }
}