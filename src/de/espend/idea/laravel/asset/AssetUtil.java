package de.espend.idea.laravel.asset;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AssetUtil {
    private static final String[] FOLDERS = new String[] {
        "public"
    };

    public static Collection<LookupElement> getLookupElements(@NotNull Project project) {
        Collection<LookupElement> lookupElements = new ArrayList<>();

        PsiManager psiManager = PsiManager.getInstance(project);

        for (String folder : FOLDERS) {
            VirtualFile assetDir = VfsUtil.findRelativeFile(project.getBaseDir(), folder);
            if(assetDir == null) {
                continue;
            }

            VfsUtil.visitChildrenRecursively(assetDir, new VirtualFileVisitor() {
                @Override
                public boolean visitFile(@NotNull VirtualFile virtualFile) {
                    if(virtualFile.isDirectory()) {
                        return true;
                    }

                    String filename = VfsUtil.getRelativePath(virtualFile, assetDir, '/');
                    if(filename == null || filename.startsWith(".")) {
                        return true;
                    }

                    PsiFile psiFile = psiManager.findFile(virtualFile);
                    if(psiFile != null) {
                        LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(filename);
                        lookupElementBuilder = lookupElementBuilder.withIcon(psiFile.getIcon(0));

                        lookupElements.add(lookupElementBuilder);
                    }

                    return true;
                }
            });
        }

        return lookupElements;
    }

    /**
     * Provide targets for given assets. Mainly inside "/public" folder of root
     */
    @NotNull
    public static Collection<VirtualFile> resolveAsset(@NotNull Project project, @NotNull String asset) {
        // normalize path
        String path = asset
            .replace("\\", "/")
            .replaceAll("/+", "/");

        return Stream.of(FOLDERS)
            .map(folder -> VfsUtil.findRelativeFile(project.getBaseDir(), folder))
            .filter(Objects::nonNull)
            .map(relativeFile -> VfsUtil.findRelativeFile(relativeFile, path.split("/")))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }
}
