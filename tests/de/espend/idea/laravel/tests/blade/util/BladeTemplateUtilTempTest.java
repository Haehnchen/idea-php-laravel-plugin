package de.espend.idea.laravel.tests.blade.util;

import com.intellij.openapi.vfs.VirtualFile;
import de.espend.idea.laravel.LaravelSettings;
import de.espend.idea.laravel.blade.util.BladeTemplateUtil;
import de.espend.idea.laravel.tests.LaravelTempCodeInsightFixtureTestCase;
import de.espend.idea.laravel.view.dict.TemplatePath;

import java.util.Collections;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see de.espend.idea.laravel.blade.util.BladeTemplateUtil
 */
public class BladeTemplateUtilTempTest extends LaravelTempCodeInsightFixtureTestCase {
    /**
     * @see BladeTemplateUtil#resolveTemplateName
     */
    public void testResolveTemplateNameByName() {
        LaravelSettings.getInstance(getProject()).templatePaths = Collections.singletonList(
            new TemplatePath("custom", "foo", true)
        );

        createFiles(
            "resources/views/foo/foo_blade.blade.php",
            "resources/views/foo/bar/foo_blade.blade.php",
            "resources/views/foo_blade.blade.php",
            "app/views/foo_twig.html.twig",
            "resources/templates/foo_php.php",
            "custom/foo/namespace_blade.blade.php",
            "custom/foo/bar/namespace_blade.blade.php"
        );

        assertEquals(
            "foo_blade.blade.php",
            BladeTemplateUtil.resolveTemplateName(getProject(), "foo.foo_blade").iterator().next().getName()
        );

        assertEquals(
            "foo_blade.blade.php",
            BladeTemplateUtil.resolveTemplateName(getProject(), "foo.bar.foo_blade").iterator().next().getName()
        );

        assertEquals(
            "foo_blade.blade.php",
            BladeTemplateUtil.resolveTemplateName(getProject(), "foo_blade").iterator().next().getName()
        );

        assertEquals(
            "foo_twig.html.twig",
            BladeTemplateUtil.resolveTemplateName(getProject(), "foo_twig").iterator().next().getName()
        );

        assertEquals(
            "foo_php.php",
            BladeTemplateUtil.resolveTemplateName(getProject(), "foo_php").iterator().next().getName()
        );

        assertEquals(
            "namespace_blade.blade.php",
            BladeTemplateUtil.resolveTemplateName(getProject(), "foo::foo.namespace_blade").iterator().next().getName()
        );

        assertEquals(
            "namespace_blade.blade.php",
            BladeTemplateUtil.resolveTemplateName(getProject(), "foo::foo.bar.namespace_blade").iterator().next().getName()
        );
    }

    /**
     * @see BladeTemplateUtil#resolveTemplateName
     */
    public void testResolveTemplateNameByVirtualFile() {
        LaravelSettings.getInstance(getProject()).templatePaths = Collections.singletonList(
            new TemplatePath("custom", "foo", true)
        );

        VirtualFile file = createFile("resources/views/foo/foo_blade.blade.php");
        assertContainsElements(BladeTemplateUtil.resolveTemplateName(getProject(), file), "foo.foo_blade");

        file = createFile("resources/views/foo/bar/foo_blade.blade.php");
        assertContainsElements(BladeTemplateUtil.resolveTemplateName(getProject(), file), "foo.bar.foo_blade");

        file = createFile("resources/views/foo_blade.blade.php");
        assertContainsElements(BladeTemplateUtil.resolveTemplateName(getProject(), file), "foo_blade");

        file = createFile("app/views/foo_twig.html.twig");
        assertContainsElements(BladeTemplateUtil.resolveTemplateName(getProject(), file), "foo_twig");

        file = createFile("resources/templates/foo_php.php");
        assertContainsElements(BladeTemplateUtil.resolveTemplateName(getProject(), file), "foo_php");

        file = createFile("custom/foo/namespace_blade.blade.php");
        assertContainsElements(BladeTemplateUtil.resolveTemplateName(getProject(), file), "foo::foo.namespace_blade");

        file = createFile("custom/foo/bar/namespace_blade.blade.php");
        assertContainsElements(BladeTemplateUtil.resolveTemplateName(getProject(), file), "foo::foo.bar.namespace_blade");
    }

    /**
     * @see BladeTemplateUtil#resolveTemplateDirectory
     */
    public void testResolveTemplateDirectory() {
        LaravelSettings.getInstance(getProject()).templatePaths = Collections.singletonList(
            new TemplatePath("custom", "foo", true)
        );

        createFiles(
            "resources/views/foobar/foo_blade.blade.php",
            "custom/foo/bar/namespace_blade.blade.php"
        );

        assertNotNull(BladeTemplateUtil.resolveTemplateDirectory(getProject(), "foobar")
            .stream()
            .filter(
                virtualFile -> virtualFile.isDirectory() && "foobar".equals(virtualFile.getName())
            )
            .findFirst()
            .orElse(null)
        );

        assertNotNull(BladeTemplateUtil.resolveTemplateDirectory(getProject(), "foo::foo")
            .stream()
            .filter(
                virtualFile -> virtualFile.isDirectory() && "foo".equals(virtualFile.getName())
            )
            .findFirst()
            .orElse(null)
        );

        assertNotNull(BladeTemplateUtil.resolveTemplateDirectory(getProject(), "foo::foo.bar")
            .stream()
            .filter(
                virtualFile -> virtualFile.isDirectory() && "bar".equals(virtualFile.getName())
            )
            .findFirst()
            .orElse(null)
        );
    }

    /**
     * @see BladeTemplateUtil#resolveTemplate
     */
    public void testResolveTemplate() {
        createFiles("resources/views/foobar/foo/foo_blade.blade.php");

        assertNotNull(BladeTemplateUtil.resolveTemplate(getProject(), "foobar.foo.foo_blade", 3)
            .stream()
            .filter(virtualFile -> virtualFile.isDirectory() && "foobar".equals(virtualFile.getName()))
            .findFirst()
            .orElseGet(null)
        );

        assertNotNull(BladeTemplateUtil.resolveTemplate(getProject(), "foobar.foo.foo_blade", 9)
            .stream()
            .filter(virtualFile -> virtualFile.isDirectory() && "foo".equals(virtualFile.getName()))
            .findFirst()
            .orElseGet(null)
        );

        assertNotNull(BladeTemplateUtil.resolveTemplate(getProject(), "foobar/foo/foo_blade", 9)
            .stream()
            .filter(virtualFile -> virtualFile.isDirectory() && "foo".equals(virtualFile.getName()))
            .findFirst()
            .orElseGet(null)
        );

        assertNotNull(BladeTemplateUtil.resolveTemplate(getProject(), "foobar.foo.foo_blade", 14)
            .stream()
            .filter(virtualFile -> "foo_blade.blade.php".equals(virtualFile.getName()))
            .findFirst()
            .orElseGet(null)
        );
    }

    /**
     * @see BladeTemplateUtil#normalizeTemplate
     */
    public void testNormalizeTemplate() {
        assertEquals("foo/bar", BladeTemplateUtil.normalizeTemplate("foo\\bar"));
        assertEquals("foo/bar", BladeTemplateUtil.normalizeTemplate("foo\\\\bar"));
    }
}
