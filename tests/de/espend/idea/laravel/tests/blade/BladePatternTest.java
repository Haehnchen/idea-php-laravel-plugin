package de.espend.idea.laravel.tests.blade;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.blade.BladeFileType;
import de.espend.idea.laravel.blade.BladePattern;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class BladePatternTest extends LaravelLightCodeInsightFixtureTestCase {
    /**
     * @see de.espend.idea.laravel.blade.BladePattern#getDirectiveParameterPattern
     */
    public void testGetDirectiveParameterPattern() {
        myFixture.configureByText(BladeFileType.INSTANCE, "@includeIf('auth<caret>')");
        PsiElement psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());

        assertTrue(BladePattern.getDirectiveParameterPattern("includeIf").accepts(psiElement));
    }
}
