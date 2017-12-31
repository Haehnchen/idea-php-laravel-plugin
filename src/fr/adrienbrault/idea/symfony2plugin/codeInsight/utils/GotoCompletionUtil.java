package fr.adrienbrault.idea.symfony2plugin.codeInsight.utils;

import com.intellij.psi.PsiElement;
import de.espend.idea.laravel.asset.AssetGotoCompletionRegistrar;
import de.espend.idea.laravel.blade.BladeDirectiveReferences;
import de.espend.idea.laravel.config.AppConfigReferences;
import de.espend.idea.laravel.config.ProviderGotoCompletion;
import de.espend.idea.laravel.controller.ControllerReferences;
import de.espend.idea.laravel.dic.DicCompletionRegistrar;
import de.espend.idea.laravel.routing.RoutingGotoCompletionRegistrar;
import de.espend.idea.laravel.translation.TranslationReferences;
import de.espend.idea.laravel.view.ViewReferences;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionContributor;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionLanguageRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;

import java.util.ArrayList;
import java.util.Collection;

public class GotoCompletionUtil {

    private static GotoCompletionRegistrar[] CONTRIBUTORS = new GotoCompletionRegistrar[] {
        new ProviderGotoCompletion(),
        new ViewReferences(),
        new ControllerReferences(),
        new AppConfigReferences(),
        new BladeDirectiveReferences(),
        new TranslationReferences(),
        new RoutingGotoCompletionRegistrar(),
        new DicCompletionRegistrar(),
        new AssetGotoCompletionRegistrar(),
    };

    public static Collection<GotoCompletionContributor> getContributors(final PsiElement psiElement) {
        Collection<GotoCompletionContributor> contributors = new ArrayList<>();

        GotoCompletionRegistrarParameter registrar = (pattern, contributor) -> {
            if(pattern.accepts(psiElement)) {
                contributors.add(contributor);
            }
        };

        for(GotoCompletionRegistrar register: CONTRIBUTORS) {
            // filter on language
            if(register instanceof GotoCompletionLanguageRegistrar) {
                if(((GotoCompletionLanguageRegistrar) register).support(psiElement.getLanguage())) {
                    register.register(registrar);
                }
            } else {
                register.register(registrar);
            }
        }

        return contributors;
    }
}
