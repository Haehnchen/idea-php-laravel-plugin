package de.espend.idea.laravel.view.dict;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class JsonTemplatePaths {
    @Nullable
    private Collection<Path> namespaces = new ArrayList<>();

    @Nullable
    public Collection<Path> getNamespaces() {
        return namespaces;
    }

    public static class Path {
        @Nullable
        private String path;

        @Nullable
        private String namespace;

        @Nullable
        public String getPath() {
            return path;
        }

        @Nullable
        public String getNamespace() {
            return namespace;
        }
    }
}
