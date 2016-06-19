package de.espend.idea.laravel.tests.blade;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class TemplateLineMarkerTest extends LaravelLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));
    }

    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testFoo() {

        PhpClass phpPsiFromText = PhpPsiElementFactory.createPhpPsiFromText(getProject(), PhpClass.class, "<?php\n" +
            "class FooController extends \\Illuminate\\Routing\\Controller {\n" +
            "   public function foo()\n" +
            "   {\n" +
            "       view('foo.bar');" +
            "   }\n" +
            "}"
        );

        assertLineMarker(phpPsiFromText, new LineMarker.Assert() {
            @Override
            public boolean match(@NotNull LineMarkerInfo markerInfo) {
                return false;
            }
        });
    }
}
