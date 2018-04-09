package de.espend.idea.laravel.tests.stubs;

import de.espend.idea.laravel.stub.RouteIndexExtension;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.stub.RouteIndexExtension
 */
public class RouteIndexExtensionTest extends LaravelLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("routes.php");
        myFixture.copyFileToProject("routes_api.php", "routes/foo.php");
        myFixture.copyFileToProject("routes_subfolder.php", "routes/foo/foo.php");
    }

    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/laravel/tests/stubs/fixtures";
    }
    public void testThatRoutesForLaravel52() {
        assertIndexContains(RouteIndexExtension.KEY,
            "profile"
        );
    }

    public void testThatRoutesForLaravel53() {
        assertIndexContains(RouteIndexExtension.KEY,
            "foobar"
        );

        assertIndexContains(RouteIndexExtension.KEY,
            "profile_subfolder"
        );
    }
}
