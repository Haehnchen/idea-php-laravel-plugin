package de.espend.idea.laravel.tests.blade;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see de.espend.idea.laravel.blade.BladeDirectiveReferences
 */
public class BladeDirectiveReferencesTest extends LaravelLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("lang.php", "app/lang/fr/foo.php");
        myFixture.copyFileToProject("classes.php");
    }

    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testLangDirectoryReferences() {
        assertCompletionContains(
            BladeFileType.INSTANCE,
            "@lang('<caret>')",
            "foo.between.numeric", "foo.sub-title", "foo.title"
        );

        assertNavigationMatch(
            BladeFileType.INSTANCE,
            "@lang('foo.between.numeric<caret>')",
            PlatformPatterns.psiElement(StringLiteralExpression.class).inFile(PlatformPatterns.psiFile().withName("foo.php"))
        );
    }

    /*
    We need to skip this test; caret host injection not working in this case
    public void testInjectProvidesNavigation() {
        assertNavigationMatch(
            BladeFileType.INSTANCE,
            "@inject('foobar', 'Foobar<caret>\\Bar')",
            PlatformPatterns.psiElement(PhpClass.class).withName("Bar")
        );
    }
    */
}
