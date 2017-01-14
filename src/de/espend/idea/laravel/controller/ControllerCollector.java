package de.espend.idea.laravel.controller;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.laravel.controller.namespace.ControllerNamespaceCutter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ControllerCollector {

    private static final Set<String> commonControllerTraits = getCommonControllerTraits();

    private ControllerNamespaceCutter controllerNamespaceCutter;

    public ControllerCollector(ControllerNamespaceCutter controllerNamespaceCutter) {
        this.controllerNamespaceCutter = controllerNamespaceCutter;
    }

    public void visitControllerActions(@NotNull final Project project, @NotNull ControllerActionVisitor visitor, @Nullable String prefix) {

        Collection<PhpClass> allSubclasses = new HashSet<PhpClass>() {{
            addAll(PhpIndex.getInstance(project).getAllSubclasses("\\Illuminate\\Routing\\Controller"));
            addAll(PhpIndex.getInstance(project).getAllSubclasses("\\App\\Http\\Controllers\\Controller"));
        }};

        controllerNamespaceCutter.init(project, prefix);

        for(PhpClass phpClass: allSubclasses) {
            if(!phpClass.isAbstract()) {
                for(Method method: phpClass.getMethods()) {
                    String className = phpClass.getPresentableFQN();
                    String methodName = method.getName();
                    if(!method.isStatic() && method.getAccess().isPublic() && !methodName.startsWith("__")) {
                        PhpClass phpTrait = method.getContainingClass();
                        if(phpTrait == null || !commonControllerTraits.contains(phpTrait.getName())) {

                            controllerNamespaceCutter.cut(className, (processedClassName, prioritised) -> {
                                if(StringUtils.isNotBlank(processedClassName)) {
                                    visitor.visit(phpClass, method, processedClassName + "@" + methodName, prioritised);
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    public void visitController(@NotNull final Project project, @NotNull ControllerVisitor visitor, @Nullable String prefix) {

        Collection<PhpClass> allSubclasses = new HashSet<PhpClass>() {{
            addAll(PhpIndex.getInstance(project).getAllSubclasses("\\Illuminate\\Routing\\Controller"));
            addAll(PhpIndex.getInstance(project).getAllSubclasses("\\App\\Http\\Controllers\\Controller"));
        }};

        controllerNamespaceCutter.init(project, prefix);

        for(PhpClass phpClass: allSubclasses) {

            if(phpClass.isAbstract()) {
                continue;
            }

            String className = phpClass.getPresentableFQN();

            controllerNamespaceCutter.cut(className, (processedClassName, prioritised) -> {
                if(StringUtils.isNotBlank(processedClassName)) {
                    visitor.visit(phpClass, processedClassName, prioritised);
                }
            });
        }
    }

    @NotNull
    private static Set<String> getCommonControllerTraits() {
        Set<String> traits = new HashSet<String>();

        traits.add("ValidatesRequests");
        traits.add("DispatchesCommands");
        traits.add("DispatchesJobs"); // For laravel >=5.3
        traits.add("AuthorizesRequests");
        traits.add("Controller");

        return traits;
    }

    public interface ControllerVisitor {
        void visit(@NotNull PhpClass phpClass, @NotNull String name, boolean prioritised);
    }

    public interface ControllerActionVisitor {
        void visit(@NotNull PhpClass phpClass, @NotNull Method method, @NotNull String name, boolean prioritised);
    }
}