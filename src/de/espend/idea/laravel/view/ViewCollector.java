package de.espend.idea.laravel.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import de.espend.idea.laravel.LaravelSettings;
import de.espend.idea.laravel.view.dict.TemplatePath;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ViewCollector {

    public static Collection<TemplatePath> getPaths(@NotNull Project project, boolean includeSettings) {
        String[] defaultDirs = new String[] {"resources/views", "app/views"};
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

                String extension = virtualFile.getExtension();
                if(extension == null || !extension.equalsIgnoreCase("php")) {
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

                if(templatePath.getNamespace() != null) {
                    visitor.visit(virtualFile, templatePath.getNamespace() + "::" + filename);
                } else {
                    visitor.visit(virtualFile, filename);
                }


                return true;
            }
        });

    }

    public static interface ViewVisitor {
        public void visit(@NotNull VirtualFile virtualFile, String name);
    }

}
