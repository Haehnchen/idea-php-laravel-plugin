package de.espend.idea.laravel.tests.stubs;

import com.jetbrains.php.blade.BladeFileType;
import de.espend.idea.laravel.stub.BladeStackStubIndex;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.stub.BladeStackStubIndex
 */
public class BladeStackStubIndexTest extends LaravelLightCodeInsightFixtureTestCase {
    public void testThatStackIsInIndex() {
        myFixture.configureByText(BladeFileType.INSTANCE, "@stack('foobar')");
        assertIndexContains(BladeStackStubIndex.KEY, "foobar");
    }
}
