package de.espend.idea.laravel.tests.stubs;

import de.espend.idea.laravel.stub.TranslationKeyStubIndex;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.stub.TranslationKeyStubIndex
 */
public class TranslationKeyStubIndexTest extends LaravelLightCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("translation.php", "app/lang/fr/messages.php");
        myFixture.copyFileToProject("translation.php", "app/lang/fr_FR/foo_fr.php");
        myFixture.copyFileToProject("translation.php", "app/lang/packages/en/packages/packages_messages.php");
        myFixture.copyFileToProject("translation.php", "app/lang/packages/fr_FR/packages/packages_fr_messages.php");
    }

    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testNamespacesTranslationInIndex() {
        assertIndexContains(TranslationKeyStubIndex.KEY, "messages.foo");
        assertIndexContains(TranslationKeyStubIndex.KEY, "foo_fr.foo");
        assertIndexContains(TranslationKeyStubIndex.KEY, "packages_messages.foo");
        assertIndexContains(TranslationKeyStubIndex.KEY, "packages_fr_messages.foo");
    }
}
