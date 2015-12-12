package de.espend.idea.laravel.controller;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.laravel.LaravelSettings;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ControllerCollector {

    public static void visitControllerActions(@NotNull final Project project, @NotNull ControllerActionVisitor visitor, @Nullable String prefix) {

        Collection<PhpClass> allSubclasses = new HashSet<PhpClass>() {{
            addAll(PhpIndex.getInstance(project).getAllSubclasses("\\Illuminate\\Routing\\Controller"));
            addAll(PhpIndex.getInstance(project).getAllSubclasses("\\App\\Http\\Controllers\\Controller"));
        }};

        String ns = getDefaultNamespace(project) + "\\";
        String prefixedNs = ns + (prefix != null && !prefix.equals("") ? prefix + "\\":"");

        for(PhpClass phpClass: allSubclasses) {
            if(!phpClass.isAbstract()) {
                for(Method method: phpClass.getMethods()) {
                    String className = phpClass.getPresentableFQN();
                    String methodName = method.getName();
                    if(!method.isStatic() && method.getAccess().isPublic() && !methodName.startsWith("__")) {
                        PhpClass phpTrait = method.getContainingClass();
                        if(phpTrait == null || !("ValidatesRequests".equals(phpTrait.getName()) || "DispatchesCommands".equals(phpTrait.getName()) || "Controller".equals(phpTrait.getName()))) {

                            boolean prioritised = false;
                            if(prefix != null && className.startsWith(prefixedNs)) {
                                className = className.substring(prefixedNs.length());
                                prioritised = true;
                            } else if(className.startsWith(ns)) {
                                className = className.substring(ns.length());
                            } if(prefix != null && className.startsWith(prefix)) {
                                className = className.substring(prefix.length() + 1);
                            }

                            if(StringUtils.isNotBlank(className)) {
                                visitor.visit(phpClass, method, className + "@" + methodName, prioritised);
                            }
                        }
                    }
                }
            }
        }
    }

    @NotNull
    public static String getDefaultNamespace(@NotNull Project project) {

        String controllerNamespace = LaravelSettings.getInstance(project).routerNamespace;
        if(controllerNamespace != null && StringUtils.isNotBlank(controllerNamespace)) {
            return StringUtils.stripStart(controllerNamespace, "\\");
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
                return StringUtils.stripStart(stringValue, "\\");
            }
        }

        return "App\\Http\\Controllers";
    }

    public interface ControllerActionVisitor {
        void visit(@NotNull PhpClass phpClass, @NotNull Method method, @NotNull String name, boolean prioritised);
    }

    public static void visitController(@NotNull final Project project, @NotNull ControllerVisitor visitor, @Nullable String prefix) {

        Collection<PhpClass> allSubclasses = new HashSet<PhpClass>() {{
            addAll(PhpIndex.getInstance(project).getAllSubclasses("\\Illuminate\\Routing\\Controller"));
            addAll(PhpIndex.getInstance(project).getAllSubclasses("\\App\\Http\\Controllers\\Controller"));
        }};

        String ns = getDefaultNamespace(project) + "\\";
        String prefixedNs = ns + (prefix != null && !prefix.equals("") ? prefix + "\\":"");

        for(PhpClass phpClass: allSubclasses) {

            if(phpClass.isAbstract()) {
                continue;
            }

            String className = phpClass.getPresentableFQN();

            boolean prioritised = false;
            if(prefix != null && className.startsWith(prefixedNs)) {
                className = className.substring(prefixedNs.length());
                prioritised = true;
            } else if(className.startsWith(ns)) {
                className = className.substring(ns.length());
            } if(prefix != null && className.startsWith(prefix)) {
                className = className.substring(prefix.length() + 1);
            }

            if(StringUtils.isNotBlank(className)) {
                visitor.visit(phpClass, className, prioritised);
            }
        }
    }

    public interface ControllerVisitor {
        void visit(@NotNull PhpClass phpClass, @NotNull String name, boolean prioritised);
    }

}

