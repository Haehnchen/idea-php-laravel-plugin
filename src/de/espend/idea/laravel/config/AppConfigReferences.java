package de.espend.idea.laravel.config;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.LaravelIcons;
import de.espend.idea.laravel.LaravelProjectComponent;
import de.espend.idea.laravel.stub.ConfigKeyStubIndex;
import de.espend.idea.laravel.stub.processor.CollectProjectUniqueKeys;
import de.espend.idea.laravel.util.ArrayReturnPsiRecursiveVisitor;
import de.espend.idea.laravel.util.PsiElementUtils;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionLanguageRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AppConfigReferences implements GotoCompletionLanguageRegistrar {

    private static MethodMatcher.CallToSignature[] CONFIG = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Illuminate\\Config\\Repository", "get"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Config\\Repository", "has"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Config\\Repository", "set"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Config\\Repository", "setParsedKey"),
    };

    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {
        registrar.register(PlatformPatterns.psiElement(), psiElement -> {
            if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                return null;
            }

            PsiElement parent = psiElement.getParent();
            if(parent != null && (PsiElementUtils.isFunctionReference(parent, "config", 0) || MethodMatcher.getMatchedSignatureWithDepth(parent, CONFIG) != null)) {
                return new ConfigKeyProvider(parent);
            }

            return null;
        });
    }

    @Override
    public boolean support(@NotNull Language language) {
        return PhpLanguage.INSTANCE == language;
    }

    private static class ConfigKeyProvider extends GotoCompletionProvider {

        public ConfigKeyProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {

            final Collection<LookupElement> lookupElements = new ArrayList<>();

            CollectProjectUniqueKeys ymlProjectProcessor = new CollectProjectUniqueKeys(getProject(), ConfigKeyStubIndex.KEY);
            FileBasedIndex.getInstance().processAllKeys(ConfigKeyStubIndex.KEY, ymlProjectProcessor, getProject());
            for(String key: ymlProjectProcessor.getResult()) {
                lookupElements.add(LookupElementBuilder.create(key).withIcon(LaravelIcons.CONFIG));
            }

            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {

            final Set<PsiElement> targets = new HashSet<>();

            final String contents = element.getContents();
            if(StringUtils.isBlank(contents)) {
                return targets;
            }

            FileBasedIndex.getInstance().getFilesWithKey(ConfigKeyStubIndex.KEY, new HashSet<>(Collections.singletonList(contents)), virtualFile -> {
                PsiFile psiFileTarget = PsiManager.getInstance(getProject()).findFile(virtualFile);
                if(psiFileTarget == null) {
                    return true;
                }

                psiFileTarget.acceptChildren(new ArrayReturnPsiRecursiveVisitor(virtualFile.getNameWithoutExtension(), (key, psiKey, isRootElement) -> {
                    if(!isRootElement && key.equals(contents)) {
                        targets.add(psiKey);
                    }
                }));

                return true;
            }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(getProject()), PhpFileType.INSTANCE));

            return targets;
        }
    }
}
