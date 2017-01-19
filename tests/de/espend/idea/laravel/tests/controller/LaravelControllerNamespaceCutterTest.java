package de.espend.idea.laravel.tests.controller;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.laravel.LaravelSettings;
import de.espend.idea.laravel.controller.namespace.LaravelControllerNamespaceCutter;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see de.espend.idea.laravel.controller.namespace.LaravelControllerNamespaceCutter
 */
public class LaravelControllerNamespaceCutterTest extends LaravelLightCodeInsightFixtureTestCase {

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

        LaravelControllerNamespaceCutter instance = new LaravelControllerNamespaceCutter(getProject(), null);
        instance.cut("App\\Http\\Controllers\\Foo\\TestController", (processedClassName, prioritised)
                -> assertEquals("TestController", processedClassName));
    }

    public void testGetDefaultNamespaceProvidesFallback() {
        LaravelControllerNamespaceCutter instance = new LaravelControllerNamespaceCutter(getProject(), null);
        instance.cut("App\\Http\\Controllers\\FooController", (processedClassName, prioritised)
                -> assertEquals("FooController", processedClassName));
    }

    public void testGetDefaultNamespaceSettingsWins() {
        LaravelSettings.getInstance(getProject()).routerNamespace = "\\Foo";
        LaravelControllerNamespaceCutter instance = new LaravelControllerNamespaceCutter(getProject(), null);
        instance.cut("Foo\\BarController", (processedClassName, prioritised)
                -> assertEquals("BarController", processedClassName));

        LaravelSettings.getInstance(getProject()).routerNamespace = "Foo";
        instance = new LaravelControllerNamespaceCutter(getProject(), null);
        instance.cut("Foo\\BarController", (processedClassName, prioritised)
                -> assertEquals("BarController", processedClassName));

        LaravelSettings.getInstance(getProject()).routerNamespace = "";
        instance = new LaravelControllerNamespaceCutter(getProject(), null);
        instance.cut("App\\Http\\Controllers\\FooController", (processedClassName, prioritised)
                -> assertEquals("FooController", processedClassName));
    }
}
