package de.espend.idea.laravel.blade;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.blade.parser.BladeElementTypes;
import com.jetbrains.php.blade.psi.BladePsiDirective;
import com.jetbrains.php.blade.psi.BladePsiDirectiveParameter;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
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
            .withParent(
                PlatformPatterns.psiElement(BladePsiDirectiveParameter.class).withParent(
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
                )
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
}
