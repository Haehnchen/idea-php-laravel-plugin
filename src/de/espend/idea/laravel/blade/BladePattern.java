package de.espend.idea.laravel.blade;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.blade.parser.BladeElementTypes;
import com.jetbrains.php.blade.psi.BladePsiDirective;
import com.jetbrains.php.blade.psi.BladePsiDirectiveParameter;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.blade.util.BladePsiUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class BladePattern {
    /**
     * Pattern for @includeIf('auth.<caret>') or @includeIf("auth.<caret>")
     * @param directives "includeIf" without "@"
     */
    @NotNull
    public static PsiElementPattern.Capture<PsiElement> getDirectiveParameterPattern(@NotNull String... directives) {
        return PlatformPatterns.psiElement().withElementType(BladeTokenTypes.DIRECTIVE_PARAMETER_CONTENT)
            .withText(PlatformPatterns.string().with(new PatternCondition<String>("Directive Quoted Content") {
                @Override
                public boolean accepts(@NotNull String s, ProcessingContext processingContext) {
                    return (s.startsWith("'") || s.startsWith("\"")) && s.endsWith("'") || s.endsWith("\"");
                }
            }))
            .withParent(getDirectiveNamePattern(directives));
    }

    /**
     * Pattern for @includeIf('auth.<caret>', [])
     * @param directives "includeIf" without "@"
     */
    public static PsiElementPattern.Capture<PsiElement> getDirectiveWithAdditionalParameterPattern(@NotNull String... directives) {
        return PlatformPatterns.psiElement().withElementType(BladeTokenTypes.DIRECTIVE_PARAMETER_CONTENT)
            .withText(PlatformPatterns.string().with(new PatternCondition<String>("Directive Quoted Content") {
                @Override
                public boolean accepts(@NotNull String s, ProcessingContext processingContext) {
                    return s.matches("^\\s*['|\"]([^'\"]+)['|\"].*");
                }
            }))
            .withParent(getDirectiveNamePattern(directives));
    }

    /**
     * "@foobar"
     */
    private static PsiElementPattern.Capture<BladePsiDirectiveParameter> getDirectiveNamePattern(@NotNull final String[] directives) {
        return PlatformPatterns.psiElement(BladePsiDirectiveParameter.class).withParent(
            PlatformPatterns.psiElement(BladePsiDirective.class).with(new PatternCondition<BladePsiDirective>("Directive Name") {
                @Override
                public boolean accepts(@NotNull BladePsiDirective bladePsiDirective, ProcessingContext processingContext) {
                    for (String directive : directives) {
                        if(("@" + directive).equals(bladePsiDirective.getName())) {
                            return true;
                        }
                    }

                    return false;
                }
            })
        );
    }

    /**
     * {{ $slot }}
     * {{ $title or 'Laravel News' }}
     */
    public static PsiElementPattern.Capture<PsiElement> getTextBlockContentVariablePattern() {
        return PlatformPatterns.psiElement(BladeTokenTypes.TEXT_BLOCK_CONTENT).withParent(
            PlatformPatterns.psiElement(BladeElementTypes.TEXT_BLOCK)
        );
    }

    /**
     * "@foobar('<caret>')"
     *
     * whereas "foobar" is registered a directive
     */
    public static PsiElementPattern.Capture<PsiElement> getParameterDirectiveForElementType(@NotNull IElementType... elementType) {
        return PlatformPatterns.psiElement()
            .withParent(
                PlatformPatterns.psiElement(StringLiteralExpression.class)
                    .withParent(PlatformPatterns.psiElement(ParameterList.class)).with(
                        new MyDirectiveInjectionElementPatternCondition(elementType)
                )
            )
            .withLanguage(PhpLanguage.INSTANCE);
    }

    private static class MyDirectiveInjectionElementPatternCondition extends PatternCondition<PsiElement> {
        private final IElementType[] elementType;

        private MyDirectiveInjectionElementPatternCondition(IElementType... elementType) {
            super("Directive injection");
            this.elementType = elementType;
        }

        @Override
        public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext processingContext) {
            return BladePsiUtil.isDirective(psiElement, elementType);
        }
    }
}
