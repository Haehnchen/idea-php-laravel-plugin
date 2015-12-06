package de.espend.idea.laravel.blade.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class BladePsiUtil {

    public static boolean isDirectiveWithInstance(@NotNull PsiElement psiElement, @NotNull String clazz, @NotNull String method) {
        PsiElement stringLiteral = psiElement.getParent();
        if(stringLiteral instanceof StringLiteralExpression) {
            PsiElement parameterList = stringLiteral.getParent();
            if(parameterList instanceof ParameterList) {
                PsiElement methodReference = parameterList.getParent();
                if(methodReference instanceof MethodReference && method.equals(((MethodReference) methodReference).getName())) {
                    String text = methodReference.getText();
                    int i = text.indexOf(":");
                    // method resolve dont work; extract class name from text "Foo::method(..."
                    return i > 0 && StringUtils.stripStart(text.substring(0, i), "\\").equals(clazz);
                }
            }
        }

        return false;
    }

    @Nullable
    public static String getSection(PsiElement psiSection) {

        for(PsiElement psiElement : PsiElementUtils.getChildrenFix(psiSection)) {

            if(psiElement.getNode().getElementType() == BladeTokenTypes.DIRECTIVE_PARAMETER_CONTENT) {

                String content = PsiElementUtils.trimQuote(psiElement.getText());
                if(StringUtils.isNotBlank(content)) {
                    return content;
                }

                return null;
            }
        }

        return null;

    }

}
