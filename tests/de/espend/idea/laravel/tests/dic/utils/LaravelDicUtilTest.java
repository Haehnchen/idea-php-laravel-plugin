package de.espend.idea.laravel.tests.dic.utils;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
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
public class LaravelDicUtilTest extends LaravelLightCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        myFixture.copyFileToProject("Application.php");
        myFixture.copyFileToProject("ServiceProvider.php");
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

        assertContainsElements(
            Collections.singletonList("Illuminate\\Auth\\Passwords\\TokenRepositoryInterface"),
            map.get("auth.password.tokens")
        );

        assertSize(0, map.get("blade.compiler_foo"));
    }

    /**
     * @see de.espend.idea.laravel.dic.utils.LaravelDicUtil#getServiceProviderMap
     */
    public void testGetServiceProviderMap() {
        Map<String, Collection<String>> map = LaravelDicUtil.getServiceProviderMap(getProject());
        assertContainsElements(map.get("foo"), "DateTime");
        assertContainsElements(map.get("foo1"), "DateTime");
    }

    /**
     * @see de.espend.idea.laravel.dic.utils.LaravelDicUtil#getDicMap
     */
    public void testGetDicMap() {
        Map<String, Collection<String>> map = LaravelDicUtil.getDicMap(getProject());
        assertContainsElements(map.get("foo"), "DateTime");
        assertContainsElements(map.get("blade.compiler"), "Illuminate\\View\\Compilers\\BladeCompiler");
        assertContainsElements(map.get("blade.compiler_class"), "Illuminate\\View\\Compilers\\BladeCompiler");
        assertContainsElements(map.get("blade.compiler_class_array"), "Illuminate\\View\\Compilers\\BladeCompiler");
    }

    /**
     * @see de.espend.idea.laravel.dic.utils.LaravelDicUtil#getDicTargets
     */
    public void testGetDicTargets() {
        PlatformPatterns.psiElement(PhpClass.class).withName("DateTime").accepts(
            ContainerUtil.getFirstItem(LaravelDicUtil.getDicTargets(getProject(), "foo"))
        );

        PlatformPatterns.psiElement(PhpClass.class).withName("DateTime").accepts(
            ContainerUtil.getFirstItem(LaravelDicUtil.getDicTargets(getProject(), "foo1"))
        );
    }
}
