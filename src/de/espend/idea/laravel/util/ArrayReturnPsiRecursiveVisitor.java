package de.espend.idea.laravel.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.config.AppConfigReferences;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayReturnPsiRecursiveVisitor extends PsiRecursiveElementWalkingVisitor {

    private final String fileNameWithoutExtension;
    private final AppConfigReferences.ConfigVisitor configVisitor;

    public ArrayReturnPsiRecursiveVisitor(String fileNameWithoutExtension, AppConfigReferences.ConfigVisitor configVisitor) {
        this.fileNameWithoutExtension = fileNameWithoutExtension;
        this.configVisitor = configVisitor;
    }

    @Override
    public void visitElement(PsiElement element) {

        if(element instanceof PhpReturn) {
            visitPhpReturn((PhpReturn) element);
        }

        super.visitElement(element);
    }

    public void visitPhpReturn(PhpReturn phpReturn) {
        PsiElement arrayCreation = phpReturn.getFirstPsiChild();
        if(arrayCreation instanceof ArrayCreationExpression) {
            collectConfigKeys((ArrayCreationExpression) arrayCreation, this.configVisitor, fileNameWithoutExtension);
        }
    }


    public static void collectConfigKeys(ArrayCreationExpression creationExpression, AppConfigReferences.ConfigVisitor configVisitor, String configName) {
        collectConfigKeys(creationExpression, configVisitor, Arrays.asList(configName));
    }

    public static void collectConfigKeys(ArrayCreationExpression creationExpression, AppConfigReferences.ConfigVisitor configVisitor, List<String> context) {

        for(ArrayHashElement hashElement: PsiTreeUtil.getChildrenOfTypeAsList(creationExpression, ArrayHashElement.class)) {

            PsiElement arrayKey = hashElement.getKey();
            PsiElement arrayValue = hashElement.getValue();

            if(arrayKey instanceof StringLiteralExpression) {

                List<String> myContext = new ArrayList<String>(context);
                myContext.add(((StringLiteralExpression) arrayKey).getContents());
                String keyName = StringUtils.join(myContext, ".");

                if(arrayValue instanceof ArrayCreationExpression) {
                    configVisitor.visitConfig(keyName, arrayKey, true);
                    collectConfigKeys((ArrayCreationExpression) arrayValue, configVisitor, myContext);
                } else {
                    configVisitor.visitConfig(keyName, arrayKey, false);
                }

            }
        }

    }
}
