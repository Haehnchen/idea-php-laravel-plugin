package de.espend.idea.laravel.completion;


import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ThreeState;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.laravel.LaravelProjectComponent;
import de.espend.idea.laravel.LaravelSettings;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpParameterStringCompletionConfidence extends CompletionConfidence {

    @NotNull
    @Override
    public ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {

        if(!(psiFile instanceof PhpFile)) {
            return ThreeState.UNSURE;
        }

        Project project = contextElement.getProject();
        if(!LaravelProjectComponent.isEnabled(project) || !LaravelSettings.getInstance(project).useAutoPopup) {
            return ThreeState.UNSURE;
        }

        PsiElement context = contextElement.getContext();
        if(!(context instanceof StringLiteralExpression)) {
            return ThreeState.UNSURE;
        }

        // $test == "";
        if(context.getParent() instanceof BinaryExpression) {
            return ThreeState.NO;
        }

        // $this->container->get("");
        PsiElement stringContext = context.getContext();
        if(stringContext instanceof ParameterList) {
            return ThreeState.NO;
        }

        // $this->method(... array('foo'); array('bar' => 'foo') ...);
        ArrayCreationExpression arrayCreationExpression = PhpElementsUtil.getCompletableArrayCreationElement(context);
        if(arrayCreationExpression != null && arrayCreationExpression.getContext() instanceof ParameterList) {
            return ThreeState.NO;
        }

        // $array['value']
        if(PlatformPatterns.psiElement().withSuperParent(2, ArrayIndex.class).accepts(contextElement)) {
            return ThreeState.NO;
        }

        return ThreeState.UNSURE;
    }
}
