package de.espend.idea.laravel.tests.controller;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.laravel.LaravelSettings;
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
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php\n" +
            "namespace App\\Providers\n" +
            "{\n" +
            "    class RouteServiceProvider implements \\Illuminate\\Foundation\\Support\\Providers\\RouteServiceProvider\n" +
            "    {\n" +
            "        protected $namespace = 'App\\Http\\Controllers\\Foo';\n" +
            "    }\n" +
            "}"
        );

        assertEquals("App\\Http\\Controllers\\Foo", ControllerCollector.getDefaultNamespace(getProject()));
    }

    public void testGetDefaultNamespaceProvidesFallback() {
        assertEquals("\\App\\Http\\Controllers", ControllerCollector.getDefaultNamespace(getProject()));
    }

    public void testGetDefaultNamespaceSettingsWins() {
        LaravelSettings.getInstance(getProject()).routerNamespace = "\\Foo";
        assertEquals("Foo", ControllerCollector.getDefaultNamespace(getProject()));

        LaravelSettings.getInstance(getProject()).routerNamespace = "Foo";
        assertEquals("Foo", ControllerCollector.getDefaultNamespace(getProject()));

        LaravelSettings.getInstance(getProject()).routerNamespace = "";
        assertEquals("\\App\\Http\\Controllers", ControllerCollector.getDefaultNamespace(getProject()));
    }
}
