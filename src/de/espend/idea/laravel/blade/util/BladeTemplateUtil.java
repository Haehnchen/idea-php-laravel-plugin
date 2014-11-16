package de.espend.idea.laravel.blade.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.psi.BladeDirectiveElementType;
import com.jetbrains.php.blade.psi.BladeDirectiveParameterPsiImpl;
import com.jetbrains.php.blade.psi.BladePsiDirectiveParameter;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import de.espend.idea.laravel.LaravelSettings;
import de.espend.idea.laravel.blade.dict.DirectiveParameterVisitorParameter;
import de.espend.idea.laravel.stub.BladeExtendsStubIndex;
import de.espend.idea.laravel.util.PsiElementUtils;
import de.espend.idea.laravel.view.ViewCollector;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
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

    public static void visitSection(@NotNull final PsiFile psiFile, final DirectiveParameterVisitor visitor) {
        psiFile.acceptChildren(new DirectivePsiRecursiveElementWalkingVisitor(BladeTokenTypes.SECTION_DIRECTIVE, visitor));
    }

    public static void visitExtends(final @NotNull PsiFile psiFile, final DirectiveParameterVisitor visitor) {
        psiFile.acceptChildren(new DirectivePsiRecursiveElementWalkingVisitor(BladeTokenTypes.EXTENDS_DIRECTIVE, visitor));
    }

    public static void visitYield(@NotNull final PsiFile psiFile, DirectiveParameterVisitor visitor) {
        psiFile.acceptChildren(new DirectivePsiRecursiveElementWalkingVisitor(BladeTokenTypes.YIELD_DIRECTIVE, visitor));
    }

    public static void visitSectionOrYield(@NotNull final PsiFile psiFile, final DirectiveParameterVisitor visitor) {
        psiFile.acceptChildren(new DirectivePsiRecursiveElementWalkingVisitor(visitor, BladeTokenTypes.SECTION_DIRECTIVE, BladeTokenTypes.YIELD_DIRECTIVE));
    }

    public static Set<String> getFileTemplateName(Project project, final VirtualFile currentVirtualFile) {

        final Set<String> strings = new HashSet<String>();

        ViewCollector.visitFile(project, new ViewCollector.ViewVisitor() {
            @Override
            public void visit(@NotNull VirtualFile virtualFile, String name) {
                if(virtualFile.equals(currentVirtualFile)) {
                    strings.add(name);
                }
            }
        });

        return strings;

    }

    public static Set<VirtualFile> getExtendsImplementations(Project project, String templateName) {
        Set<VirtualFile> virtualFiles = new HashSet<VirtualFile>();
        getExtendsImplementations(project, templateName, virtualFiles, 10);
        return virtualFiles;
    }

    public static Set<VirtualFile> getExtendsImplementations(Project project, Collection<String> templateNames) {
        Set<VirtualFile> virtualFiles = new HashSet<VirtualFile>();
        for(String templateName : templateNames) {
            getExtendsImplementations(project, templateName, virtualFiles, 10);
        }

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

    public static void visitUpPathSections(final PsiFile psiFile, int depth, final DirectiveParameterVisitor visitor) {

        // simple secure recursive calls
        if(depth-- <= 0) {
            return;
        }

        final int finalDepth = depth;
        BladeTemplateUtil.visitExtends(psiFile, new DirectiveParameterVisitor() {
            @Override
            public void visit(@NotNull DirectiveParameterVisitorParameter parameter) {
                VirtualFile virtualFile = BladeTemplateUtil.resolveTemplateName(psiFile.getProject(), parameter.getContent());
                if (virtualFile != null) {
                    PsiFile templatePsiFile = PsiManager.getInstance(psiFile.getProject()).findFile(virtualFile);
                    if (templatePsiFile != null) {
                        BladeTemplateUtil.visitSectionOrYield(templatePsiFile, visitor);
                        visitUpPathSections(templatePsiFile, finalDepth, visitor);
                    }
                }
            }
        });

    }


    private static class DirectivePsiRecursiveElementWalkingVisitor extends PsiRecursiveElementWalkingVisitor {

        private final BladeDirectiveElementType[] elementTypes;
        private final DirectiveParameterVisitor visitor;

        public DirectivePsiRecursiveElementWalkingVisitor(BladeDirectiveElementType elementType, DirectiveParameterVisitor visitor) {
            this.elementTypes = new BladeDirectiveElementType[] {elementType} ;
            this.visitor = visitor;
        }

        public DirectivePsiRecursiveElementWalkingVisitor(DirectiveParameterVisitor visitor, BladeDirectiveElementType... elementTypes) {
            this.elementTypes = elementTypes;
            this.visitor = visitor;
        }

        @Override
        public void visitElement(PsiElement element) {
            if(element instanceof BladePsiDirectiveParameter) {
                PsiElement sectionElement = element.getPrevSibling();
                if(this.isValidElementType(sectionElement.getNode().getElementType())) {
                    for(PsiElement psiElement : PsiElementUtils.getChildrenFix(element)) {
                        if(psiElement.getNode().getElementType() == BladeTokenTypes.DIRECTIVE_PARAMETER_CONTENT) {
                            String content = PsiElementUtils.trimQuote(psiElement.getText());
                            if(content != null && StringUtils.isNotBlank(content)) {
                                visitor.visit(new DirectiveParameterVisitorParameter(psiElement, content, sectionElement.getNode().getElementType()));
                            }
                        }
                    }
                }
            }
            super.visitElement(element);
        }

        private boolean isValidElementType(IElementType iElementType) {

            for(BladeDirectiveElementType elementType: this.elementTypes) {
                if(iElementType == elementType) {
                    return true;
                }
            }

            return false;
        }

    }

    public static interface DirectiveParameterVisitor {
        public void visit(@NotNull DirectiveParameterVisitorParameter parameter);
    }

}
