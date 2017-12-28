package de.espend.idea.laravel.ui;

import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorTextField;
import de.espend.idea.laravel.blade.actions.NewViewNameCompletionProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ExtractPartialViewDialog extends DialogWrapper {

    private EditorTextField viewNameEditor;

    private JPanel panel;

    // View name should be "header", or "partials.header", or "sh-ar_ed.partials.header",
    // so, words with '_' and '-' separated with dots
    private static Pattern viewNamePattern = Pattern.compile("^[\\w\\d\\-_]+([\\.\\/][\\w\\d\\-_]+)*$");

    public ExtractPartialViewDialog(@NotNull Project project, VirtualFile targetDirectory) {
        super(project);

        panel = new JPanel(new BorderLayout());

        panel.add(new JLabel("View name (example: partials.header)"), BorderLayout.NORTH);

        viewNameEditor = new EditorTextField("", project, FileTypes.PLAIN_TEXT);
        new NewViewNameCompletionProvider(targetDirectory).apply(viewNameEditor);
        panel.add(viewNameEditor.getComponent(), BorderLayout.CENTER);

        setTitle("Extract Partial View");

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }

    public String getViewName() {
        return viewNameEditor.getText();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return viewNameEditor.getComponent();
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "Laravel.ExtractPartialViewDialog";
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {

        if(!viewNamePattern.matcher(viewNameEditor.getText()).find()) {
            return new ValidationInfo("Wrong view name", viewNameEditor.getComponent());
        }

        return null;
    }
}
