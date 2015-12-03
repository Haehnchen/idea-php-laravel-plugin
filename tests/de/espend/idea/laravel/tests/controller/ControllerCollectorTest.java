package de.espend.idea.laravel.tests.controller;

import de.espend.idea.laravel.controller.ControllerCollector;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see de.espend.idea.laravel.controller.ControllerCollector
 */
public class ControllerCollectorTest extends LaravelLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));
    }

    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testGetDefaultNamespace() {
        assertEquals("App\\Http\\Controllers\\Foo", ControllerCollector.getDefaultNamespace(getProject()));
    }
}
