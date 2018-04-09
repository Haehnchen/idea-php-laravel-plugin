package de.espend.idea.laravel.tests.blade;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import de.espend.idea.laravel.blade.BladePattern;
import de.espend.idea.laravel.blade.BladePsiElementFactory;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class BladePatternTest extends LaravelLightCodeInsightFixtureTestCase {
    /**
     * @see de.espend.idea.laravel.blade.BladePattern#getDirectiveParameterPattern
     */
    public void testGetDirectiveParameterPattern() {
        // now HTML elements
        if(true) return;

        myFixture.configureByText(BladeFileType.INSTANCE, "@foobar('au<caret>th')");
        PsiElement psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());

        assertTrue(BladePattern.getDirectiveParameterPattern("foobar").accepts(psiElement));
    }

    /**
     * @see de.espend.idea.laravel.blade.BladePattern#getDirectiveWithAdditionalParameterPattern
     */
    public void testGetDirectiveParameterWithAdditionalParameterPattern() {
        // now HTML elements
        if(true) return;

        myFixture.configureByText(BladeFileType.INSTANCE, "@foobar('auth<caret>', [])");
        PsiElement psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());

        assertTrue(BladePattern.getDirectiveWithAdditionalParameterPattern("foobar").accepts(psiElement));
    }

    /**
     * @see de.espend.idea.laravel.blade.BladePattern#getTextBlockContentVariablePattern
     */
    public void testGetTextBlockContentVariablePattern() {
        PsiElement fromText = BladePsiElementFactory.createFromText(
            getProject(),
            BladeTokenTypes.TEXT_BLOCK_CONTENT,
            "<html>{{ $slot }}</html>"
        );

        assertTrue(BladePattern.getTextBlockContentVariablePattern().accepts(fromText));
    }
}
