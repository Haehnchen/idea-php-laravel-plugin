package de.espend.idea.laravel.tests.blade.util;

import de.espend.idea.laravel.blade.util.BladeTemplateUtil;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see de.espend.idea.laravel.blade.util.BladeTemplateUtil
 */
public class BladeTemplateUtilTest extends LaravelLightCodeInsightFixtureTestCase {
    /**
     * @see BladeTemplateUtil#getParameterFromParameterDirective
     */
    public void testGetParameterFromParameterDirective() {
        assertEquals("foobar", BladeTemplateUtil.getParameterFromParameterDirective("'foobar'"));
        assertEquals("foobar", BladeTemplateUtil.getParameterFromParameterDirective("'foobar', []'"));
    }
}
