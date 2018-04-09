package de.espend.idea.laravel.tests.dic;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see de.espend.idea.laravel.dic.DicCompletionRegistrar
 */
public class DicCompletionRegistrarTest extends LaravelLightCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("Application.php");
    }

    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/laravel/tests/dic/fixtures";
    }

    public void testCompletion() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "app('<caret>')",
            "foo"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "\\App::make('<caret>')",
            "foo"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/** @var $f \\Illuminate\\Contracts\\Container\\Container */" +
                "$f->make('<caret>')",
            "foo"
        );
    }

    public void testNavigation() {
        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "app('foo<caret>')",
            PlatformPatterns.psiElement(PhpClass.class)
        );

        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "\\App::make('foo<caret>')",
            PlatformPatterns.psiElement(PhpClass.class)
        );

        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "/** @var $f \\Illuminate\\Contracts\\Container\\Container */" +
                "$f->make('foo<caret>')",
            PlatformPatterns.psiElement(PhpClass.class)
        );
    }
}
