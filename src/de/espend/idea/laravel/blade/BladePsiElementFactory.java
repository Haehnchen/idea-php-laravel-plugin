package de.espend.idea.laravel.blade;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.blade.BladeFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class BladePsiElementFactory {

    public static PsiFile createDummyFile(@NotNull Project project, @NotNull String text) {
        return PsiFileFactory.getInstance(project).createFileFromText("DUMMY__." + BladeFileType.INSTANCE.getDefaultExtension(), BladeFileType.INSTANCE, text);
    }

    @Nullable
    public static <T extends PsiElement> T createFromText(@NotNull Project p, final Class<T> aClass, String text) {
        final PsiElement[] ret = new PsiElement[]{null};

        createDummyFile(p, text).accept(new PsiRecursiveElementWalkingVisitor() {
            public void visitElement(PsiElement element) {
                if(ret[0] == null && aClass.isInstance(element)) {
                    ret[0] = element;
                }

                super.visitElement(element);
            }
        });

        return (T) ret[0];
    }

}
