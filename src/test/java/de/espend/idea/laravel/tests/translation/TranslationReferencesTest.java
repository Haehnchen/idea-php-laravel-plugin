package de.espend.idea.laravel.tests.translation;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.translation.TranslationReferences
 */
public class TranslationReferencesTest extends LaravelLightCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
        myFixture.copyFileToProject("translation.php", "app/lang/fr/messages.php");
        myFixture.copyFileToProject("translation.php", "app/lang/fr/admin/messages.php");
    }

    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/laravel/tests/translation/fixtures";
    }

    public void testThatCompletionContainsTranslation() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/** @var $t \\Illuminate\\Translation\\Translator **/ \n" +
                "$t->get('<caret>');\n",
            "messages.foo", "admin/messages.foo"
        );
    }

    public void testThatNavigationContainsTranslation() {
        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
            "/** @var $t \\Illuminate\\Translation\\Translator **/ \n" +
            "$t->get('messa<caret>ges.foo');\n"
        );

        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
            "/** @var $t \\Illuminate\\Translation\\Translator **/ \n" +
            "$t->get('admin/mess<caret>ages.foo');\n"
        );
    }
}
