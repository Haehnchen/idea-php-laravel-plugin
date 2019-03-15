package de.espend.idea.laravel.blade.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.actions.BaseRefactoringAction;
import com.jetbrains.php.blade.BladeLanguage;
import de.espend.idea.laravel.LaravelSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ExtractPartialViewAction extends BaseRefactoringAction {
    @Override
    protected boolean isAvailableInEditorOnly() {
        return true;
    }

    @Override
    protected boolean isEnabledOnElements(@NotNull PsiElement[] psiElements) {
        return true;
    }

    @Nullable
    @Override
    protected RefactoringActionHandler getHandler(@NotNull DataContext dataContext) {
        return new ExtractPartialViewHandler();
    }

    protected boolean isAvailableForLanguage(com.intellij.lang.Language language) {
        return BladeLanguage.INSTANCE.getID().equals(language.getID());
    }

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        super.update(anActionEvent);

        final Project project = anActionEvent.getProject();
        if (project == null) {
            return;
        }

        if(!LaravelSettings.getInstance(project).pluginEnabled) {
            anActionEvent.getPresentation().setVisible(false);
            return;
        }

        Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);

        if(editor == null) {
            return;
        }

        anActionEvent.getPresentation().setEnabled((editor.getSelectionModel().hasSelection()));
    }
}
