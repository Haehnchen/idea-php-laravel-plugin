package fr.adrienbrault.idea.symfony2plugin.codeInsight.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class CompletionContributorParameter {
    @NotNull
    private final CompletionParameters completionParameters;

    @NotNull
    private final ProcessingContext processingContext;

    @NotNull
    private final CompletionResultSet completionResultSet;

    public CompletionContributorParameter(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        this.completionParameters = completionParameters;
        this.processingContext = processingContext;
        this.completionResultSet = completionResultSet;
    }

    @NotNull
    public CompletionParameters getCompletionParameters() {
        return completionParameters;
    }

    @NotNull
    public ProcessingContext getProcessingContext() {
        return processingContext;
    }

    @NotNull
    public CompletionResultSet getCompletionResultSet() {
        return completionResultSet;
    }
}
