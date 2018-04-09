package de.espend.idea.laravel.tests.view;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.VfsTestUtil;
import com.intellij.util.containers.ContainerUtil;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;
import de.espend.idea.laravel.view.ViewCollector;
import de.espend.idea.laravel.view.dict.TemplatePath;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.view.ViewCollector
 */
public class ViewCollectorTest extends LaravelLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();

        VirtualFile virtualFile = myFixture.copyFileToProject("ide-blade.json");
        VfsTestUtil.createDir(virtualFile.getParent(), "res");
    }

    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/laravel/tests/view/fixtures";
    }

    public void testJsonConfigurationPathAreAdded() {
        Collection<TemplatePath> paths = ViewCollector.getPaths(getProject());

        assertNotNull(ContainerUtil.find(paths, templatePath ->
            "src/res".equals(templatePath.getPath()) && templatePath.isCustomPath()
        ));

        assertNotNull(ContainerUtil.find(paths, templatePath ->
            "src".equals(templatePath.getPath()) && templatePath.isCustomPath()
        ));
    }
}
