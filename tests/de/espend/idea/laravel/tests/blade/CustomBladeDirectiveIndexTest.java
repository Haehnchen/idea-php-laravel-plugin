package de.espend.idea.laravel.tests.blade;

import de.espend.idea.laravel.stub.BladeCustomDirectivesStubIndex;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @see de.espend.idea.laravel.stub.BladeCustomDirectivesStubIndex
 */
public class CustomBladeDirectiveIndexTest extends LaravelLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("custom_directives.php");
    }

    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testCustomDirective() {
        assertIndexContains(BladeCustomDirectivesStubIndex.KEY, "datetime");
    }
}
