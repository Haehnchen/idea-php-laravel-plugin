package de.espend.idea.laravel.ui;

import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import de.espend.idea.laravel.LaravelSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class LaravelProjectSettingsForm implements Configurable {

    private Project project;

    public LaravelProjectSettingsForm(@NotNull final Project project) {
        this.project = project;
    }

    private JCheckBox enabled;
    private JPanel panel1;
    private JCheckBox useAutoPopopForCompletionCheckBox;
    private JTextField textRouterNamespace;
    private JTextField textMainLang;

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
        return panel1;
    }

    @Override
    public boolean isModified() {
        return !enabled.isSelected() == getSettings().pluginEnabled
            || !useAutoPopopForCompletionCheckBox.isSelected() == getSettings().useAutoPopup
            || !textRouterNamespace.getText().equals(getSettings().routerNamespace)
            || !textMainLang.getText().equals(getSettings().mainLanguage)
            ;
    }

    @Override
    public void apply() throws ConfigurationException {
        getSettings().pluginEnabled = enabled.isSelected();
        getSettings().useAutoPopup = useAutoPopopForCompletionCheckBox.isSelected();
        getSettings().routerNamespace = textRouterNamespace.getText();
        getSettings().mainLanguage = textMainLang.getText();
    }

    @Override
    public void reset() {
        updateUIFromSettings();
    }

    private void updateUIFromSettings() {
        enabled.setSelected(getSettings().pluginEnabled);
        useAutoPopopForCompletionCheckBox.setSelected(getSettings().useAutoPopup);
        textRouterNamespace.setText(getSettings().routerNamespace);
        textMainLang.setText(getSettings().getMainLanguage());
    }

    @Override
    public void disposeUIResources() {
    }

    private LaravelSettings getSettings() {
        return LaravelSettings.getInstance(this.project);
    }

    public static void show(@NotNull Project project) {
        ShowSettingsUtilImpl.showSettingsDialog(project, "Laravel.SettingsForm", null);
    }
}
