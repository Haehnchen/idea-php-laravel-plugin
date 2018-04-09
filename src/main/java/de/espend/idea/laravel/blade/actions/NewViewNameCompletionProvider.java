package de.espend.idea.laravel.blade.actions;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.util.TextFieldCompletionProvider;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class NewViewNameCompletionProvider extends TextFieldCompletionProvider {
    private final List<LookupElement> completions;

    public NewViewNameCompletionProvider(VirtualFile targetDirectory) {

        completions = new ArrayList<>();

        VfsUtil.visitChildrenRecursively(targetDirectory, new VirtualFileVisitor() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {

                if(!file.isDirectory()) {
                    return true;
                }

                String path = StringUtils.stripStart(file.getPath().replace(targetDirectory.getPath(), ""), "\\/");

                completions.add(LookupElementBuilder.create(path.replace("\\", ".").replace("/", ".") + "."));

                return true;
            }
        });
    }

    @Override
    protected void addCompletionVariants(@NotNull String s, int i, @NotNull String s1, @NotNull CompletionResultSet completionResultSet) {

        completionResultSet.addAllElements(completions);
    }
}
