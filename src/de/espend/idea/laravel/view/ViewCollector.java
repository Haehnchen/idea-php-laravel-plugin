package de.espend.idea.laravel.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import de.espend.idea.laravel.LaravelSettings;
import org.jetbrains.annotations.NotNull;

public class ViewCollector {

    public static void visitFile(@NotNull Project project, @NotNull final ViewVisitor visitor) {

        final VirtualFile templateDir = VfsUtil.findRelativeFile(LaravelSettings.getInstance(project).getRelativeViewsDirectory(), project.getBaseDir());
        if(templateDir == null) {
            return;
        }

        // collect on project template dir
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

                visitor.visit(virtualFile, filename);

                return true;
            }
        });
    }

    public static interface ViewVisitor {
        public void visit(@NotNull VirtualFile virtualFile, String name);
    }

}
