package de.espend.idea.laravel.tests.dic;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.Method;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see de.espend.idea.laravel.dic.DicTypeProvider
 */
public class DicTypeProviderTest extends LaravelLightCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        myFixture.copyFileToProject("Application.php");
    }

    protected String getTestDataPath() {
        return "src/test/java/de/espend/idea/laravel/tests/dic/fixtures";
    }

    public void testTypeReferences() {
        assertPhpReferenceResolveTo(PhpFileType.INSTANCE, "<?php\n" +
                "app('foo')->b<caret>ar()",
            PlatformPatterns.psiElement(Method.class).withName("bar")
        );

        assertPhpReferenceResolveTo(PhpFileType.INSTANCE, "<?php\n" +
                "\\App::make('foo')->b<caret>ar()",
            PlatformPatterns.psiElement(Method.class).withName("bar")
        );

        assertPhpReferenceResolveTo(PhpFileType.INSTANCE, "<?php\n" +
                "/** @var $f \\Illuminate\\Contracts\\Container\\Container */" +
                "$f->make('foo')->b<caret>ar()",
            PlatformPatterns.psiElement(Method.class).withName("bar")
        );

        assertPhpReferenceResolveTo(PhpFileType.INSTANCE, "<?php\n" +
                "/** @var $f \\Illuminate\\Contracts\\Container\\Container */" +
                "$f->make('Foo\\Bar')->b<caret>ar()",
            PlatformPatterns.psiElement(Method.class).withName("bar")
        );

        assertPhpReferenceResolveTo(PhpFileType.INSTANCE, "<?php\n" +
                "/** @var $f \\Illuminate\\Contracts\\Container\\Container */" +
                "$f->make(\\Foo\\Bar::class)->b<caret>ar()",
            PlatformPatterns.psiElement(Method.class).withName("bar")
        );
    }
}
