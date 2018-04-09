package de.espend.idea.laravel;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import de.espend.idea.laravel.util.IdeHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class LaravelProjectComponent implements ProjectComponent {
    private Project project;

    public LaravelProjectComponent(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        notifyPluginEnableDialog();
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

    private void notifyPluginEnableDialog() {
        // Enable Project dialog
        if(!isEnabled(this.project) && !LaravelSettings.getInstance(this.project).dismissEnableNotification) {
            if(VfsUtil.findRelativeFile(this.project.getBaseDir(), "app") != null
                && VfsUtil.findRelativeFile(this.project.getBaseDir(), "vendor", "laravel") != null
                ) {
                IdeHelper.notifyEnableMessage(project);
            }
        }
    }
}
