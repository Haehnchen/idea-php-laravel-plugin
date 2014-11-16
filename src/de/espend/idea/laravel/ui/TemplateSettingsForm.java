package de.espend.idea.laravel.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.AnActionButtonUpdater;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ElementProducer;
import com.intellij.util.ui.ListTableModel;
import de.espend.idea.laravel.LaravelSettings;
import de.espend.idea.laravel.view.ViewCollector;
import de.espend.idea.laravel.view.dict.TemplatePath;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class TemplateSettingsForm implements Configurable {

    private JPanel panel1;
    private JPanel panelTableView;
    private JButton resetToDefault;
    private TableView<TemplatePath> tableView;
    private Project project;
    private boolean changed = false;
    private ListTableModel<TemplatePath> modelList;

    public TemplateSettingsForm(@NotNull Project project) {

        this.project = project;

        this.tableView = new TableView<TemplatePath>();
        this.modelList = new ListTableModel<TemplatePath>(
            new NamespaceColumn(),
            new PathColumn(project),
            new CustomColumn()
        );

        this.attachItems(true);

        this.tableView.setModelAndUpdateColumns(this.modelList);

        this.modelList.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                TemplateSettingsForm.this.changed = true;
            }
        });

        resetToDefault.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                TemplateSettingsForm.this.resetList();

                List<TemplatePath> sortableLookupItems = new ArrayList<TemplatePath>();
                sortableLookupItems.addAll(ViewCollector.getPaths(TemplateSettingsForm.this.project, false));
                //Collections.sort(sortableLookupItems);
            }
        });
    }

    private void attachItems(boolean includeSettings) {

        // dont load on project less context
        if(this.project == null) {
            return;
        }
        
        List<TemplatePath> sortableLookupItems = new ArrayList<TemplatePath>();
        sortableLookupItems.addAll(ViewCollector.getPaths(this.project, includeSettings));
        //Collections.sort(sortableLookupItems);

        for (TemplatePath twigPath : sortableLookupItems) {
            // dont use managed class here
            this.modelList.addRow(twigPath.clone());
        }
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Twig";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        ToolbarDecorator tablePanel = ToolbarDecorator.createDecorator(this.tableView, new ElementProducer<TemplatePath>() {
            @Override
            public TemplatePath createElement() {
                //IdeFocusManager.getInstance(TwigSettingsForm.this.project).requestFocus(TwigNamespaceDialog.getWindows(), true);
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean canCreateElement() {
                return true;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        tablePanel.setEditAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                TemplateSettingsForm.this.openTwigPathDialog(TemplateSettingsForm.this.tableView.getSelectedObject());
            }
        });


        tablePanel.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
               TemplateSettingsForm.this.openTwigPathDialog(null);
            }
        });

        tablePanel.setEditActionUpdater(new AnActionButtonUpdater() {
            @Override
            public boolean isEnabled(AnActionEvent e) {
                TemplatePath twigPath = TemplateSettingsForm.this.tableView.getSelectedObject();
                return twigPath != null && twigPath.isCustomPath();
            }
        });

        tablePanel.setRemoveActionUpdater(new AnActionButtonUpdater() {
            @Override
            public boolean isEnabled(AnActionEvent e) {
                TemplatePath twigPath = TemplateSettingsForm.this.tableView.getSelectedObject();
                return twigPath != null && twigPath.isCustomPath();
            }
        });

        tablePanel.disableUpAction();
        tablePanel.disableDownAction();

        this.panelTableView.add(tablePanel.createPanel());

        return this.panel1;
    }

    @Override
    public boolean isModified() {
        return this.changed;
    }

    @Override
    public void apply() throws ConfigurationException {
        List<TemplatePath> twigPaths = new ArrayList<TemplatePath>();

        for(TemplatePath twigPath :this.tableView.getListTableModel().getItems()) {
            // only custom and disabled path need to save
            if((twigPath.getRelativePath(this.project) != null) || twigPath.isCustomPath()) {
                twigPaths.add(twigPath.clone());
            }
        }

        getSettings().templatePaths = twigPaths;
        this.changed = false;
    }

    private LaravelSettings getSettings() {
        return LaravelSettings.getInstance(this.project);
    }

    private void resetList() {
        // clear list, easier?
        while(this.modelList.getRowCount() > 0) {
            this.modelList.removeRow(0);
        }

    }

    @Override
    public void reset() {
        this.resetList();
        this.attachItems(true);
        this.changed = false;
    }

    @Override
    public void disposeUIResources() {
        this.resetList();
    }

    private class NamespaceColumn extends ColumnInfo<TemplatePath, String> {

        public NamespaceColumn() {
            super("Namespace");
        }

        @Nullable
        @Override
        public String valueOf(TemplatePath twigPath) {
             return twigPath.getNamespace();
        }
    }

    private class PathColumn extends ColumnInfo<TemplatePath, String> {

        private Project project;

        public PathColumn(Project project) {
            super("Path");
            this.project = project;
        }

        @Nullable
        @Override
        public String valueOf(TemplatePath twigPath) {
            return twigPath.getPath();
        }
    }

    private class CustomColumn extends ColumnInfo<TemplatePath, String> {

        public CustomColumn() {
            super("Parser");
        }

        @Nullable
        @Override
        public String valueOf(TemplatePath twigPath) {
            return twigPath.isCustomPath() ? "Custom" : "Internal";
        }
    }

    private void openTwigPathDialog(@Nullable TemplatePath twigPath) {
        TemplatePathDialog templatePathDialog;
        if(twigPath == null) {
            templatePathDialog = new TemplatePathDialog(project, this.tableView);
        } else {
            templatePathDialog = new TemplatePathDialog(project, this.tableView, twigPath);
        }

        Dimension dim = new Dimension();
        dim.setSize(500, 190);
        templatePathDialog.setTitle("Twig Namespace");
        templatePathDialog.setMinimumSize(dim);
        templatePathDialog.pack();
        templatePathDialog.setLocationRelativeTo(TemplateSettingsForm.this.panel1);

        templatePathDialog.setVisible(true);
    }

}
