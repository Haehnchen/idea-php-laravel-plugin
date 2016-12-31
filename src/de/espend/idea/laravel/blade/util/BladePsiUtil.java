package de.espend.idea.laravel.blade.util;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.blade.psi.BladePsiDirectiveParameter;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static boolean isDirective(@NotNull PsiElement psiElement, @NotNull IElementType elementType) {
        PsiLanguageInjectionHost host = InjectedLanguageManager.getInstance(psiElement.getProject()).getInjectionHost(psiElement);

        return host instanceof BladePsiDirectiveParameter &&
            ((BladePsiDirectiveParameter) host).getDirectiveElementType() == elementType;
    }

    /**
     * Extract template attribute of @each directory eg index 0 and 3
     */
    @NotNull
    public static List<String> getEachDirectiveTemplateParameter(@NotNull BladePsiDirectiveParameter parameter) {
        String text = parameter.getText();
        if(StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }

        List<String> parameters = extractParameters(text);

        // first and fourth item is our template attribute
        List<String> templateVariables = new ArrayList<>();
        for (Integer integer : Arrays.asList(0, 3)) {
            if(parameters.size() < integer + 1) {
                return templateVariables;
            }

            String content = PsiElementUtils.trimQuote(parameters.get(integer));
            if(StringUtils.isBlank(content)) {
                continue;
            }

            templateVariables.add(content);
        }

        return templateVariables;
    }

    /**
     * Extract parameter from directive parameters
     *
     * @param text "foo", "foobar"
     */
    @NotNull
    private static List<String> extractParameters(@NotNull String text) {
        String content = StringUtils.stripStart(StringUtils.stripEnd(text, ")"), "(");

        Matcher matcher = Pattern.compile("([^,]+\\(.+?\\))|([^,]+)").matcher(content);
        List<String> parameters = new ArrayList<>();

        while(matcher.find()) {
            parameters.add(StringUtil.trim(matcher.group(0)));
        }

        return parameters;
    }
}
