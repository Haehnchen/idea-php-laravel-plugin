package de.espend.idea.laravel.util;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PsiElementUtils {

    /**
     * getChildren fixed helper
     */
    static public PsiElement[] getChildrenFix(PsiElement psiElement) {
        List<PsiElement> psiElements = new ArrayList<PsiElement>();

        PsiElement startElement = psiElement.getFirstChild();
        if(startElement == null) {
            return new PsiElement[0];
        }

        psiElements.add(startElement);

        for (PsiElement child = psiElement.getFirstChild().getNextSibling(); child != null; child = child.getNextSibling()) {
            psiElements.add(child);
        }

        return psiElements.toArray(new PsiElement[psiElements.size()]);
    }

    @Nullable
    public static String trimQuote(@Nullable String text) {

        if(text == null) return null;

        return text.replaceAll("^\"|\"$|\'|\'$", "");
    }

}
