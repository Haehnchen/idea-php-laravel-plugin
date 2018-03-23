package de.espend.idea.laravel.asset;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.LaravelProjectComponent;
import de.espend.idea.laravel.util.PsiElementUtils;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * public/foobar => {{ asset('foobar.css') }}
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AssetGotoCompletionRegistrar implements GotoCompletionRegistrar {
    private static final MethodMatcher.CallToSignature[] ASSETS = {
        new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\UrlGenerator", "asset"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\UrlGenerator", "secureAsset"),
    };

    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {
        /*
         * view('caret');
         * Factory::make('caret');
         */
        registrar.register(PlatformPatterns.psiElement().withParent(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE), psiElement -> {
            if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                return null;
            }

            PsiElement stringLiteral = psiElement.getParent();
            if(!(stringLiteral instanceof StringLiteralExpression)) {
                return null;
            }

            if(!(
                PsiElementUtils.isFunctionReference(stringLiteral, "asset", 0)
                    || PsiElementUtils.isFunctionReference(stringLiteral, "secure_asset", 0)
                    || PsiElementUtils.isFunctionReference(stringLiteral, "secureAsset", 0)
            ) && MethodMatcher.getMatchedSignatureWithDepth(stringLiteral, ASSETS) == null) {
                return null;
            }

            return new AssetGotoCompletionProvider(stringLiteral);
        });
    }

    /**
     * public/foobar => {{ asset('foobar.css') }}
     */
    private static class AssetGotoCompletionProvider extends GotoCompletionProvider {
        AssetGotoCompletionProvider(PsiElement stringLiteral) {
            super(stringLiteral);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {
            return AssetUtil.getLookupElements(getProject());
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {
            String contents = element.getContents();
            if(StringUtils.isBlank(contents)) {
                return Collections.emptyList();
            }

            return new ArrayList<>(
                PsiElementUtils.convertVirtualFilesToPsiFiles(getProject(), AssetUtil.resolveAsset(getProject(), contents))
            );
        }
    }
}
