package de.espend.idea.laravel.tests.blade.util;

import com.jetbrains.php.blade.psi.BladePsiDirectiveParameter;
import de.espend.idea.laravel.blade.BladePsiElementFactory;
import de.espend.idea.laravel.blade.util.BladePsiUtil;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.blade.util.BladePsiUtil
 */
public class BladePsiUtilTest extends LaravelLightCodeInsightFixtureTestCase {
    public void testGetEachDirectiveTemplateParameter() {
        BladePsiDirectiveParameter parameter = BladePsiElementFactory.createFromText(getProject(), BladePsiDirectiveParameter.class, "@each('auth.password', [], '', 'auth.login')");

        List<String> templates = BladePsiUtil.getEachDirectiveTemplateParameter(parameter);

        assertEquals("auth.password", templates.get(0));
        assertEquals("auth.login", templates.get(1));
    }

    public void testGetEachDirectiveTemplateParameterWithSingleItem() {
        BladePsiDirectiveParameter parameter = BladePsiElementFactory.createFromText(getProject(), BladePsiDirectiveParameter.class, "@each('auth.password')");

        List<String> templates = BladePsiUtil.getEachDirectiveTemplateParameter(parameter);

        assertEquals("auth.password", templates.get(0));
    }
}
