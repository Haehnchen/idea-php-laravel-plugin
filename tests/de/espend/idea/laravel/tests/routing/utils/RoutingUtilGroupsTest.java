package de.espend.idea.laravel.tests.routing.utils;

import com.intellij.psi.PsiFile;
import de.espend.idea.laravel.routing.utils.RoutingUtil;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see RoutingUtil
 */
public class RoutingUtilGroupsTest extends LaravelLightCodeInsightFixtureTestCase {

    private PsiFile psiFile;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        myFixture.copyFileToProject("group_routes.php", "routes/group.php");
        this.psiFile = myFixture.configureFromTempProjectFile("routes/group.php");
    }

    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testGroupRouteNamePrefixes() {
        Collection<String> routesAsNames = RoutingUtil.getRoutesAsNames(this.psiFile);
        assertTrue(routesAsNames.contains("foo::bar"));
        assertTrue(routesAsNames.contains("foo::bar::test"));
    }
}
