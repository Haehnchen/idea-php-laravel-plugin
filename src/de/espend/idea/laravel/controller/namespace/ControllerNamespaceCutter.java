package de.espend.idea.laravel.controller.namespace;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Cuts the controllers namespace for completion. Usually using prefix and default namespace.
 */
public interface ControllerNamespaceCutter {
    void cut(String controllerClassName, ControllerClassNameProcessor processor);

    interface ControllerClassNameProcessor {
        void process(@NotNull String processedClassName, boolean prioritised);
    }
}
