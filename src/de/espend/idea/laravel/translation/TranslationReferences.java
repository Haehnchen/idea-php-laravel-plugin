package de.espend.idea.laravel.translation;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.LaravelIcons;
import de.espend.idea.laravel.LaravelProjectComponent;
import de.espend.idea.laravel.config.AppConfigReferences;
import de.espend.idea.laravel.stub.TranslationKeyStubIndex;
import de.espend.idea.laravel.stub.processor.ArrayKeyVisitor;
import de.espend.idea.laravel.stub.processor.CollectProjectUniqueKeys;
import de.espend.idea.laravel.util.ArrayReturnPsiRecursiveVisitor;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionContributor;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TranslationReferences implements GotoCompletionRegistrar {

    private static MethodMatcher.CallToSignature[] TRANSLATION_KEY = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Illuminate\\Translation\\Translator", "get"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Translation\\Translator", "has"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Translation\\Translator", "choice"),
    };

    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {

        registrar.register(PlatformPatterns.psiElement(), new GotoCompletionContributor() {

            @Nullable
            @Override
            public GotoCompletionProvider getProvider(@Nullable PsiElement psiElement) {

                if(psiElement == null || !LaravelProjectComponent.isEnabled(psiElement)) {
                    return null;
                }

                PsiElement parent = psiElement.getParent();
                if(parent != null && (
                    MethodMatcher.getMatchedSignatureWithDepth(parent, TRANSLATION_KEY) != null || PhpElementsUtil.isFunctionReference(parent, 0, "trans")
                )) {
                    return new TranslationKey(parent);
                }

                return null;

            }

        });

    }

    private static class TranslationKey extends GotoCompletionProvider {

        public TranslationKey(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {

            final Collection<LookupElement> lookupElements = new ArrayList<LookupElement>();

            CollectProjectUniqueKeys ymlProjectProcessor = new CollectProjectUniqueKeys(getProject(), TranslationKeyStubIndex.KEY);
            FileBasedIndexImpl.getInstance().processAllKeys(TranslationKeyStubIndex.KEY, ymlProjectProcessor, getProject());
            for(String key: ymlProjectProcessor.getResult()) {
                lookupElements.add(LookupElementBuilder.create(key).withIcon(LaravelIcons.TRANSLATION));
            }

            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {

            final Set<PsiElement> targets = new HashSet<PsiElement>();

            final String contents = element.getContents();
            if(StringUtils.isBlank(contents)) {
                return targets;
            }

            FileBasedIndexImpl.getInstance().getFilesWithKey(TranslationKeyStubIndex.KEY, new HashSet<String>(Arrays.asList(contents)), new Processor<VirtualFile>() {
                @Override
                public boolean process(VirtualFile virtualFile) {
                    PsiFile psiFileTarget = PsiManager.getInstance(getProject()).findFile(virtualFile);
                    if(psiFileTarget == null) {
                        return true;
                    }

                    psiFileTarget.acceptChildren(new ArrayReturnPsiRecursiveVisitor(virtualFile.getNameWithoutExtension(), new ArrayKeyVisitor() {
                        @Override
                        public void visit(String key, PsiElement psiKey, boolean isRootElement) {
                            if(!isRootElement && key.equals(contents)) {
                                targets.add(psiKey);
                            }
                        }
                    }));

                    return true;
                }
            }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(getProject()), PhpFileType.INSTANCE));

            return targets;
        }

    }

}
