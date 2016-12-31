package de.espend.idea.laravel.view;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.LaravelProjectComponent;
import de.espend.idea.laravel.blade.util.BladePsiUtil;
import de.espend.idea.laravel.blade.util.BladeTemplateUtil;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import fr.adrienbrault.idea.symfony2plugin.util.ParameterBag;
import fr.adrienbrault.idea.symfony2plugin.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ViewReferences implements GotoCompletionRegistrar {

    private static MethodMatcher.CallToSignature[] VIEWS = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "make"),
        new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "exists"),
        new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "alias"),
        new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "name"),
        new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "of"),
        new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "renderEach"),
        new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "callComposer"),
        new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "callCreator"),
    };

    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {
        registrar.register(PlatformPatterns.psiElement().withParent(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE), psiElement -> {
            if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                return null;
            }

            PsiElement parent = psiElement.getParent();
            if(parent == null) {
                return null;
            }

            if (MethodMatcher.getMatchedSignatureWithDepth(parent, VIEWS) != null) {
                return new ViewProvider(parent);
            }

            return null;
        });

        registrar.register(PlatformPatterns.psiElement().withParent(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE), psiElement -> {
            if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                return null;
            }

            PsiElement stringLiteral = psiElement.getParent();
            if(!(stringLiteral instanceof StringLiteralExpression)) {
                return null;
            }

            if(!de.espend.idea.laravel.util.PsiElementUtils.isFunctionReference(stringLiteral, "view", 0)) {
                return null;
            }

            return new ViewProvider(stringLiteral);
        });

        /*
         * @each('view.name', $jobs, 'job')
         * @each('view.name', $jobs, 'job', 'view.empty')
         */
        registrar.register(PlatformPatterns.psiElement().withParent(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE), psiElement -> {
            if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                return null;
            }

            if(!BladePsiUtil.isDirective(psiElement, BladeTokenTypes.EACH_DIRECTIVE)) {
                return null;
            }

            PsiElement stringLiteral = psiElement.getParent();
            if(!(stringLiteral instanceof StringLiteralExpression)) {
                return null;
            }

            ParameterBag parameterBag = PhpElementsUtil.getCurrentParameterIndex(stringLiteral);
            if(parameterBag == null || (parameterBag.getIndex() != 0 && parameterBag.getIndex() != 3)) {
                return null;
            }

            return new ViewProvider(stringLiteral);
        });
    }

    private class ViewProvider extends GotoCompletionProvider {

        public ViewProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {

            final Collection<LookupElement> lookupElements = new ArrayList<>();

            ViewCollector.visitFile(getProject(), (virtualFile, name) ->
                lookupElements.add(LookupElementBuilder.create(name).withIcon(virtualFile.getFileType().getIcon()))
            );

            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(final StringLiteralExpression element) {

            final String content = element.getContents();
            if(StringUtils.isBlank(content)) {
                return Collections.emptyList();
            }

            final Collection<PsiElement> targets = new ArrayList<>();

            for(VirtualFile virtualFile: BladeTemplateUtil.resolveTemplateName(element.getProject(), content)) {
                PsiFile psiFile = PsiManager.getInstance(element.getProject()).findFile(virtualFile);
                if(psiFile != null) {
                    targets.add(psiFile);
                }
            }

            return targets;
        }
    }

}
