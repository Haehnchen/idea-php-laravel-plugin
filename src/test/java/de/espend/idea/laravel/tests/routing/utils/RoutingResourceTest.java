package de.espend.idea.laravel.tests.routing.utils;

import com.intellij.psi.PsiFile;
import de.espend.idea.laravel.routing.utils.RoutingUtil;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.util.Collection;

/**
 * @author Adel Fayzrakhmanov <adel.faiz@gmail.com>
 *
 * @see RoutingUtil
 */
public class RoutingResourceTest extends LaravelLightCodeInsightFixtureTestCase {

    private PsiFile psiFile;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        myFixture.copyFileToProject("resource_routes.php", "routes/resource.php");
        this.psiFile = myFixture.configureFromTempProjectFile("routes/resource.php");
    }

    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/laravel/tests/routing/utils/fixtures";
    }

    public void testRouteResourceNames() {
        Collection<String> routesAsNames = RoutingUtil.getRoutesAsNames(this.psiFile);

        assertContainsElements(routesAsNames, "test1.index", "test1.show", "test1.store");

        assertContainsElements(routesAsNames, "foo::test2.index", "foo::test2.show", "foo::test2.store");

        assertContainsElements(routesAsNames, "foo::bar::test3.index", "foo::bar::test3.show", "foo::bar::test3.store");

        assertContainsElements(routesAsNames, "testNonPrefix.index", "testNonPrefix.show", "testNonPrefix.store");

        assertContainsElements(routesAsNames, "testLongRouteName.index");

        assertContainsElements(routesAsNames, "test4.index", "test4.show");
        assertFalse(routesAsNames.contains("test4.store"));
        assertFalse(routesAsNames.contains("test4.destroy"));

        assertContainsElements(routesAsNames, "test5.index", "test5.create", "test5.store");
        assertFalse(routesAsNames.contains("test5.show"));
        assertFalse(routesAsNames.contains("test5.edit"));
        assertFalse(routesAsNames.contains("test5.update"));
        assertFalse(routesAsNames.contains("test5.destroy"));

        assertContainsElements(routesAsNames, "named.route", "test6.show");
        assertFalse(routesAsNames.contains("test6.index")); // It was overridden
        assertFalse(routesAsNames.contains("test6.wrong"));
        assertFalse(routesAsNames.contains("nothing"));
    }
}
