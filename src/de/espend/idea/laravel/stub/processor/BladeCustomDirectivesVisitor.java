package de.espend.idea.laravel.stub.processor;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import fr.adrienbrault.idea.symfony2plugin.Symfony2InterfacesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class BladeCustomDirectivesVisitor extends PsiRecursiveElementVisitor {

    private Set<String> availableClasses = new HashSet<>();

    private Consumer<Pair<PsiElement, String>> consumer;

    public BladeCustomDirectivesVisitor(@NotNull Consumer<Pair<PsiElement, String>> consumer) {
        this.consumer = consumer;

        availableClasses.add("\\Illuminate\\Support\\Facades\\Blade");
        availableClasses.add("\\Blade");
        availableClasses.add("Blade");
    }

    @Override
    public void visitElement(PsiElement element) {

        try {
            if (!(element instanceof MethodReference)) {
                return;
            }

            MethodReference methodReference = (MethodReference) element;
            PsiElement[] parameters = methodReference.getParameters();

            if (parameters.length == 0) {
                return;
            }

            if (!"directive".equals(methodReference.getName())) {
                return;
            }

            Method method = Symfony2InterfacesUtil.getMultiResolvedMethod(methodReference);

            if (method == null) {
                return;
            }

            if (method.getContainingClass() == null || !availableClasses.contains(method.getContainingClass().getPresentableFQN())) {
                return;
            }

            if (!(parameters[0] instanceof StringLiteralExpression)) {
                return;
            }

            consumer.accept(Pair.create(element, ((StringLiteralExpression) parameters[0]).getContents()));

        } finally {
            super.visitElement(element);
        }
    }
}