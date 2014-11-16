package de.espend.idea.laravel.view;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.LaravelProjectComponent;
import de.espend.idea.laravel.blade.util.BladeTemplateUtil;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionContributor;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
        registrar.register(PlatformPatterns.psiElement().withParent(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE), new GotoCompletionContributor() {
            @Nullable
            @Override
            public GotoCompletionProvider getProvider(@Nullable PsiElement psiElement) {

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
            }
        });

        registrar.register(PlatformPatterns.psiElement().withParent(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE), new GotoCompletionContributor() {
            @Nullable
            @Override
            public GotoCompletionProvider getProvider(@Nullable PsiElement psiElement) {

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
            }
        });

    }

    private class ViewProvider extends GotoCompletionProvider {

        public ViewProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {

            final Collection<LookupElement> lookupElements = new ArrayList<LookupElement>();

            ViewCollector.visitFile(getProject(), new ViewCollector.ViewVisitor() {
                @Override
                public void visit(@NotNull VirtualFile virtualFile, String name) {
                    lookupElements.add(LookupElementBuilder.create(name).withIcon(virtualFile.getFileType().getIcon()));
                }
            });

            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(final StringLiteralExpression element) {

            final String content = element.getContents();
            if(StringUtils.isBlank(content)) {
                return Collections.emptyList();
            }

            final Collection<PsiElement> targets = new ArrayList<PsiElement>();

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
