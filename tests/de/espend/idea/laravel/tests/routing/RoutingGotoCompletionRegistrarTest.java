package de.espend.idea.laravel.tests.routing;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see de.espend.idea.laravel.routing.RoutingGotoCompletionRegistrar
 */
public class RoutingGotoCompletionRegistrarTest extends LaravelLightCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("routes.php");
    }

    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testRouteNameReferencesAsFunction() {
        for (String s : new String[]{"route", "link_to_route"}) {
            assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                    String.format("%s('<caret>');", s),
                "profile"
            );

            assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                    String.format("%s('profile<caret>');", s),
                PlatformPatterns.psiElement().inFile(PlatformPatterns.psiFile().withName("routes.php"))
            );
        }
    }

    public void testRouteNameReferencesAsMethod() {
        Collection<String[]> providers = new ArrayList<String[]>() {{
            add(new String[] {"Illuminate\\Routing\\UrlGenerator", "route"});
            add(new String[] {"Illuminate\\Contracts\\Routing\\UrlGenerator", "route"});
            add(new String[] {"Collective\\Html\\HtmlBuilder", "linkRoute"});
        }};

        for (String[] provider : providers) {
            assertCompletionContains(PhpFileType.INSTANCE, String.format("<?php\n" +
                    "/** @var $r \\%s */\n" +
                    "$r->%s('<caret>')"
                    , provider[0], provider[1]),
                "profile"
            );

            assertNavigationMatch(PhpFileType.INSTANCE, String.format("<?php\n" +
                    "/** @var $r \\%s */\n" +
                    "$r->%s('profile<caret>')"
                    , provider[0], provider[1]),
                PlatformPatterns.psiElement().inFile(PlatformPatterns.psiFile().withName("routes.php"))
            );
        }
    }
}
