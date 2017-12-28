package de.espend.idea.laravel.controller.namespace;

import org.jetbrains.annotations.NotNull;

/**
 * Cuts the controllers namespace for completion. Usually using prefix and default namespace.
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public interface ControllerNamespaceCutter {
    void cut(String controllerClassName, ControllerClassNameProcessor processor);

    interface ControllerClassNameProcessor {
        void process(@NotNull String processedClassName, boolean prioritised);
    }
}
