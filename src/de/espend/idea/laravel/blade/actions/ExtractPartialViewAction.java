package de.espend.idea.laravel.blade.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.actions.BaseRefactoringAction;
import com.jetbrains.php.blade.BladeLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
}
