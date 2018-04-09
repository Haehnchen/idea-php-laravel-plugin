package fr.adrienbrault.idea.symfony2plugin.codeInsight;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public interface GotoCompletionLanguageRegistrar extends GotoCompletionRegistrar {
    boolean support(@NotNull Language language);
}
