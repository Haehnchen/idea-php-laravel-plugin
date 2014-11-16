package de.espend.idea.laravel.view.dict;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.Nullable;

@Tag("template_path")
public class TemplatePath {

    private final String path;

    @Nullable
    private String namespace;

    public TemplatePath(String path) {
        this.path = path;
    }

    public TemplatePath(String path, @Nullable String namespace) {
        this(path);
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

}
