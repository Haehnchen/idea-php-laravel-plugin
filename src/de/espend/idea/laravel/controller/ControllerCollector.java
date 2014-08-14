package de.espend.idea.laravel.controller;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

public class ControllerCollector {

    public static void visitController(Project project, ControllerVisitor visitor) {

        for(PhpClass phpClass: PhpIndex.getInstance(project).getAllSubclasses("\\Illuminate\\Routing\\Controller")) {
            if(!phpClass.isAbstract()) {
                for(Method method: phpClass.getOwnMethods()) {
                    String className = phpClass.getPresentableFQN();
                    if(className != null) {
                        if(!method.isStatic() && method.getAccess().isPublic() && !method.getName().startsWith("__")) {
                            visitor.visit(method, className + "@" + method.getName());
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

