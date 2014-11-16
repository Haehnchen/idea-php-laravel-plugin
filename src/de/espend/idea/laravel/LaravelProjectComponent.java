package de.espend.idea.laravel;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class LaravelProjectComponent implements ProjectComponent {
    @Override
    public void projectOpened() {

    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "LaravelProjectComponent";
    }

    public static boolean isEnabled(Project project) {
        return LaravelSettings.getInstance(project).pluginEnabled;
    }

    public static boolean isEnabled(@Nullable PsiElement psiElement) {
        return psiElement != null && isEnabled(psiElement.getProject());
    }
    public static boolean isEnabledForIndex(@Nullable Project project) {

        if(project == null) {
            return false;
        }

        if(isEnabled(project)) {
            return true;
        }

        VirtualFile baseDir = project.getBaseDir();
        return VfsUtil.findRelativeFile(baseDir, "vendor", "laravel") != null;

    }

}
