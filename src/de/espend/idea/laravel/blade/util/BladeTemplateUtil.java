package de.espend.idea.laravel.blade.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.psi.BladeDirectiveElementType;
import com.jetbrains.php.blade.psi.BladePsiDirectiveParameter;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.blade.dict.DirectiveParameterVisitorParameter;
import de.espend.idea.laravel.stub.BladeExtendsStubIndex;
import de.espend.idea.laravel.util.PsiElementUtils;
import de.espend.idea.laravel.view.ViewCollector;
import de.espend.idea.laravel.view.dict.TemplatePath;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class BladeTemplateUtil {
    public static Set<String> RENDER_METHODS = new HashSet<String>() {{
        add("make");
        add("of");
    }};

    @NotNull
    public static Set<VirtualFile> resolveTemplateName(Project project, String templateName) {

        Set<String> templateNames = new HashSet<>();

        int i = templateName.indexOf("::");
        String ns = null;
        if(i > 0) {
            ns = templateName.substring(0, i);
            templateName = templateName.substring(i + 2, templateName.length());
        }

        String pointName = templateName.replace(".", "/");
        templateNames.add(pointName.concat(".blade.php"));
        templateNames.add(pointName.concat(".html.twig"));
        templateNames.add(pointName.concat(".php"));

        Set<VirtualFile> templateFiles = new HashSet<>();
        for(TemplatePath templatePath : ViewCollector.getPaths(project, true)) {

            if(ns != null && !ns.equals(templatePath.getNamespace())) {
                continue;
            }

            VirtualFile viewDir = templatePath.getRelativePath(project);
            if(viewDir == null) {
                continue;
            }

            for(String templateRelative: templateNames) {
                VirtualFile viewsDir = VfsUtil.findRelativeFile(templateRelative, viewDir);
                if(viewsDir != null) {
                    templateFiles.add(viewsDir);
                }
            }

        }

        return templateFiles;
    }


    @NotNull
    public static Set<String> resolveTemplateName(Project project, VirtualFile virtualFile) {

        Set<String> templateNames = new HashSet<>();
        for(TemplatePath templatePath : ViewCollector.getPaths(project, true)) {

            VirtualFile viewDir = templatePath.getRelativePath(project);
            if(viewDir == null) {
                continue;
            }

            String relativePath = VfsUtil.getRelativePath(virtualFile, viewDir);
            if(relativePath != null) {
                if(relativePath.endsWith(".blade.php")) {
                    relativePath = relativePath.substring(0, relativePath.length() - ".blade.php".length());
                }

                if(relativePath.endsWith(".twig.html")) {
                    relativePath = relativePath.substring(0, relativePath.length() - ".twig.html".length());
                }

                if(templatePath.getNamespace() != null && StringUtils.isNotBlank(templatePath.getNamespace())) {
                    templateNames.add(templatePath.getNamespace() + "::" + relativePath.replace("/", "."));
                } else {
                    templateNames.add(relativePath.replace("/", "."));
                }

            }

        }

        return templateNames;
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

    public static void visit(@NotNull final PsiFile psiFile, @NotNull BladeDirectiveElementType elementType, DirectiveParameterVisitor visitor) {
        psiFile.acceptChildren(new DirectivePsiRecursiveElementWalkingVisitor(elementType, visitor));
    }

    public static void visitSectionOrYield(@NotNull final PsiFile psiFile, final DirectiveParameterVisitor visitor, @NotNull BladeDirectiveElementType... elementTypes) {
        psiFile.acceptChildren(new DirectivePsiRecursiveElementWalkingVisitor(visitor, elementTypes));
    }

    public static Set<String> getFileTemplateName(Project project, final VirtualFile currentVirtualFile) {
        final Set<String> strings = new HashSet<>();

        ViewCollector.visitFile(project, (virtualFile, name) -> {
            if(virtualFile.equals(currentVirtualFile)) {
                strings.add(name);
            }
        });

        return strings;

    }

    public static Set<VirtualFile> getExtendsImplementations(Project project, String templateName) {
        Set<VirtualFile> virtualFiles = new HashSet<>();
        getExtendsImplementations(project, templateName, virtualFiles, 10);
        return virtualFiles;
    }

    public static Set<VirtualFile> getExtendsImplementations(Project project, Collection<String> templateNames) {
        Set<VirtualFile> virtualFiles = new HashSet<>();
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
        FileBasedIndexImpl.getInstance().getFilesWithKey(BladeExtendsStubIndex.KEY, new HashSet<>(Collections.singletonList(templateName)), virtualFile -> {
            if (!virtualFiles.contains(virtualFile)) {
                virtualFiles.add(virtualFile);
                Set<String> nextTpls = BladeTemplateUtil.resolveTemplateName(project, virtualFile);
                for (String nextTpl : nextTpls) {
                    getExtendsImplementations(project, nextTpl, virtualFiles, finalDepth);
                }
            }
            return true;
        }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(project), BladeFileType.INSTANCE));
    }

    public static void visitUpPath(final PsiFile psiFile, int depth, final DirectiveParameterVisitor visitor, @NotNull BladeDirectiveElementType... elementTypes) {
        // simple secure recursive calls
        if(depth-- <= 0) {
            return;
        }

        final int finalDepth = depth;
        BladeTemplateUtil.visitExtends(psiFile, parameter -> {
            Set<VirtualFile> virtualFiles = BladeTemplateUtil.resolveTemplateName(psiFile.getProject(), parameter.getContent());
            for(VirtualFile virtualFile : virtualFiles) {
                PsiFile templatePsiFile = PsiManager.getInstance(psiFile.getProject()).findFile(virtualFile);
                if (templatePsiFile != null) {
                    BladeTemplateUtil.visitSectionOrYield(templatePsiFile, visitor, elementTypes);
                    visitUpPath(templatePsiFile, finalDepth, visitor, elementTypes);
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

    public interface DirectiveParameterVisitor {
        void visit(@NotNull DirectiveParameterVisitorParameter parameter);
    }

    public static Collection<Pair<String, PsiElement>> getViewTemplatesPairScope(@NotNull PsiElement psiElement) {
        Collection<Pair<String, PsiElement>> views = new ArrayList<>();

        psiElement.accept(new MyViewRecursiveElementWalkingVisitor(views));

        return views;
    }

    public static Collection<String> getViewTemplatesScope(@NotNull PsiElement psiElement) {
        return getViewTemplatesPairScope(psiElement)
                .stream().map(view -> view.getFirst())
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static class MyViewRecursiveElementWalkingVisitor extends PsiRecursiveElementWalkingVisitor {
        private final Collection<Pair<String, PsiElement>> views;

        private MyViewRecursiveElementWalkingVisitor(Collection<Pair<String, PsiElement>> views) {
            this.views = views;
        }

        @Override
        public void visitElement(PsiElement element) {

            if(element instanceof MethodReference) {
                visitMethodReference((MethodReference) element);
            }

            if(element instanceof FunctionReference) {
                visitFunctionReference((FunctionReference) element);
            }

            super.visitElement(element);
        }

        private void visitFunctionReference(FunctionReference functionReference) {

            if(!"view".equals(functionReference.getName())) {
                return;
            }

            PsiElement[] parameters = functionReference.getParameters();

            if(parameters.length < 1 || !(parameters[0] instanceof StringLiteralExpression)) {
                return;
            }

            String contents = ((StringLiteralExpression) parameters[0]).getContents();
            if(StringUtils.isBlank(contents)) {
                return;
            }

            views.add(Pair.create(contents, parameters[0]));
        }

        private void visitMethodReference(MethodReference methodReference) {

            String methodName = methodReference.getName();
            if(!RENDER_METHODS.contains(methodName)) {
                return;
            }

            PsiElement classReference = methodReference.getFirstChild();
            if(!(classReference instanceof ClassReference)) {
                return;
            }

            if(!"View".equals(((ClassReference) classReference).getName())) {
                return;
            }

            PsiElement[] parameters = methodReference.getParameters();
            if(parameters.length == 0 || !(parameters[0] instanceof StringLiteralExpression)) {
                return;
            }

            String contents = ((StringLiteralExpression) parameters[0]).getContents();
            if(StringUtils.isBlank(contents)) {
                return;
            }

            views.add(Pair.create(contents, parameters[0]));
        }
    }
}
