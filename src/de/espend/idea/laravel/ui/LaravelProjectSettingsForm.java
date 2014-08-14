package de.espend.idea.laravel.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import de.espend.idea.laravel.LaravelSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LaravelProjectSettingsForm implements Configurable {

    private Project project;

    public LaravelProjectSettingsForm(@NotNull final Project project) {
        this.project = project;
    }

    private JCheckBox enabled;
    private JPanel panel1;

    @Nls
    @Override
    public String getDisplayName() {
        return "Laravel Plugin";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return (JComponent) panel1;
    }

    @Override
    public boolean isModified() {
        return !enabled.isSelected() == getSettings().pluginEnabled;
    }

    @Override
    public void apply() throws ConfigurationException {
        getSettings().pluginEnabled = enabled.isSelected();
    }

    @Override
    public void reset() {
        updateUIFromSettings();
    }

    private void updateUIFromSettings() {
        enabled.setSelected(getSettings().pluginEnabled);
    }

    @Override
    public void disposeUIResources() {

    }

    private LaravelSettings getSettings() {
        return LaravelSettings.getInstance(this.project);
    }
}
