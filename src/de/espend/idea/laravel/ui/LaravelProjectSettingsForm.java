package de.espend.idea.laravel.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import de.espend.idea.laravel.LaravelSettings;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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
    private TextFieldWithBrowseButton textViewsPath;
    private JButton buttonViewsPathReset;
    private JCheckBox useAutoPopopForCompletionCheckBox;
    private JTextField textRouterNamespace;

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

        textViewsPath.getButton().addMouseListener(createPathButtonMouseListener(textViewsPath.getTextField(), FileChooserDescriptorFactory.createSingleFolderDescriptor()));

        return (JComponent) panel1;
    }

    @Override
    public boolean isModified() {
        return !enabled.isSelected() == getSettings().pluginEnabled
            || !useAutoPopopForCompletionCheckBox.isSelected() == getSettings().useAutoPopup
            || !textRouterNamespace.getText().equals(getSettings().routerNamespace)
            ;
    }

    @Override
    public void apply() throws ConfigurationException {
        getSettings().pluginEnabled = enabled.isSelected();
        getSettings().useAutoPopup = useAutoPopopForCompletionCheckBox.isSelected();
        getSettings().routerNamespace = textRouterNamespace.getText();
    }

    @Override
    public void reset() {
        updateUIFromSettings();
    }

    private void updateUIFromSettings() {
        enabled.setSelected(getSettings().pluginEnabled);
        useAutoPopopForCompletionCheckBox.setSelected(getSettings().useAutoPopup);
        textRouterNamespace.setText(getSettings().routerNamespace);
    }

    @Override
    public void disposeUIResources() {

    }

    private LaravelSettings getSettings() {
        return LaravelSettings.getInstance(this.project);
    }

    private MouseListener createPathButtonMouseListener(final JTextField textField, final FileChooserDescriptor fileChooserDescriptor) {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                VirtualFile projectDirectory = project.getBaseDir();
                VirtualFile selectedFile = FileChooser.chooseFile(
                    fileChooserDescriptor,
                    project,
                    VfsUtil.findRelativeFile(textField.getText(), projectDirectory)
                );

                if (null == selectedFile) {
                    return; // Ignore but keep the previous path
                }

                String path = VfsUtil.getRelativePath(selectedFile, projectDirectory, '/');
                if (null == path) {
                    path = selectedFile.getPath();
                }

                textField.setText(path);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        };
    }

    private MouseListener createResetPathButtonMouseListener(final JTextField textField, final String defaultValue) {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                textField.setText(defaultValue);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        };
    }
}
