package de.espend.idea.laravel.tests.view;

import de.espend.idea.laravel.LaravelSettings;
import de.espend.idea.laravel.tests.LaravelTempCodeInsightFixtureTestCase;
import de.espend.idea.laravel.view.ViewCollector;
import de.espend.idea.laravel.view.dict.TemplatePath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.view.ViewCollector
 */
public class ViewCollectorTempTest extends LaravelTempCodeInsightFixtureTestCase {
    /**
     * @see ViewCollector#getPaths
     */
    public void testGetPaths() {
        LaravelSettings.getInstance(getProject()).templatePaths = Collections.singletonList(
            new TemplatePath("custom", "foo", true)
        );

        assertEquals(4, ViewCollector.getPaths(getProject()).size());
    }

    /**
     * @see ViewCollector#visitFile
     */
   public void testVisitFile() {
       LaravelSettings.getInstance(getProject()).templatePaths = Collections.singletonList(
           new TemplatePath("custom", "foo", true)
       );

       createFiles(
           "custom/blade/test.blade.php",
           "resources/templates/foobar/bar.html.twig",
           "resources/templates/foobar/php.php"
       );

       Collection<String> templateNames = new ArrayList<>();
       ViewCollector.visitFile(getProject(), (virtualFile, name)
           -> templateNames.add(name)
       );

       assertContainsElements(templateNames, "foo::blade.test");
       assertContainsElements(templateNames, "foobar.bar");
       assertContainsElements(templateNames, "foobar.php");
   }
}
