package de.espend.idea.laravel.tests.dic.utils;

import de.espend.idea.laravel.dic.utils.LaravelDicUtil;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see de.espend.idea.laravel.dic.utils.LaravelDicUtil
 */
public class LaravelDicUtilTest  extends LaravelLightCodeInsightFixtureTestCase {


    @Override
    public void setUp() throws Exception {
        super.setUp();

        myFixture.copyFileToProject("Application.php");
    }

    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    /**
     * @see de.espend.idea.laravel.dic.utils.LaravelDicUtil#getCoreAliases
     */
    public void testGetCoreAliases() {
        assertContainsElements(
            LaravelDicUtil.getCoreAliases(getProject()),
            Arrays.asList("auth.driver", "auth", "blade.compiler_foo")
        );
    }

    /**
     * @see de.espend.idea.laravel.dic.utils.LaravelDicUtil#getCoreAliasMap
     */
    public void testGetCoreAliasMap() {

        Map<String, Collection<String>> map = LaravelDicUtil.getCoreAliasMap(getProject());

        assertContainsElements(
            Arrays.asList("Illuminate\\Auth\\Guard", "Illuminate\\Contracts\\Auth\\Guard"),
            map.get("auth.driver")
        );

        assertContainsElements(
            Collections.singletonList("Illuminate\\View\\Compilers\\BladeCompiler"),
            map.get("blade.compiler")
        );

        assertSize(0, map.get("blade.compiler_foo"));
    }
}
