package fr.adrienbrault.idea.symfony2plugin.codeInsight.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.patterns.PhpPatterns;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import fr.adrienbrault.idea.symfony2plugin.dic.MethodReferenceBag;
import fr.adrienbrault.idea.symfony2plugin.util.ParameterBag;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

        ParameterBag currentIndex = getCurrentParameterIndex(psiElement);
        if(currentIndex == null) {
            return null;
        }

        if(wantIndex >= 0 && currentIndex.getIndex() != wantIndex) {
            return null;
        }

        return new MethodReferenceBag(parameterList, (MethodReference) parameterList.getContext(), currentIndex);

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

        if(name == null || !Arrays.asList(funcName).contains(name)) {
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

    @Nullable
    public static ArrayCreationExpression getCompletableArrayCreationElement(PsiElement psiElement) {

        // array('<test>' => '')
        if(PhpPatterns.psiElement(PhpElementTypes.ARRAY_KEY).accepts(psiElement.getContext())) {
            PsiElement arrayKey = psiElement.getContext();
            if(arrayKey != null) {
                PsiElement arrayHashElement = arrayKey.getContext();
                if(arrayHashElement instanceof ArrayHashElement) {
                    PsiElement arrayCreationExpression = arrayHashElement.getContext();
                    if(arrayCreationExpression instanceof ArrayCreationExpression) {
                        return (ArrayCreationExpression) arrayCreationExpression;
                    }
                }
            }

        }

        // on array creation key dont have value, so provide completion here also
        // array('foo' => 'bar', '<test>')
        if(PhpPatterns.psiElement(PhpElementTypes.ARRAY_VALUE).accepts(psiElement.getContext())) {
            PsiElement arrayKey = psiElement.getContext();
            if(arrayKey != null) {
                PsiElement arrayCreationExpression = arrayKey.getContext();
                if(arrayCreationExpression instanceof ArrayCreationExpression) {
                    return (ArrayCreationExpression) arrayCreationExpression;
                }

            }

        }

        return null;
    }

    @Nullable
    public static String getStringValue(@Nullable PsiElement psiElement) {
        return getStringValue(psiElement, 0);
    }

    @Nullable
    private static String getStringValue(@Nullable PsiElement psiElement, int depth) {

        if(psiElement == null || ++depth > 5) {
            return null;
        }

        if(psiElement instanceof StringLiteralExpression) {
            String resolvedString = ((StringLiteralExpression) psiElement).getContents();
            if(StringUtils.isEmpty(resolvedString)) {
                return null;
            }

            return resolvedString;
        }

        if(psiElement instanceof Field) {
            return getStringValue(((Field) psiElement).getDefaultValue(), depth);
        }

        if(psiElement instanceof PhpReference) {

            PsiReference psiReference = psiElement.getReference();
            if(psiReference == null) {
                return null;
            }

            PsiElement ref = psiReference.resolve();
            if(ref instanceof PhpReference) {
                return getStringValue(psiElement, depth);
            }

            if(ref instanceof Field) {
                PsiElement resolved = ((Field) ref).getDefaultValue();

                if(resolved instanceof StringLiteralExpression) {
                    return ((StringLiteralExpression) resolved).getContents();
                }
            }

        }

        return null;

    }


    @Nullable
    static public PhpPsiElement getArrayValue(ArrayCreationExpression arrayCreationExpression, String name) {

        for(ArrayHashElement arrayHashElement: arrayCreationExpression.getHashElements()) {
            PhpPsiElement child = arrayHashElement.getKey();
            if(child instanceof StringLiteralExpression) {
                if(((StringLiteralExpression) child).getContents().equals(name)) {
                    return arrayHashElement.getValue();
                }
            }
        }

        return null;
    }

    @Nullable
    static public String getArrayValueString(ArrayCreationExpression arrayCreationExpression, String name) {
        PhpPsiElement phpPsiElement = getArrayValue(arrayCreationExpression, name);
        if(phpPsiElement == null) {
            return null;
        }

        if(phpPsiElement instanceof StringLiteralExpression) {
            return ((StringLiteralExpression) phpPsiElement).getContents();
        }

        return null;
    }

    /**
     * Gets array key-value as single PsiElement map
     *
     * ['foo' => $bar]
     */
    @NotNull
    static public Map<String, PsiElement> getArrayValueMap(@NotNull ArrayCreationExpression arrayCreationExpression) {
        Map<String, PsiElement> keys = new HashMap<String, PsiElement>();

        for(ArrayHashElement arrayHashElement: arrayCreationExpression.getHashElements()) {
            PhpPsiElement child = arrayHashElement.getKey();
            if(child instanceof StringLiteralExpression) {
                PhpPsiElement value = arrayHashElement.getValue();
                if(value != null) {
                    keys.put(((StringLiteralExpression) child).getContents(), value);
                }
            }
        }

        return keys;
    }


    /**
     * Gets string values of array
     *
     * ["value", "value2"]
     */
    @NotNull
    static public Set<String> getArrayValuesAsString(@NotNull ArrayCreationExpression arrayCreationExpression) {
        return getArrayValuesAsMap(arrayCreationExpression).keySet();
    }

    /**
     * Get array string values mapped with their PsiElements
     *
     * ["value", "value2"]
     */
    @NotNull
    static public Map<String, PsiElement> getArrayValuesAsMap(@NotNull ArrayCreationExpression arrayCreationExpression) {

        List<PsiElement> arrayValues = PhpPsiUtil.getChildren(arrayCreationExpression, new Condition<PsiElement>() {
            @Override
            public boolean value(PsiElement psiElement) {
                return psiElement.getNode().getElementType() == PhpElementTypes.ARRAY_VALUE;
            }
        });

        if(arrayValues == null) {
            return Collections.emptyMap();
        }

        Map<String, PsiElement> keys = new HashMap<String, PsiElement>();
        for (PsiElement child : arrayValues) {
            String stringValue = PhpElementsUtil.getStringValue(child.getFirstChild());
            if(stringValue != null && StringUtils.isNotBlank(stringValue)) {
                keys.put(stringValue, child);
            }
        }

        return keys;
    }
}
