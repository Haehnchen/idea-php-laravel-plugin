package de.espend.idea.laravel.blade.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import de.espend.idea.laravel.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class BladePsiUtil {

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
