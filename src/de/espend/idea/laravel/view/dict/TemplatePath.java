package de.espend.idea.laravel.view.dict;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@Tag("templatePath")
public class TemplatePath {

    private String path;
    private boolean customPath = true;

    @Nullable
    private String namespace;

    public TemplatePath() {
    }

    public TemplatePath(@NotNull String path, boolean customPath) {
        this.path = path;
        this.customPath = customPath;
    }

    public TemplatePath(@NotNull String path, @Nullable String namespace, boolean customPath) {
        this(path, customPath);
        this.namespace = namespace;
    }

    @Attribute("path")
    public String getPath() {
        return path;
    }

    @Nullable
    @Attribute("namespace")
    public String getNamespace() {
        return namespace;
    }

    @Override
    public TemplatePath clone() {

        try {
            super.clone();
        } catch (CloneNotSupportedException ignored) {
        }

        return new TemplatePath(this.getPath(), this.getNamespace(), this.isCustomPath());
    }

    public boolean isCustomPath() {
        return customPath;
    }

    @Nullable
    public VirtualFile getRelativePath(Project project) {
        return VfsUtil.findRelativeFile(project.getBaseDir(), this.getPath().split("/"));
    }

    public VirtualFile getDirectory() {

        File file = new File(this.getPath());

        if(!file.exists()) {
            return null;
        }

        return VfsUtil.findFileByIoFile(file, true);
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

}
