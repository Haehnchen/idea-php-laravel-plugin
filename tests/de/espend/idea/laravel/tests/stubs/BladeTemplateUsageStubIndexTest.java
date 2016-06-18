package de.espend.idea.laravel.tests.stubs;

import de.espend.idea.laravel.stub.BladeTemplateUsageStubIndex;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.stub.BladeTemplateUsageStubIndex
 */
public class BladeTemplateUsageStubIndexTest extends LaravelLightCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("views.php");
    }

    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testThatViewInIndex() {
        assertIndexContains(BladeTemplateUsageStubIndex.KEY,
            "foobar", "foobar_car", "foo/bar", "foo/bar_cat"
        );
    }
}
