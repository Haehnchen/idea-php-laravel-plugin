package de.espend.idea.laravel.config;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.laravel.LaravelIcons;
import de.espend.idea.laravel.LaravelProjectComponent;
import de.espend.idea.laravel.LaravelSettings;
import de.espend.idea.laravel.util.ArrayReturnPsiRecursiveVisitor;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionContributor;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AppConfigReferences implements GotoCompletionRegistrar {

    private static MethodMatcher.CallToSignature[] CONFIG = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Illuminate\\Config\\Repository", "get"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Config\\Repository", "has"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Config\\Repository", "set"),
        new MethodMatcher.CallToSignature("\\Illuminate\\Config\\Repository", "setParsedKey"),
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
                if(parent != null && MethodMatcher.getMatchedSignatureWithDepth(parent, CONFIG) != null) {
                    return new ControllerRoute(parent);
                }

                return null;

            }

        });

    }

    private static class ControllerRoute extends GotoCompletionProvider {

        public ControllerRoute(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {

            final Collection<LookupElement> lookupElements = new ArrayList<LookupElement>();

            visitConfigs(new ConfigVisitor() {
                @Override
                public void visitConfig(String key, PsiElement psiKey, boolean isRootElement) {

                    LookupElementBuilder lookup = LookupElementBuilder.create(key)
                        .withTypeText(psiKey.getContainingFile().getName(), true)
                        .withIcon(LaravelIcons.LARAVEL);

                    if (isRootElement) {
                        lookup.withTypeText("(root)", true);
                    }

                    lookupElements.add(lookup);
                }
            });

            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {

            final String text = element.getContents();
            if(StringUtils.isBlank(text)) {
                return Collections.emptyList();
            }

            final Set<PsiElement> targets = new HashSet<PsiElement>();

            visitConfigs(new ConfigVisitor() {
                @Override
                public void visitConfig(String key, PsiElement psiKey, boolean isRootElement) {
                    if(text.equalsIgnoreCase(key)) {
                        targets.add(psiKey);
                    }
                }
            });

            return targets;
        }

        private void visitConfigs(ConfigVisitor configVisitor) {

            VirtualFile appConfig = VfsUtil.findRelativeFile(getProject().getBaseDir(), LaravelSettings.getInstance(getProject()).configDirectory.split("/"));
            if(appConfig == null) {
                return;
            }

            VfsUtil.visitChildrenRecursively(appConfig, new MyVirtualFileVisitor(PsiManager.getInstance(getProject()), configVisitor));
        }
    }

    public interface ConfigVisitor {
        public void visitConfig(String key, PsiElement psiKey, boolean isRootElement);
    }

    private static class MyVirtualFileVisitor extends VirtualFileVisitor {

        private final PsiManager psiManager;
        private final ConfigVisitor configVisitor;

        public MyVirtualFileVisitor(PsiManager psiManager, ConfigVisitor configVisitor) {
            this.psiManager = psiManager;
            this.configVisitor = configVisitor;
        }

        @Override
        public boolean visitFile(@NotNull VirtualFile virtualFile) {

            if(virtualFile.getFileType() == PhpFileType.INSTANCE) {
                final PsiFile psiFile = psiManager.findFile(virtualFile);
                if(psiFile != null) {
                    psiFile.acceptChildren(new ArrayReturnPsiRecursiveVisitor(psiFile.getVirtualFile().getNameWithoutExtension(), configVisitor));
                }
            }

            return super.visitFile(virtualFile);
        }

    }
}
