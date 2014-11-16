package de.espend.idea.laravel.blade;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.BladeLanguage;
import com.jetbrains.php.blade.psi.BladeFileImpl;
import com.jetbrains.php.blade.psi.BladePsiLanguageInjectionHost;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.LaravelIcons;
import de.espend.idea.laravel.LaravelProjectComponent;
import de.espend.idea.laravel.LaravelSettings;
import de.espend.idea.laravel.blade.dict.DirectiveParameterVisitorParameter;
import de.espend.idea.laravel.blade.util.BladeTemplateUtil;
import de.espend.idea.laravel.view.ViewCollector;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionContributor;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

public class BladeDirectiveReferences implements GotoCompletionRegistrar {

    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {
        registrar.register(PlatformPatterns.psiElement(), new GotoCompletionContributor() {

            @Nullable
            @Override
            public GotoCompletionProvider getProvider(@Nullable PsiElement psiElement) {

                if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                    return null;
                }

                if(isDirectiveWithName(psiElement, "startSection")) {
                    return new BladeSectionGotoCompletionProvider(psiElement);
                }

                return null;

            }

        });

        // @extends('extends.bade')
        // @include('include.include')
        registrar.register(PlatformPatterns.psiElement().inVirtualFile(PlatformPatterns.virtualFile().withName(PlatformPatterns.string().endsWith("blade.php"))), new GotoCompletionContributor() {

            @Nullable
            @Override
            public GotoCompletionProvider getProvider(@Nullable PsiElement psiElement) {

                if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                    return null;
                }

                if(isDirectiveWithName(psiElement, "make")) {
                    return new BladeExtendGotoProvider(psiElement);
                }

                return null;

            }

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

    private static class BladeExtendGotoProvider extends GotoCompletionProvider {

        public BladeExtendGotoProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {

            final List<LookupElement> lookupElementList = new ArrayList<LookupElement>();

            final Icon icon = BladeFileType.INSTANCE.getIcon();

            ViewCollector.visitFile(getProject(), new ViewCollector.ViewVisitor() {
                @Override
                public void visit(@NotNull VirtualFile virtualFile, String name) {
                    lookupElementList.add(LookupElementBuilder.create(name).withIcon(icon));
                }
            });

            return lookupElementList;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {

            // disable this, is in core but not really nice
            // if we enable this, Blade path goto is not possible, without a filter on "." prefix
            if(true == true) {
                return Collections.emptyList();
            }

            final String contents = element.getContents();
            if(StringUtils.isBlank(contents)) {
                return Collections.emptyList();
            }

            final Collection<PsiElement> psiElements = new ArrayList<PsiElement>();

            ViewCollector.visitFile(getProject(), new ViewCollector.ViewVisitor() {
                @Override
                public void visit(@NotNull VirtualFile virtualFile, String name) {
                    if(contents.equalsIgnoreCase(name)) {
                        PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(virtualFile);
                        if(psiFile != null) {
                            psiElements.add(psiFile);
                        }
                    }
                }
            });


            return psiElements;
        }
    }

    private static class BladeSectionGotoCompletionProvider extends GotoCompletionProvider {

        public BladeSectionGotoCompletionProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {

            final List<LookupElement> lookupElementList = new ArrayList<LookupElement>();

            PsiLanguageInjectionHost host = InjectedLanguageManager.getInstance(getProject()).getInjectionHost(getElement());
            if (!(host instanceof BladePsiLanguageInjectionHost)) {
                return Collections.emptyList();
            }

            final Set<String> uniqueSet = new HashSet<String>();
            BladeTemplateUtil.visitUpPathSections(host.getContainingFile(), 10, new BladeTemplateUtil.DirectiveParameterVisitor() {
                @Override
                public void visit(@NotNull DirectiveParameterVisitorParameter parameter) {
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
                            }

                            lookupElementList.add(lookupElement);
                        }

                    }
                }
            });

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

            final Set<PsiElement> uniqueSet = new HashSet<PsiElement>();
            BladeTemplateUtil.visitUpPathSections(host.getContainingFile(), 10, new BladeTemplateUtil.DirectiveParameterVisitor() {
                @Override
                public void visit(@NotNull DirectiveParameterVisitorParameter parameter) {
                    if(sectionNameSource.equalsIgnoreCase(parameter.getContent())) {
                        uniqueSet.add(parameter.getPsiElement());
                    }
                }
            });

            return uniqueSet;

        }
    }

}
