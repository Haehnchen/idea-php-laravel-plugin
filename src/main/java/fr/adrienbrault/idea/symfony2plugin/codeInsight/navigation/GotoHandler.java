package fr.adrienbrault.idea.symfony2plugin.codeInsight.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.LaravelProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionContributor;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProviderInterface;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.GotoCompletionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class GotoHandler implements GotoDeclarationHandler {
    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor) {

        if (!LaravelProjectComponent.isEnabled(psiElement)) {
            return PsiElement.EMPTY_ARRAY;
        }

        Collection<PsiElement> psiTargets = new ArrayList<PsiElement>();

        PsiElement parent = psiElement.getParent();

        for(GotoCompletionContributor contributor: GotoCompletionUtil.getContributors(psiElement)) {
            GotoCompletionProviderInterface gotoCompletionContributorProvider = contributor.getProvider(psiElement);
            if(gotoCompletionContributorProvider != null) {
                // @TODO: replace this: just valid PHP files
                if(parent instanceof StringLiteralExpression) {
                    psiTargets.addAll(gotoCompletionContributorProvider.getPsiTargets((StringLiteralExpression) parent));
                } else {
                    psiTargets.addAll(gotoCompletionContributorProvider.getPsiTargets(psiElement));
                }

                psiTargets.addAll(gotoCompletionContributorProvider.getPsiTargets(psiElement, i, editor));
            }
        }

        return psiTargets.toArray(new PsiElement[psiTargets.size()]);
    }

    @Nullable
    @Override
    public String getActionText(DataContext dataContext) {
        return null;
    }
}
