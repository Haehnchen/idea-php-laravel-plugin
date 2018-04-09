package de.espend.idea.laravel.tests.routing.utils;

import com.intellij.psi.PsiFile;
import de.espend.idea.laravel.routing.utils.RoutingUtil;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see de.espend.idea.laravel.routing.utils.RoutingUtil
 */
public class RoutingUtilTest extends LaravelLightCodeInsightFixtureTestCase {

    private PsiFile psiFile;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        myFixture.copyFileToProject("routes.php");
        myFixture.copyFileToProject("routes_api.php", "routes/api.php");
        this.psiFile = myFixture.configureFromTempProjectFile("routes.php");
    }

    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/laravel/tests/routing/utils/fixtures";
    }

    public void testGetRoutesAsNames() {
        assertContainsElements(
            Arrays.asList("profile1", "profile2", "profile3", "profile4"),
            RoutingUtil.getRoutesAsNames(this.psiFile)
        );
    }

    public void testGetRoutesAsNamesNotInsideRouteClassReference() {
        Collection<String> routesAsNames = RoutingUtil.getRoutesAsNames(this.psiFile);
        assertFalse(routesAsNames.contains("foo"));
        assertFalse(routesAsNames.contains("var"));
    }

    public void testGetRoutesAsTargets() {
        for (String s : Arrays.asList("profile1", "profile2", "profile3", "profile4")) {
            assertSize(1, RoutingUtil.getRoutesAsTargets(this.psiFile, s));
        }
    }

    public void testGetRoutesAsTargetsForProjectOfLaravel53() {
        assertSize(1, RoutingUtil.getRoutesAsTargets(getProject(), "profile_api"));
    }

    public void testRoutesCollectionOnProjectScope() {
        for (String s : Arrays.asList("profile1", "profile2", "profile3", "profile4")) {
            assertSize(1, RoutingUtil.getRoutesAsTargets(getProject(), s));
        }

        assertContainsElements(
            RoutingUtil.getRoutesAsNames(getProject()),
            "profile1", "profile2", "profile3", "profile4"
        );
    }
}
