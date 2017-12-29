package de.espend.idea.laravel.tests.stubs;

import com.jetbrains.php.blade.BladeFileType;
import de.espend.idea.laravel.stub.BladeIncludeStubIndex;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.stub.BladeIncludeStubIndex
 */
public class BladeIncludeStubIndexTest extends LaravelLightCodeInsightFixtureTestCase {
    public void testIndex() {
        myFixture.configureByText(BladeFileType.INSTANCE, "" +
            "@include('foobar.include')\n" +
            "@includeIf( 'foobar.includeIf' , ['some' => 'data'])\n" +
            "@includeWhen($boolean, 'foobar.includeWhen', ['some' => 'data'])\n" +
            "@includeFirst(['foobar.includeFirst', 'foobar.includeFirst2'], ['some' => 'data'])\n" +
            "@includeFirst(   [    'foobar.includeFirst3'    , \"foobar.includeFirst4\"]    )\n"
        );

        assertIndexContains(BladeIncludeStubIndex.KEY , "foobar.include");
        assertIndexContains(BladeIncludeStubIndex.KEY , "foobar.includeIf");
        assertIndexContains(BladeIncludeStubIndex.KEY , "foobar.includeWhen");

        assertIndexContains(BladeIncludeStubIndex.KEY , "foobar.includeFirst");
        assertIndexContains(BladeIncludeStubIndex.KEY , "foobar.includeFirst2");

        assertIndexContains(BladeIncludeStubIndex.KEY , "foobar.includeFirst3");
        assertIndexContains(BladeIncludeStubIndex.KEY , "foobar.includeFirst4");

        assertIndexNotContains(BladeIncludeStubIndex.KEY , "some");
        assertIndexNotContains(BladeIncludeStubIndex.KEY , "data");
    }
}
