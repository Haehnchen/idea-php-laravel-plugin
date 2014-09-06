package de.espend.idea.laravel.blade.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.psi.BladeDirectiveParameterPsiImpl;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import de.espend.idea.laravel.LaravelSettings;
import de.espend.idea.laravel.stub.BladeExtendsStubIndex;
import de.espend.idea.laravel.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BladeTemplateUtil {

    @Nullable
    public static VirtualFile resolveTemplateName(Project project, String templateName) {

        VirtualFile viewsDir = VfsUtil.findRelativeFile(project.getBaseDir(), LaravelSettings.getInstance(project).getRelativeViewsDirectory().split("/"));
        if(viewsDir == null) {
            return null;
        }

        String bladeFilename = templateName.replace(".", "/").concat(".blade.php");

        return VfsUtil.findRelativeFile(viewsDir, bladeFilename.split("/"));

    }

    @Nullable
    public static String resolveTemplateName(Project project, VirtualFile virtualFile) {

        VirtualFile viewsDir = VfsUtil.findRelativeFile(project.getBaseDir(), LaravelSettings.getInstance(project).getRelativeViewsDirectory().split("/"));
        if(viewsDir == null) {
            return null;
        }

        String relativePath = VfsUtil.getRelativePath(virtualFile, viewsDir);
        if(relativePath == null) {
            return null;
        }

        if(relativePath.endsWith(".blade.php")) {
            relativePath = relativePath.substring(0, relativePath.length() - ".blade.php".length());
        }

        return relativePath.replace("/", ".");
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

    public static String getFileTemplateName(Project project, VirtualFile virtualFile) {

        String relativeFile = VfsUtil.getRelativePath(virtualFile, project.getBaseDir());
        if(relativeFile == null) {
            return null;
        }

        if(!relativeFile.startsWith("app/views/")) {
            return null;
        }

        String filename = relativeFile.substring("app/views/".length());
        if(filename.endsWith(".php")) {
            filename = filename.substring(0, filename.length() - 4);
        }
        if(filename.endsWith(".blade")) {
            filename = filename.substring(0, filename.length() - 6);
        }

        return filename.replace("/", ".");

    }

    public static Set<VirtualFile> getExtendsImplementations(Project project, String templateName) {
        Set<VirtualFile> virtualFiles = new HashSet<VirtualFile>();
        getExtendsImplementations(project, templateName, virtualFiles, 10);
        return virtualFiles;
    }

    private static void getExtendsImplementations(final Project project, String templateName, final Set<VirtualFile> virtualFiles, int depth) {

        if(depth-- <= 0) {
            return;
        }

        final int finalDepth = depth;
        FileBasedIndexImpl.getInstance().getFilesWithKey(BladeExtendsStubIndex.KEY, new HashSet<String>(Arrays.asList(templateName)), new Processor<VirtualFile>() {
            @Override
            public boolean process(VirtualFile virtualFile) {
                if(!virtualFiles.contains(virtualFile)) {
                    virtualFiles.add(virtualFile);
                    String nextTpl = BladeTemplateUtil.resolveTemplateName(project, virtualFile);
                    if(nextTpl != null) {
                        getExtendsImplementations(project, nextTpl, virtualFiles, finalDepth);
                    }
                }
                return true;
            }
        }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(project), BladeFileType.INSTANCE));
    }
}
