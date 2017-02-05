package de.espend.idea.laravel.tests.blade.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.psi.BladePsiDirective;
import com.jetbrains.php.blade.psi.BladePsiDirectiveParameter;
import de.espend.idea.laravel.blade.BladePsiElementFactory;
import de.espend.idea.laravel.blade.util.BladePsiUtil;
import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;

import java.io.File;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.blade.util.BladePsiUtil
 */
public class BladePsiUtilTest extends LaravelLightCodeInsightFixtureTestCase {
    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

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

    public void testCollectPrintBlockVariables() {
        PsiFile psiFile = myFixture.configureByFile("component.blade.php");

        assertContainsElements(BladePsiUtil.collectPrintBlockVariables(psiFile), "ti_t-le", "slot");
    }

    public void testFindComponentForSlotScope() {
        myFixture.configureByText(BladeFileType.INSTANCE, "" +
            "@component('layouts.app')\n" +
            "   @slot('ti<caret>tle')\n" +
            "       Home Page\n" +
            "   @endslot\n" +
            "@endcomponent");

        PsiElement psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());

        assertEquals("layouts.app", BladePsiUtil.findComponentForSlotScope(psiElement));
    }

    /**
     * @see BladePsiUtil#getDirectiveParameter
     */
    public void testGetDirectiveParameter() {
        BladePsiDirective bladePsiDirective = BladePsiElementFactory.createFromText(
            getProject(),
            BladePsiDirective.class,
            "@slot('layouts.app', [])\n"
        );

        assertEquals("layouts.app", BladePsiUtil.getDirectiveParameter(bladePsiDirective));
    }

    /**
     * @see BladePsiUtil#getDirectiveParameter
     */
    public void testGetDirectiveParameterWithAdditionalParameter() {
        BladePsiDirective bladePsiDirective = BladePsiElementFactory.createFromText(
            getProject(),
            BladePsiDirective.class,
            "@slot('layouts.app', [])\n"
        );

        assertEquals("layouts.app", BladePsiUtil.getDirectiveParameter(bladePsiDirective));
    }
}
