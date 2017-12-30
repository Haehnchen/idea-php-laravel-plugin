package de.espend.idea.laravel.config;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigFileUtil
{
    private static final Pattern configFilePattern = Pattern.compile(".*/config/([\\w-/]+).php$");

    public static ConfigFileMatchResult matchConfigFile(Project project, VirtualFile virtualFile) {
        String path = virtualFile.getPath();
        String projectPath = project.getBaseDir().getPath();

        if(path.startsWith(projectPath)) {
            path = path.substring(projectPath.length());
        }

        Matcher m = configFilePattern.matcher(path);

        // config/app.php
        // config/testing/app.php
        if(m.matches()) {
            return new ConfigFileMatchResult(true, m.group(1).replace('/', '.'));
        } else {
            return new ConfigFileMatchResult(false, "");
        }
    }

    public static class ConfigFileMatchResult
    {
        private boolean matches;

        private String keyPrefix;

        ConfigFileMatchResult(boolean matches, @NotNull String keyPrefix) {
            this.matches = matches;
            this.keyPrefix = keyPrefix;
        }

        public boolean matches() {
            return matches;
        }

        @NotNull
        public String getKeyPrefix() {
            return keyPrefix;
        }
    }
}
