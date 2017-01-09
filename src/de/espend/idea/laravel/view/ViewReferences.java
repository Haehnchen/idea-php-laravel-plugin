package de.espend.idea.laravel.view;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.LaravelIcons;
import de.espend.idea.laravel.LaravelProjectComponent;
import de.espend.idea.laravel.blade.BladePattern;
import de.espend.idea.laravel.blade.util.BladePsiUtil;
import de.espend.idea.laravel.blade.util.BladeTemplateUtil;
import de.espend.idea.laravel.util.PsiElementUtils;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import fr.adrienbrault.idea.symfony2plugin.util.ParameterBag;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        /*
         * @includeIf('view.name')
         * @component('view.name')
         */
        registrar.register(BladePattern.getDirectiveParameterPattern("includeIf", "component"), psiElement -> {
            if (psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                return null;
            }

            return new BladeViewProvider(psiElement);
        });

        /*
         * @slot('title')
         */
        registrar.register(BladePattern.getDirectiveParameterPattern("slot"), psiElement -> {
            if (psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                return null;
            }

            return new MyBladeSlotDirectiveCompletionProvider(psiElement);
        });
    }

    private static class BladeViewProvider extends ViewProvider {
        private BladeViewProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(final StringLiteralExpression element) {
            return Collections.emptyList();
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(@NotNull PsiElement element) {
            String contents = element.getText();

            return this.getTemplateTargets(
                element.getProject(),
                contents.replace("\"", "'").substring(1, contents.length() - 1)
            );
        }
    }

    private static class ViewProvider extends GotoCompletionProvider {

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

            return getTemplateTargets(element.getProject(), content);
        }

        @NotNull
        protected Collection<PsiElement> getTemplateTargets(@NotNull Project project, @NotNull String content) {
            final Collection<PsiElement> targets = new ArrayList<>();

            for(VirtualFile virtualFile: BladeTemplateUtil.resolveTemplateName(project, content)) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                if(psiFile != null) {
                    targets.add(psiFile);
                }
            }

            return targets;
        }
    }

    /**
     * Navigation and completion
     *
     * "@slot('title')"
     */
    private static class MyBladeSlotDirectiveCompletionProvider extends GotoCompletionProvider {
        private final PsiElement psiElement;

        MyBladeSlotDirectiveCompletionProvider(PsiElement psiElement) {
            super(psiElement);
            this.psiElement = psiElement;
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {
            String component = BladePsiUtil.findComponentForSlotScope(psiElement);
            if(component == null) {
                return Collections.emptyList();
            }

            Collection<String> slots = new HashSet<>();

            for (VirtualFile virtualFile : BladeTemplateUtil.resolveTemplateName(getProject(), component)) {
                PsiFile file = PsiManager.getInstance(getProject()).findFile(virtualFile);
                if(file != null) {
                    slots.addAll(BladePsiUtil.collectPrintBlockVariables(file));
                }
            }

            return slots.stream()
                .map((Function<String, LookupElement>) s ->
                    LookupElementBuilder.create(s).withIcon(LaravelIcons.LARAVEL).withTypeText(component, true)
                )
                .collect(Collectors.toList());
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(PsiElement element) {
            List<String> strings = BladePsiUtil.extractParameters(element.getText());
            if(strings.size() < 1) {
                return Collections.emptyList();
            }

            String variable = PsiElementUtils.trimQuote(strings.get(0));
            if(StringUtils.isBlank(variable)) {
                return Collections.emptyList();
            }

            String component = BladePsiUtil.findComponentForSlotScope(psiElement);
            if(component == null) {
                return Collections.emptyList();
            }

            Collection<PsiElement> psiElements = new ArrayList<>();

            for (VirtualFile virtualFile : BladeTemplateUtil.resolveTemplateName(getProject(), component)) {
                PsiFile file = PsiManager.getInstance(getProject()).findFile(virtualFile);
                if(file != null) {
                    psiElements.addAll(BladePsiUtil.collectPrintBlockVariableTargets(file, variable));
                }
            }

            return psiElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {
            return Collections.emptyList();
        }
    }
}
