package de.espend.idea.laravel.blade.actions;

import com.intellij.codeInsight.actions.ReformatCodeAction;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.formatting.FormatterImpl;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.refactoring.RefactoringActionHandler;
import com.jetbrains.php.blade.formatter.BladeFormattingModelBuilder;
import de.espend.idea.laravel.LaravelSettings;
import de.espend.idea.laravel.ui.ExtractPartialViewDialog;
import de.espend.idea.laravel.view.ViewCollector;
import de.espend.idea.laravel.view.dict.TemplatePath;
import com.jetbrains.php.blade.BladeLanguage;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExtractPartialViewHandler implements RefactoringActionHandler {

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile, DataContext dataContext) {

        if(!LaravelSettings.getInstance(project).pluginEnabled){
            return;
        }

        final String selectedText = editor.getSelectionModel().getSelectedText();

        if(StringUtils.isBlank(selectedText)) {
            return;
        }

        PsiDirectory targetDirectory = getViewsDirectory(project, psiFile);

        if(targetDirectory == null) {
            return;
        }

        ExtractPartialViewDialog dialog = new ExtractPartialViewDialog(project, targetDirectory.getVirtualFile());
        dialog.show();

        if(!dialog.isOK()) {
            return;
        }

        final String viewName = dialog.getViewName();
        final String canonizedViewName = viewName.replace('/', '.');

        ApplicationManager.getApplication().runWriteAction(() -> {
            CommandProcessor.getInstance().executeCommand(editor.getProject(), () -> {

                final String[] viewPathParts = canonizedViewName.split("\\.");

                PsiDirectory directory = targetDirectory;

                for(int i = 0; i < viewPathParts.length - 1; i++) {
                    PsiDirectory newDirectory = directory.findSubdirectory(viewPathParts[i]);

                    if(newDirectory == null) {
                        newDirectory = directory.createSubdirectory(viewPathParts[i]);
                    }

                    directory = newDirectory;
                }

                final String fileName = viewPathParts[viewPathParts.length - 1] + ".blade.php";

                if(directory.findFile(fileName) != null) {
                    Messages.showMessageDialog(project, "File already exists", "Info", Messages.getErrorIcon());
                    return;
                }

                PsiFile newViewFile = PsiFileFactory.getInstance(project).createFileFromText(fileName,
                        BladeLanguage.INSTANCE,
                        selectedText);
                CodeStyleManager.getInstance(project).reformat(newViewFile, false);

                directory.add(newViewFile);

                int selectionStart = editor.getSelectionModel().getSelectionStart();
                int selectionEnd = editor.getSelectionModel().getSelectionEnd();
                editor.getDocument().replaceString(selectionStart,
                        selectionEnd,
                        "@include('" + viewName + "')");

                editor.getSelectionModel().removeSelection();
                editor.getCaretModel().moveToOffset(selectionStart);

            }, "Extract partial view", editor.getDocument());
        });
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiElement[] psiElements, DataContext dataContext) {
        // Will work only on editor, so this method won't be called
    }

    @Nullable
    private PsiDirectory getViewsDirectory(Project project, PsiFile psiFile)
    {
        PsiDirectory firstExistingTemplatePath = null;
        for(TemplatePath templatePath : ViewCollector.getPaths(project)) {
            final VirtualFile templateDir = VfsUtil.findRelativeFile(templatePath.getPath(), project.getBaseDir());

            if(templateDir != null) {

                if(psiFile.getVirtualFile().getPath().startsWith(templateDir.getPath()))
                {
                    return PsiManager.getInstance(project).findDirectory(templateDir);
                }

                if(firstExistingTemplatePath == null) {
                    firstExistingTemplatePath = PsiManager.getInstance(project).findDirectory(templateDir);
                }
            }
        }

        if(firstExistingTemplatePath != null) {
            return firstExistingTemplatePath;
        }

        return PsiManager.getInstance(project).findDirectory(project.getBaseDir());
    }
}
