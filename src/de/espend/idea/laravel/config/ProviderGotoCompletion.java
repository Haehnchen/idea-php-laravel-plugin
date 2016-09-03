package de.espend.idea.laravel.config;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.*;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ProviderGotoCompletion implements GotoCompletionRegistrar {

    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {
        registrar.register(PlatformPatterns.psiElement(), psiElement -> {
            if(psiElement == null || !psiElement.getContainingFile().getName().contains("app.php")) {
                return null;
            }

            // array('providers' => array('foo'))
            PsiElement literal = psiElement.getParent();
            if(literal instanceof StringLiteralExpression) {
                PsiElement arrayValue = literal.getParent();
                if(arrayValue.getNode().getElementType() == PhpElementTypes.ARRAY_VALUE) {
                    PsiElement arrayCreation = arrayValue.getParent();
                    if(arrayCreation instanceof ArrayCreationExpression) {
                        PsiElement arrayValueKey = arrayCreation.getParent();
                        if(arrayValueKey.getNode().getElementType() == PhpElementTypes.ARRAY_VALUE) {
                            PsiElement hashArrayElement = arrayValueKey.getParent();
                            if(hashArrayElement instanceof ArrayHashElement) {
                                PhpPsiElement key = ((ArrayHashElement) hashArrayElement).getKey();
                                if(key instanceof StringLiteralExpression && "providers".equals(((StringLiteralExpression) key).getContents())) {
                                    return new ProviderName(psiElement);
                                }

                            }
                        }
                    }
                }
            }

            return null;
        });

    }

    private class ProviderName extends GotoCompletionProvider {

        public ProviderName(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {

            return PhpIndex.getInstance(getProject()).getAllSubclasses("\\Illuminate\\Support\\ServiceProvider")
                .stream()
                .map(phpClass -> LookupElementBuilder.create(phpClass.getPresentableFQN())
                    .withIcon(phpClass.getIcon())).collect(Collectors.toCollection(ArrayList::new)
                );
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {

            String contents = element.getContents();
            if(StringUtils.isBlank(contents)) {
                return Collections.emptyList();
            }

            Collection<PsiElement> psiElements = new ArrayList<>();
            for(PhpClass phpClass: PhpElementsUtil.getClassesOrInterfaces(element.getProject(), contents)) {
                psiElements.add(phpClass);
            }

            return psiElements;
        }
    }
}
