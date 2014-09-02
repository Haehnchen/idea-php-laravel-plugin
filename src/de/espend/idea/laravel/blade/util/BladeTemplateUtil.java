package de.espend.idea.laravel.blade.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.blade.psi.BladeDirectiveParameterPsiImpl;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import de.espend.idea.laravel.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BladeTemplateUtil {

    @Nullable
    public static VirtualFile resolveTemplateName(Project project, String templateName) {

        VirtualFile viewsDir = VfsUtil.findRelativeFile(project.getBaseDir(), "app", "views");
        if(viewsDir == null) {
            return null;
        }

        String bladeFilename = templateName.replace(".", "/").concat(".blade.php");

        return VfsUtil.findRelativeFile(viewsDir, bladeFilename.split("/"));

    }

    public static void visitSection(@NotNull final PsiFile psiFile, final SectionVisitor visitor) {
        psiFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if(element instanceof BladeDirectiveParameterPsiImpl) {

                    PsiElement sectionElement = element.getPrevSibling();
                    if(sectionElement.getNode().getElementType() == BladeTokenTypes.SECTION_DIRECTIVE) {
                        for(PsiElement psiElement : PsiElementUtils.getChildrenFix(element)) {
                            if(psiElement.getNode().getElementType() == BladeTokenTypes.DIRECTIVE_PARAMETER_CONTENT) {
                                String content = PsiElementUtils.trimQuote(psiElement.getText());
                                if(content != null && StringUtils.isNotBlank(content)) {
                                    visitor.visit(psiElement, content);
                                }
                            }
                        }
                    }

                }

                super.visitElement(element);
            }
        });
    }

    public static interface ExtendsVisitor {
        public void visit(@NotNull PsiElement psiElement, @NotNull String content);
    }

    public static void visitExtends(final @NotNull PsiFile psiFile, final ExtendsVisitor visitor) {
        psiFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if(element instanceof BladeDirectiveParameterPsiImpl) {

                    PsiElement sectionElement = element.getPrevSibling();
                    if(sectionElement.getNode().getElementType() == BladeTokenTypes.EXTENDS_DIRECTIVE) {
                        for(PsiElement psiElement : PsiElementUtils.getChildrenFix(element)) {
                            if(psiElement.getNode().getElementType() == BladeTokenTypes.DIRECTIVE_PARAMETER_CONTENT) {
                                String content = PsiElementUtils.trimQuote(psiElement.getText());
                                if(content != null && StringUtils.isNotBlank(content)) {
                                    visitor.visit(psiFile, content);
                                }
                            }
                        }
                    }

                }

                super.visitElement(element);
            }
        });
    }

    public static interface SectionVisitor {
        public void visit(@NotNull PsiElement psiElement, @NotNull String templateName);
    }

}
