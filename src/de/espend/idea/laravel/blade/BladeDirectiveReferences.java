package de.espend.idea.laravel.blade;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiManager;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.psi.BladeDirectiveElementType;
import com.jetbrains.php.blade.psi.BladePsiDirectiveParameter;
import com.jetbrains.php.blade.psi.BladePsiLanguageInjectionHost;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.LaravelIcons;
import de.espend.idea.laravel.LaravelProjectComponent;
import de.espend.idea.laravel.blade.util.BladePsiUtil;
import de.espend.idea.laravel.blade.util.BladeTemplateUtil;
import de.espend.idea.laravel.translation.TranslationReferences;
import de.espend.idea.laravel.view.ViewCollector;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class BladeDirectiveReferences implements GotoCompletionRegistrar {

    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {
        registrar.register(PlatformPatterns.psiElement(), psiElement -> {
            if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                return null;
            }

            if(isDirective(psiElement, BladeTokenTypes.SECTION_DIRECTIVE)) {
                return new BladeSectionGotoCompletionProvider(psiElement, BladeTokenTypes.SECTION_DIRECTIVE, BladeTokenTypes.YIELD_DIRECTIVE);
            }

            return null;

        });

        // @extends('extends.bade')
        // @include('include.include')
        registrar.register(PlatformPatterns.psiElement().inVirtualFile(PlatformPatterns.virtualFile().withName(PlatformPatterns.string().endsWith("blade.php"))), psiElement -> {
            if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                return null;
            }

            if(isDirectiveWithName(psiElement, "make")) {
                return new BladeExtendGotoProvider(psiElement);
            }

            return null;

        });

        // @lang('lang.foo')
        registrar.register(PlatformPatterns.psiElement().inVirtualFile(PlatformPatterns.virtualFile().withName(PlatformPatterns.string().endsWith("blade.php"))), psiElement -> {
            if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                return null;
            }

            if(BladePsiUtil.isDirectiveWithInstance(psiElement, "Illuminate\\Support\\Facades\\Lang", "get")) {
                return new TranslationReferences.TranslationKey(psiElement);
            }

            return null;

        });

        // @push('my_stack')
        registrar.register(PlatformPatterns.psiElement().inVirtualFile(PlatformPatterns.virtualFile().withName(PlatformPatterns.string().endsWith("blade.php"))), psiElement -> {
            if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                return null;
            }

            if(isDirective(psiElement, BladeTokenTypes.PUSH_DIRECTIVE)) {
                return new BladeSectionGotoCompletionProvider(psiElement, BladeTokenTypes.STACK_DIRECTIVE);
            }

            return null;
        });
    }

    private boolean isDirectiveWithName(PsiElement psiElement, String directiveName) {
        PsiElement stringLiteral = psiElement.getParent();
        if(stringLiteral instanceof StringLiteralExpression) {
            PsiElement parameterList = stringLiteral.getParent();
            if(parameterList instanceof ParameterList) {
                PsiElement methodReference = parameterList.getParent();
                if(methodReference instanceof MethodReference) {
                    String name = ((MethodReference) methodReference).getName();
                    if(name != null && name.equalsIgnoreCase(directiveName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isDirective(@NotNull PsiElement psiElement, @NotNull IElementType elementType) {
        PsiLanguageInjectionHost host = InjectedLanguageManager.getInstance(psiElement.getProject()).getInjectionHost(psiElement);

        return host instanceof BladePsiDirectiveParameter &&
            ((BladePsiDirectiveParameter) host).getDirectiveElementType() == elementType;
    }

    private static class BladeExtendGotoProvider extends GotoCompletionProvider {

        public BladeExtendGotoProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {

            final List<LookupElement> lookupElementList = new ArrayList<>();

            final Icon icon = BladeFileType.INSTANCE.getIcon();

            ViewCollector.visitFile(getProject(), (virtualFile, name) ->
                lookupElementList.add(LookupElementBuilder.create(name).withIcon(icon))
            );

            return lookupElementList;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {

            // disable this, is in core but not really nice
            // if we enable this, Blade path goto is not possible, without a filter on "." prefix
            /* if(true == true) {
                return Collections.emptyList();
            } */

            String contents = element.getContents();
            if(StringUtils.isBlank(contents)) {
                return Collections.emptyList();
            }
            
            contents = contents.replace("/", ".");
            final Collection<PsiElement> psiElements = new ArrayList<>();

            final String finalContents = contents;
            ViewCollector.visitFile(getProject(), (virtualFile, name) -> {
                if(finalContents.equalsIgnoreCase(name)) {
                    PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(virtualFile);
                    if(psiFile != null) {
                        psiElements.add(psiFile);
                    }
                }
            });


            return psiElements;
        }
    }

    private static class BladeSectionGotoCompletionProvider extends GotoCompletionProvider {

        @NotNull
        private final BladeDirectiveElementType[] visitElements;

        public BladeSectionGotoCompletionProvider(@NotNull PsiElement element, @NotNull BladeDirectiveElementType... visitElements) {
            super(element);
            this.visitElements = visitElements;
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {

            final List<LookupElement> lookupElementList = new ArrayList<>();

            PsiLanguageInjectionHost host = InjectedLanguageManager.getInstance(getProject()).getInjectionHost(getElement());
            if (!(host instanceof BladePsiLanguageInjectionHost)) {
                return Collections.emptyList();
            }

            final Set<String> uniqueSet = new HashSet<>();
            BladeTemplateUtil.visitUpPath(host.getContainingFile(), 10, parameter -> {
                if (!uniqueSet.contains(parameter.getContent())) {
                    uniqueSet.add(parameter.getContent());

                    LookupElementBuilder lookupElement = LookupElementBuilder.create(parameter.getContent()).withIcon(LaravelIcons.LARAVEL);
                    Set<String> templateNames = BladeTemplateUtil.getFileTemplateName(parameter.getPsiElement().getProject(), parameter.getPsiElement().getContainingFile().getVirtualFile());

                    for(String templateName: templateNames) {

                        lookupElement = lookupElement.withTypeText(templateName, true);

                        if(parameter.getElementType() == BladeTokenTypes.SECTION_DIRECTIVE) {
                            lookupElement = lookupElement.withTailText("(section)", true);
                        } else if(parameter.getElementType() == BladeTokenTypes.YIELD_DIRECTIVE) {
                            lookupElement = lookupElement.withTailText("(yield)", true);
                        } else if(parameter.getElementType() == BladeTokenTypes.STACK_DIRECTIVE) {
                            lookupElement = lookupElement.withTailText("(stack)", true);
                        }

                        lookupElementList.add(lookupElement);
                    }
                }
            }, this.visitElements);

            return lookupElementList;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {

            PsiLanguageInjectionHost host = InjectedLanguageManager.getInstance(getProject()).getInjectionHost(getElement());
            if (!(host instanceof BladePsiLanguageInjectionHost)) {
                return Collections.emptyList();
            }

            final String sectionNameSource = element.getContents();
            if(StringUtils.isBlank(sectionNameSource)) {
                return Collections.emptyList();
            }

            final Set<PsiElement> uniqueSet = new HashSet<>();
            BladeTemplateUtil.visitUpPath(host.getContainingFile(), 10, parameter -> {
                if(sectionNameSource.equalsIgnoreCase(parameter.getContent())) {
                    uniqueSet.add(parameter.getPsiElement());
                }
            }, this.visitElements);

            return uniqueSet;
        }
    }

}
