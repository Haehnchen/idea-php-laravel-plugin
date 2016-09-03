package de.espend.idea.laravel;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import de.espend.idea.laravel.view.dict.TemplatePath;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@State(
    name = "LaravelPluginSettings",
    storages = {
        @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
        @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/laravel-plugin.xml", scheme = StorageScheme.DIRECTORY_BASED)
    }
)
public class LaravelSettings implements PersistentStateComponent<LaravelSettings> {

    public boolean pluginEnabled = false;
    public boolean useAutoPopup = false;
    public String routerNamespace;

    @Nullable
    public List<TemplatePath> templatePaths = new ArrayList<>();

    public static LaravelSettings getInstance(Project project) {
        return ServiceManager.getService(project, LaravelSettings.class);
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
