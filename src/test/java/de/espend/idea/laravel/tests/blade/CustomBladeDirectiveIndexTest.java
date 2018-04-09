package de.espend.idea.laravel.tests.blade;

import de.espend.idea.laravel.stub.BladeCustomDirectivesStubIndex;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.stub.BladeCustomDirectivesStubIndex
 */
public class CustomBladeDirectiveIndexTest extends LaravelLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("custom_directives.php");
    }

    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/laravel/tests/blade/fixtures";
    }

    public void testCustomDirective() {
        assertIndexContains(BladeCustomDirectivesStubIndex.KEY, "datetime");
        assertIndexContains(BladeCustomDirectivesStubIndex.KEY, "foo");
    }
}
