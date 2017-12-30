package de.espend.idea.laravel.tests.view;

import com.intellij.openapi.vfs.VirtualFile;
import de.espend.idea.laravel.tests.LaravelTempCodeInsightFixtureTestCase;
import de.espend.idea.laravel.view.ViewCollector;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.view.ViewCollector
 */
public class ViewCollectorTempTest extends LaravelTempCodeInsightFixtureTestCase {

    public void testVisitFile() {
        ViewCollector.visitFile(getProject(), new ViewCollector.ViewVisitor() {
            @Override
            public void visit(@NotNull VirtualFile virtualFile, String name) {

            }
        });
    }

}
