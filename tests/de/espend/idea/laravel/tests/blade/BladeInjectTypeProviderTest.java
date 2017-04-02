package de.espend.idea.laravel.tests.blade;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.blade.BladeFileType;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see de.espend.idea.laravel.blade.BladeInjectTypeProvider
 */
public class BladeInjectTypeProviderTest extends LaravelLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testThatInjectVariablesAreAttached() {
        assertPhpReferenceResolveTo(BladeFileType.INSTANCE, "" +
            "@inject('foobar', 'Foobar\\Bar')\n" +
            "{{ $foobar->foo<caret>bar() }}",
            PlatformPatterns.psiElement()
        );
    }
}
