package de.espend.idea.laravel.stub.processor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.blade.psi.BladeDirectiveElementType;
import com.jetbrains.php.blade.psi.BladeDirectiveParameterPsiImpl;
import com.jetbrains.php.blade.psi.BladePsiDirectiveParameter;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import de.espend.idea.laravel.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class BladeDirectivePsiElementWalkingVisitor extends PsiRecursiveElementWalkingVisitor {

    private final BladeDirectiveElementType directiveElementType;

    private final Map<String, Void> map;

    public BladeDirectivePsiElementWalkingVisitor(BladeDirectiveElementType directiveElementType, Map<String, Void> map) {
        this.directiveElementType = directiveElementType;
        this.map = map;
    }

    @Override
    public void visitElement(PsiElement element) {

        if(element instanceof BladePsiDirectiveParameter) {
            PsiElement sectionElement = element.getPrevSibling();
            if(sectionElement.getNode().getElementType() == this.directiveElementType) {
                for(PsiElement psiElement : PsiElementUtils.getChildrenFix(element)) {
                    if(psiElement.getNode().getElementType() == BladeTokenTypes.DIRECTIVE_PARAMETER_CONTENT) {
                        String content = PsiElementUtils.trimQuote(psiElement.getText());
                        if(StringUtils.isNotBlank(content)) {
                            map.put(content, null);
                        }
                    }
                }
            }
        }

        super.visitElement(element);
    }
}
