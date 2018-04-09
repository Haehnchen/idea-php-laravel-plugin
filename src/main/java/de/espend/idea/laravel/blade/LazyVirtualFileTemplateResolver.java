package de.espend.idea.laravel.blade;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import de.espend.idea.laravel.blade.util.BladeTemplateUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolve template file names and cache the results
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class LazyVirtualFileTemplateResolver {

    @NotNull
    private final Map<VirtualFile, Collection<String>> resolvedFiles = new HashMap<>();

    @NotNull
    private final Map<String, Collection<VirtualFile>> resolvedStrings = new HashMap<>();

    public Collection<VirtualFile> resolveTemplateName(@NotNull Project project, @NotNull String templateName) {
        return resolvedStrings.computeIfAbsent(templateName, template
            -> BladeTemplateUtil.resolveTemplateName(project, template)
        );
    }

    public Collection<String> resolveTemplateName(@NotNull PsiFile psiFile) {
        return resolveTemplateName(psiFile.getProject(), psiFile.getVirtualFile());
    }

    public Collection<String> resolveTemplateName(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return resolvedFiles.computeIfAbsent(virtualFile, file
            -> BladeTemplateUtil.resolveTemplateName(project, file)
        );
    }
}
