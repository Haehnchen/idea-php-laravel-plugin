package de.espend.idea.laravel.tests.stubs;

import de.espend.idea.laravel.stub.PhpTemplateUsageStubIndex;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.stub.PhpTemplateUsageStubIndex
 */
public class PhpTemplateUsageStubIndexTest extends LaravelLightCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("views.php");
    }

    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testThatViewInIndex() {
        assertIndexContains(PhpTemplateUsageStubIndex.KEY,
            "foobar", "foobar_car", "foo/bar", "foo/bar_cat"
        );
    }
}
