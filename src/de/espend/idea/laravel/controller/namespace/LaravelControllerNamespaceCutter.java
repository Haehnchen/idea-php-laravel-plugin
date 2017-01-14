package de.espend.idea.laravel.controller.namespace;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.laravel.LaravelSettings;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

public class LaravelControllerNamespaceCutter implements ControllerNamespaceCutter {

    private String prefix;

    /**
     * Collection of possible default namespaces. default namespace + default namespace with prefix
     */
    private Collection<String> defaultNamespaces = new HashSet<>();

    @Override
    public void init(@NotNull Project project, @Nullable String prefix) {

        this.prefix = prefix;

        this.defaultNamespaces = this.getDefaultNamespaces(project);
    }

    @Override
    public void cut(String className, ControllerClassNameProcessor processor) {

        for(String defaultNamespace: this.defaultNamespaces) {
            String prefixedNamespace = defaultNamespace + (StringUtils.isNotBlank(prefix) ? prefix + "\\" : "");

            if(StringUtils.isNotBlank(prefix) && className.startsWith(prefixedNamespace)) {
                processor.process(className.substring(prefixedNamespace.length()), true);
                return;
            } else if(className.startsWith(defaultNamespace)) {
                processor.process(className.substring(defaultNamespace.length()), false);
                return;
            } else if(StringUtils.isNotBlank(prefix) && className.startsWith(prefix + "\\")) {
                processor.process(className.substring(prefix.length() + 1), false);
                return;
            }
        }

        processor.process(className, false);
    }


    @NotNull
    private Collection<String> getDefaultNamespaces(@NotNull Project project) {

        Collection<String> result = new HashSet<>();

        String controllerNamespace = LaravelSettings.getInstance(project).routerNamespace;
        if(controllerNamespace != null && StringUtils.isNotBlank(controllerNamespace)) {
            result.add(StringUtils.stripStart(controllerNamespace, "\\") + "\\");
            return result;
        }

        for(PhpClass providerPhpClass: PhpIndex.getInstance(project).getAllSubclasses("\\Illuminate\\Foundation\\Support\\Providers\\RouteServiceProvider")) {

            Field namespace = providerPhpClass.findOwnFieldByName("namespace", false);
            if(namespace == null) {
                continue;
            }

            PsiElement defaultValue = namespace.getDefaultValue();
            if(defaultValue == null) {
                continue;
            }

            String stringValue = PhpElementsUtil.getStringValue(defaultValue);
            if(stringValue != null) {
                result.add(StringUtils.stripStart(stringValue, "\\") + "\\");
            }
        }

        if(result.isEmpty()) {
            result.add("App\\Http\\Controllers\\");
        }

        return result;
    }
}
