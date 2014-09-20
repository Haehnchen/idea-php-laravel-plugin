package de.espend.idea.laravel;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

@State(
    name = "LaravelPluginSettings",
    storages = {
        @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
        @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/laravel-plugin.xml", scheme = StorageScheme.DIRECTORY_BASED)
    }
)
public class LaravelSettings implements PersistentStateComponent<LaravelSettings> {

    public static String DEFAULT_VIEWS_DIRECTORY = "app/views";

    public boolean pluginEnabled = false;
    public boolean useAutoPopup = false;
    public String viewsDirectory = DEFAULT_VIEWS_DIRECTORY;

    protected Project project;

    public static LaravelSettings getInstance(Project project) {
        LaravelSettings settings = ServiceManager.getService(project, LaravelSettings.class);
        settings.project = project;
        return settings;
    }

    public String getRelativeViewsDirectory() {
        if(StringUtils.isBlank(viewsDirectory)) {
            return DEFAULT_VIEWS_DIRECTORY;
        }

        return viewsDirectory;
    }

    @Nullable
    @Override
    public LaravelSettings getState() {
        return this;
    }

    @Override
    public void loadState(LaravelSettings settings) {
        XmlSerializerUtil.copyBean(settings, this);
    }
}
