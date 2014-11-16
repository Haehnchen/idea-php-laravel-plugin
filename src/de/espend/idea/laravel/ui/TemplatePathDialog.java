package de.espend.idea.laravel.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.table.TableView;
import de.espend.idea.laravel.view.dict.TemplatePath;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;

public class TemplatePathDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private TextFieldWithBrowseButton namespacePath;
    private JTextField name;
    private TableView<TemplatePath> tableView;
    private Project project;
    private TemplatePath twigPath;

    public TemplatePathDialog(Project project, TableView<TemplatePath> tableView, TemplatePath twigPath) {
        this(project, tableView);
        this.name.setText(twigPath.getNamespace());
        this.namespacePath.getTextField().setText(twigPath.getPath());
        //this.namespaceType.getModel().setSelectedItem(twigPath.getNamespaceType().toString());
        this.twigPath = twigPath;
        this.setOkState();
    }

    public TemplatePathDialog(Project project, TableView<TemplatePath> tableView) {

        this.tableView = tableView;
        this.project = project;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        this.setOkState();

        this.namespacePath.getTextField().getDocument().addDocumentListener(new ChangeDocumentListener());
        this.name.getDocument().addDocumentListener(new ChangeDocumentListener());

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        namespacePath.getButton().addMouseListener(createPathButtonMouseListener(namespacePath.getTextField(), FileChooserDescriptorFactory.createSingleFolderDescriptor()));

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void setOkState() {
        TemplatePathDialog.this.buttonOK.setEnabled(
            TemplatePathDialog.this.namespacePath.getText().length() > 0
        );
    }

    private void onOK() {

        TemplatePath twigPath = new TemplatePath(this.namespacePath.getText(), this.name.getText(), true);
        if(this.namespacePath.getText().length() == 0 || this.namespacePath.getText().length() == 0) {
            dispose();
            return;
        }

        // re-add old item to not use public setter wor twigpaths
        // update ?
        if(this.twigPath != null) {
            int row = this.tableView.getSelectedRows()[0];
            this.tableView.getListTableModel().removeRow(row);
            this.tableView.getListTableModel().insertRow(row, twigPath);
            this.tableView.setRowSelectionInterval(row, row);
        } else {
            int row = this.tableView.getRowCount();
            this.tableView.getListTableModel().addRow(twigPath);
            this.tableView.setRowSelectionInterval(row, row);
        }

        dispose();
    }

    private void onCancel() {
        dispose();
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


    private class ChangeDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            setOkState();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            setOkState();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            setOkState();
        }
    }

}
