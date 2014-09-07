package fr.adrienbrault.idea.symfony2plugin.codeInsight.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import fr.adrienbrault.idea.symfony2plugin.dic.MethodReferenceBag;
import fr.adrienbrault.idea.symfony2plugin.util.ParameterBag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

public class PhpElementsUtil {

    @Nullable
    static public PhpClass getClassInterface(Project project, @NotNull String className) {

        // api workaround for at least interfaces
        if(!className.startsWith("\\")) {
            className = "\\" + className;
        }

        Collection<PhpClass> phpClasses = PhpIndex.getInstance(project).getAnyByFQN(className);
        return phpClasses.size() == 0 ? null : phpClasses.iterator().next();
    }


    @Nullable
    static public Method getClassMethod(Project project, String phpClassName, String methodName) {

        // we need here an each; because eg Command is non unique because phar file
        for(PhpClass phpClass: PhpIndex.getInstance(project).getClassesByFQN(phpClassName)) {
            Method method = getClassMethod(phpClass, methodName);
            if(method != null) {
                return method;
            }
        }

        return null;
    }

    @Nullable
    static public Method getClassMethod(PhpClass phpClass, String methodName) {
        for(Method method: phpClass.getMethods()) {
            if(method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    static public Collection<PhpClass> getClassesOrInterfaces(Project project, @NotNull String className) {

        // api workaround for at least interfaces
        if(!className.startsWith("\\")) {
            className = "\\" + className;
        }

        return PhpIndex.getInstance(project).getAnyByFQN(className);
    }

    @Nullable
    public static MethodReferenceBag getMethodParameterReferenceBag(PsiElement psiElement) {
        return getMethodParameterReferenceBag(psiElement, -1);
    }

    @Nullable
    public static MethodReferenceBag getMethodParameterReferenceBag(PsiElement psiElement, int wantIndex) {

        PsiElement variableContext = psiElement.getContext();
        if(!(variableContext instanceof ParameterList)) {
            return null;
        }

        ParameterList parameterList = (ParameterList) variableContext;
        if (!(parameterList.getContext() instanceof MethodReference)) {
            return null;
        }

        MethodReference methodReference = (MethodReference) parameterList.getContext();
        PsiElement method = methodReference.resolve();
        if(!(method instanceof Method)) {
            return null;
        }

        ParameterBag currentIndex = getCurrentParameterIndex(psiElement);
        if(currentIndex == null) {
            return null;
        }

        if(wantIndex >= 0 && currentIndex.getIndex() != wantIndex) {
            return null;
        }

        return new MethodReferenceBag(parameterList, methodReference, currentIndex);

    }

    public static boolean isFunctionReference(PsiElement psiElement, int wantIndex, String... funcName) {

        PsiElement variableContext = psiElement.getContext();
        if(!(variableContext instanceof ParameterList)) {
            return false;
        }

        ParameterList parameterList = (ParameterList) variableContext;
        PsiElement context = parameterList.getContext();
        if (!(context instanceof FunctionReference)) {
            return false;
        }

        FunctionReference methodReference = (FunctionReference) context;
        String name = methodReference.getName();

        if(name == null || Arrays.asList(funcName).contains(funcName)) {
            return false;
        }

        ParameterBag currentIndex = getCurrentParameterIndex(psiElement);
        if(currentIndex == null) {
            return false;
        }

        return !(wantIndex >= 0 && currentIndex.getIndex() != wantIndex);

    }

    @Nullable
    public static ParameterBag getCurrentParameterIndex(PsiElement psiElement) {

        if (!(psiElement.getContext() instanceof ParameterList)) {
            return null;
        }

        ParameterList parameterList = (ParameterList) psiElement.getContext();
        if (!(parameterList.getContext() instanceof ParameterListOwner)) {
            return null;
        }

        return getCurrentParameterIndex(parameterList.getParameters(), psiElement);
    }

    @Nullable
    public static ParameterBag getCurrentParameterIndex(PsiElement[] parameters, PsiElement parameter) {
        int i;
        for(i = 0; i < parameters.length; i = i + 1) {
            if(parameters[i].equals(parameter)) {
                return new ParameterBag(i, parameters[i]);
            }
        }

        return null;
    }

    @Nullable
    public static PsiElement[] getMethodParameterReferences(Method method, int parameterIndex) {

        // we dont have a parameter on resolved method
        Parameter[] parameters = method.getParameters();
        if(parameters.length == 0 || parameterIndex >= parameters.length) {
            return null;
        }

        final String tempVariableName = parameters[parameterIndex].getName();
        return PsiTreeUtil.collectElements(method.getLastChild(), new PsiElementFilter() {
            @Override
            public boolean isAccepted(PsiElement element) {
                return element instanceof Variable && tempVariableName.equals(((Variable) element).getName());
            }
        });

    }
}
