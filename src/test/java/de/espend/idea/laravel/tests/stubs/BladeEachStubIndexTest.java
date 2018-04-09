package de.espend.idea.laravel.tests.stubs;

import com.jetbrains.php.blade.BladeFileType;
import de.espend.idea.laravel.stub.BladeEachStubIndex;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.stub.BladeEachStubIndex
 */
public class BladeEachStubIndexTest extends LaravelLightCodeInsightFixtureTestCase {
    public void testEachParameterWithEmptyFallbackIsInIndex() {
        myFixture.configureByText(BladeFileType.INSTANCE, "@each('foobar', [], '', 'foobar2')");

        assertIndexContains(BladeEachStubIndex.KEY, "foobar");
        assertIndexContains(BladeEachStubIndex.KEY, "foobar2");
    }

    public void testEachParameterIsInIndex() {
        myFixture.configureByText(BladeFileType.INSTANCE, "@each('my::car.bus_foobar')");

        assertIndexContains(BladeEachStubIndex.KEY, "my::car.bus_foobar");
    }
}
