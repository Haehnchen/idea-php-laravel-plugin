package de.espend.idea.laravel.blade.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.refactoring.RefactoringActionHandler;
import org.apache.commons.lang.StringUtils;
import com.jetbrains.php.blade.BladeLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExtractPartialViewHandler implements RefactoringActionHandler {

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile, DataContext dataContext) {
        final String selectedText = editor.getSelectionModel().getSelectedText();

        if(selectedText == null) return;

        final String viewPath = Messages.showInputDialog(project, "View path", "New View Path", Messages.getQuestionIcon());

        if(StringUtils.isBlank(viewPath)) return;

        final String canonizedViewPath = viewPath.replace('/', '.');

        ApplicationManager.getApplication().runWriteAction(() -> {
            Document document = FileDocumentManager.getInstance().getDocument(psiFile.getVirtualFile());

            if(document == null) return;

            CommandProcessor.getInstance().executeCommand(editor.getProject(), () -> {

                final String[] viewPathParts = canonizedViewPath.split("\\.");

                PsiDirectory targetDirectory = getViewsDirectory(psiFile);

                if(targetDirectory == null) return;

                for(int i = 0; i < viewPathParts.length - 1; i++) {
                    PsiDirectory newDirectory = targetDirectory.findSubdirectory(viewPathParts[i]);

                    if(newDirectory == null) {
                        newDirectory = targetDirectory.createSubdirectory(viewPathParts[i]);
                    }

                    targetDirectory = newDirectory;
                }

                final String fileName = viewPathParts[viewPathParts.length - 1] + ".blade.php";

                if(targetDirectory.findFile(fileName) != null) {
                    Messages.showMessageDialog(project, "File already exists", "Info", Messages.getErrorIcon());
                    return;
                }

                targetDirectory.add(PsiFileFactory.getInstance(project).createFileFromText(fileName,
                        BladeLanguage.INSTANCE,
                        selectedText));

                int selectionStart = editor.getSelectionModel().getSelectionStart();
                document.replaceString(selectionStart,
                        editor.getSelectionModel().getSelectionEnd(),
                        "@include('" + canonizedViewPath + "')");

                editor.getSelectionModel().removeSelection();
                editor.getCaretModel().moveToOffset(selectionStart);

            }, "Extracting partial view", editor.getDocument());
        });
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiElement[] psiElements, DataContext dataContext) {
        // Will work only on editor, so this method won't be called
    }

    @Nullable
    private PsiDirectory getViewsDirectory(PsiFile psiFile)
    {
        String basePath = psiFile.getProject().getBasePath();

        PsiDirectory directory = psiFile.getContainingDirectory();
        while(directory != null && !directory.getVirtualFile().getPath().equals(basePath)) {
            if(directory.getName().equals("views")) {
                return directory;
            }

            if(directory.getParentDirectory() != null) {
                directory = directory.getParentDirectory();
            } else {
                break;
            }
        }

        if(directory == null) return null;

        // if current view isn't in 'views' directory, try to find resources/views dir.
        PsiDirectory resourcesDirectory = directory.findSubdirectory("resources");
        if( resourcesDirectory != null ) {
            PsiDirectory viewsDirectory = resourcesDirectory.findSubdirectory("views");
            if(viewsDirectory != null) {
                return viewsDirectory;
            }
        }

        return directory;
    }
}
