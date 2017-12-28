package de.espend.idea.laravel.tests.view;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.view.ViewReferences
 */
public class ViewReferencesTest extends LaravelLightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();

        myFixture.copyFileToProject("classes.php");
    }

    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testBladeIncludeIf() {
        assertCompletionContains(BladeFileType.INSTANCE, "@includeIf('<caret>')", "test_view");
        assertNavigationMatch(BladeFileType.INSTANCE, "@includeIf('test<caret>_view')", PlatformPatterns.psiElement());
    }

    public void testBladeIncludeWhen() {
        assertCompletionContains(BladeFileType.INSTANCE, "@includeWhen(true, '<caret>')", "test_view");
        assertNavigationMatch(BladeFileType.INSTANCE, "@includeWhen(true, 'test<caret>_view')", PlatformPatterns.psiElement());
    }

    public void testBladeIncludeFirst() {
        assertCompletionContains(BladeFileType.INSTANCE, "@includeFirst(['', '<caret>'])", "test_view");
        assertNavigationMatch(BladeFileType.INSTANCE, "@includeFirst(['', 'test<caret>_view'])", PlatformPatterns.psiElement());
    }

    public void testBladeComponent() {
        assertCompletionContains(BladeFileType.INSTANCE, "@component('<caret>')", "test_view");
        assertNavigationMatch(BladeFileType.INSTANCE, "@component('test<caret>_view')", PlatformPatterns.psiElement());
    }

    public void testBladeEach() {
        assertCompletionContains(BladeFileType.INSTANCE, "@each('<caret>')", "test_view");
        assertNavigationMatch(BladeFileType.INSTANCE, "@each('test<caret>_view')", PlatformPatterns.psiElement());

        assertCompletionContains(BladeFileType.INSTANCE, "@each('', '', '', '<caret>')", "test_view");
        assertNavigationMatch(BladeFileType.INSTANCE, "@each('', '', '', 'test<caret>_view')", PlatformPatterns.psiElement());
    }

    public void testPhpViewFunction() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php view('<caret>')", "test_view");
        assertNavigationMatch(PhpFileType.INSTANCE, "<?php view('test<caret>_view')", PlatformPatterns.psiElement());
    }

    public void testPhpViewFactory() {
        assertCompletionContains(
            PhpFileType.INSTANCE,
            "<?php /** @var $x \\Illuminate\\View\\Factory */\n $x->make('<caret>')')",
            "test_view"
        );

        assertNavigationMatch(            PhpFileType.INSTANCE,
            "<?php /** @var $x \\Illuminate\\View\\Factory */\n $x->make('test<caret>_view')')",
            PlatformPatterns.psiElement()
        );
    }
}
