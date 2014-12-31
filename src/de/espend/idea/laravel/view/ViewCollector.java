package de.espend.idea.laravel.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.laravel.LaravelSettings;
import de.espend.idea.laravel.view.dict.TemplatePath;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ViewCollector {

    public static Collection<TemplatePath> getPaths(@NotNull Project project, boolean includeSettings) {

        // "resources/views" -> laravel 4
        // "app/views" -> laravel 5 (deprecated)
        // "resources/templates" -> laravel 5

        String[] defaultDirs = new String[] {"resources/views", "app/views", "resources/templates"};
        Collection<TemplatePath> templatePaths = new ArrayList<TemplatePath>();

        for(String path: new HashSet<String>(Arrays.asList(defaultDirs))) {
            templatePaths.add(new TemplatePath(path, false));
        }

        if(includeSettings) {
            List<TemplatePath> paths = LaravelSettings.getInstance(project).templatePaths;
            if(paths != null) {
                for(TemplatePath templatePath : paths) {
                    templatePaths.add(templatePath.clone());
                }
            }
        }

        return templatePaths;
    }

    public static void visitFile(@NotNull Project project, @NotNull final ViewVisitor visitor) {
        for(TemplatePath templatePath : getPaths(project, true)) {
            visitTemplatePath(project, templatePath, visitor);
        }
    }

    public static void visitTemplatePath(@NotNull Project project, final @NotNull TemplatePath templatePath, @NotNull final ViewVisitor visitor) {

        final VirtualFile templateDir = VfsUtil.findRelativeFile(templatePath.getPath(), project.getBaseDir());
        if(templateDir == null) {
            return;
        }

        VfsUtil.visitChildrenRecursively(templateDir, new VirtualFileVisitor() {
            @Override
            public boolean visitFile(@NotNull VirtualFile virtualFile) {

                if(virtualFile.isDirectory() || !isTemplateFile(virtualFile)) {
                    return true;
                }

                String filename = VfsUtil.getRelativePath(virtualFile, templateDir, '.');
                if(filename == null) {
                    return true;
                }

                if(filename.endsWith(".php")) {
                    filename = filename.substring(0, filename.length() - 4);
                }

                if(filename.endsWith(".blade")) {
                    filename = filename.substring(0, filename.length() - 6);
                }

                if(filename.endsWith(".html.twig")) {
                    filename = filename.substring(0, filename.length() - ".html.twig".length());
                }

                if(templatePath.getNamespace() != null) {
                    visitor.visit(virtualFile, templatePath.getNamespace() + "::" + filename);
                } else {
                    visitor.visit(virtualFile, filename);
                }

                return true;
            }

            private boolean isTemplateFile(VirtualFile virtualFile) {

                if(virtualFile.getFileType() == BladeFileType.INSTANCE || virtualFile.getFileType() == PhpFileType.INSTANCE) {
                    return true;
                }

                String extension = virtualFile.getExtension();
                if(extension != null && (extension.equalsIgnoreCase("php") || extension.equalsIgnoreCase("twig"))) {
                    return true;
                }

                return false;
            }

        });

    }

    public static interface ViewVisitor {
        public void visit(@NotNull VirtualFile virtualFile, String name);
    }

}
