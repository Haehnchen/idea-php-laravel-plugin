package de.espend.idea.laravel.controller;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ControllerCollector {

    public static void visitControllerActions(final Project project, ControllerActionVisitor visitor) {

        Collection<PhpClass> allSubclasses = new HashSet<PhpClass>() {{
            addAll(PhpIndex.getInstance(project).getAllSubclasses("\\Illuminate\\Routing\\Controller"));
            addAll(PhpIndex.getInstance(project).getAllSubclasses("\\App\\Http\\Controllers\\Controller"));
        }};

        String ns = getDefaultNamespace(project);

        for(PhpClass phpClass: allSubclasses) {
            if(!phpClass.isAbstract()) {
                for(Method method: phpClass.getMethods()) {
                    String className = phpClass.getPresentableFQN();
                    if(className != null) {
                        String methodName = method.getName();
                        if(!method.isStatic() && method.getAccess().isPublic() && !methodName.startsWith("__")) {
                            PhpClass phpTrait = method.getContainingClass();
                            if(phpTrait == null || !("ValidatesRequests".equals(phpTrait.getName()) || "DispatchesCommands".equals(phpTrait.getName()) || "Controller".equals(phpTrait.getName()))) {

                                if(className.startsWith(ns + "\\")) {
                                    className = className.substring(ns.length() + 1);
                                }

                                if(StringUtils.isNotBlank(className)) {
                                    visitor.visit(method, className + "@" + methodName);
                                }
                            }
                        }
                    }

                }
            }

        }
    }

    @NotNull
    public static String getDefaultNamespace(@NotNull Project project) {

        for (PhpClass providerPhpClass: PhpIndex.getInstance(project).getAllSubclasses("\\Illuminate\\Support\\ServiceProvider")) {
            if(!"RouteServiceProvider".equals(providerPhpClass.getName())) {
                continue;
            }

            Field namespace = providerPhpClass.findOwnFieldByName("namespace", false);
            if(namespace != null) {
                PsiElement defaultValue = namespace.getDefaultValue();
                if(defaultValue instanceof StringLiteralExpression) {
                    String contents = ((StringLiteralExpression) defaultValue).getContents();
                    if(StringUtils.isNotBlank(contents)) {
                        if(contents.startsWith("\\")) {
                            contents = contents.substring(1);
                        }
                        return contents;
                    }
                }
            }
        }

        return "\\App\\Http\\Controllers";
    }

    public interface ControllerActionVisitor {
        void visit(@NotNull Method method, String name);
    }

    public static void visitController(@NotNull final Project project, @NotNull ControllerVisitor visitor) {

        Collection<PhpClass> allSubclasses = new HashSet<PhpClass>() {{
            addAll(PhpIndex.getInstance(project).getAllSubclasses("\\Illuminate\\Routing\\Controller"));
            addAll(PhpIndex.getInstance(project).getAllSubclasses("\\App\\Http\\Controllers\\Controller"));
        }};

        String ns = getDefaultNamespace(project);

        for(PhpClass phpClass: allSubclasses) {

            if(phpClass.isAbstract()) {
                continue;
            }

            String className = phpClass.getPresentableFQN();
            if(className == null) {
                continue;
            }

            if(className.startsWith(ns + "\\")) {
                className = className.substring(ns.length() + 1);
            }

            if(StringUtils.isNotBlank(className)) {
                visitor.visit(phpClass, className);
            }
        }
    }

    public interface ControllerVisitor {
        void visit(@NotNull PhpClass phpClass, @NotNull String name);
    }

}

