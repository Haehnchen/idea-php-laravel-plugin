package de.espend.idea.laravel.controller;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ControllerCollector {

    public static void visitController(final Project project, ControllerVisitor visitor) {

        Collection<PhpClass> allSubclasses = new HashSet<PhpClass>() {{
            addAll(PhpIndex.getInstance(project).getAllSubclasses("\\Illuminate\\Routing\\Controller"));
            addAll(PhpIndex.getInstance(project).getAllSubclasses("\\App\\Http\\Controllers\\Controller"));
        }};

        for(PhpClass phpClass: allSubclasses) {
            if(!phpClass.isAbstract()) {
                for(Method method: phpClass.getMethods()) {
                    String className = phpClass.getPresentableFQN();
                    if(className != null) {
                        String methodName = method.getName();
                        if(!method.isStatic() && method.getAccess().isPublic() && !methodName.startsWith("__")) {
                            PhpClass phpTrait = method.getContainingClass();
                            if(phpTrait == null || !("ValidatesRequests".equals(phpTrait.getName()) || "DispatchesCommands".equals(phpTrait.getName()) || "Controller".equals(phpTrait.getName()))) {
                                visitor.visit(method, className + "@" + methodName);
                            }
                        }
                    }

                }
            }

        }
    }

    public static interface ControllerVisitor {
        public void visit(@NotNull Method method, String name);
    }

}

