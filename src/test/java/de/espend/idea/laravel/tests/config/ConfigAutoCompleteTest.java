package de.espend.idea.laravel.tests.config;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see de.espend.idea.laravel.blade.BladeDirectiveReferences
 */
public class ConfigAutoCompleteTest extends LaravelLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("simple.php", "config/simple.php");
        myFixture.copyFileToProject("sim-ple.php", "config/sim-ple.php");
        myFixture.copyFileToProject("sub.php", "config/app/sub.php");
    }

    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/laravel/tests/config/fixtures";
    }

    public void testConfigReferences() {
        assertCompletionContains(
            PhpFileType.INSTANCE,
            "<?php\n" + "config('<caret>');",
            "simple.test", "simple.folder.subKey", "sim-ple.test", "app.sub.test"
        );

        assertNavigationMatch(
            PhpFileType.INSTANCE,
            "<?php\n" + "config('simple.test');",
            PlatformPatterns.psiElement(StringLiteralExpression.class).inFile(PlatformPatterns.psiFile().withName("simple.php"))
        );

        assertNavigationMatch(
                PhpFileType.INSTANCE,
                "<?php\n" + "config('app.sub.test');",
                PlatformPatterns.psiElement(StringLiteralExpression.class).inFile(PlatformPatterns.psiFile().withName("sub.php"))
        );
    }
}
