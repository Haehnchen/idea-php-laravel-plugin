package de.espend.idea.laravel.dic;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider3;
import de.espend.idea.laravel.LaravelSettings;
import de.espend.idea.laravel.dic.utils.LaravelDicUtil;
import fr.adrienbrault.idea.symfony2plugin.Symfony2InterfacesUtil;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PhpTypeProviderUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DicTypeProvider implements PhpTypeProvider3 {

    private final static char TRIM_KEY = '\u0197';

    @Override
    public char getKey() {
        return '\u0196';
    }

    @Nullable
    @Override
    public PhpType getType(PsiElement psiElement) {
        if (!LaravelSettings.getInstance(psiElement.getProject()).pluginEnabled) {
            return null;
        }

        // container calls are only on "get" methods
        if(psiElement instanceof FunctionReference && "app".equals(((FunctionReference) psiElement).getName())) {
            PsiElement[] parameters = ((FunctionReference) psiElement).getParameters();
            if(parameters.length > 0 && parameters[0] instanceof StringLiteralExpression) {
                String contents = ((StringLiteralExpression) parameters[0]).getContents();
                if(StringUtils.isNotBlank(contents)) {
                    return new PhpType().add(
                        "#" + this.getKey() + ((FunctionReference) psiElement).getSignature() + TRIM_KEY + contents
                    );
                }
            }
        }

        // container calls are only on "get" methods
        if(psiElement instanceof MethodReference && PhpElementsUtil.isMethodWithFirstStringOrFieldReference(psiElement, "make")) {
            String referenceSignature = PhpTypeProviderUtil.getReferenceSignature((MethodReference) psiElement, TRIM_KEY);
            if(referenceSignature != null) {
                return new PhpType().add("#" + this.getKey() + referenceSignature);
            }
        }

        return null;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String expression, Set<String> visited, int depth, Project project) {
        int endIndex = expression.lastIndexOf(TRIM_KEY);
        if(endIndex == -1) {
            return null;
        }

        String originalSignature = expression.substring(0, endIndex);
        String parameter = expression.substring(endIndex + 1);

        // search for called method
        PhpIndex phpIndex = PhpIndex.getInstance(project);
        Collection<? extends PhpNamedElement> phpNamedElementCollections = phpIndex.getBySignature(originalSignature, null, 0);
        if(phpNamedElementCollections.size() == 0) {
            return null;
        }

        // get first matched item
        PhpNamedElement phpNamedElement = phpNamedElementCollections.iterator().next();
        if(!(phpNamedElement instanceof Function)) {
            return null;
        }

        // on method reference check class instance
        if(phpNamedElement instanceof Method) {
            PhpClass containingClass = ((Method) phpNamedElement).getContainingClass();
            if(containingClass == null) {
                return null;
            }

            Symfony2InterfacesUtil util = new Symfony2InterfacesUtil();
            if(!(util.isInstanceOf(containingClass, "App") || util.isInstanceOf(containingClass, "Illuminate\\Contracts\\Container\\Container"))) {
                return null;
            }
        }

        parameter = PhpTypeProviderUtil.getResolvedParameter(phpIndex, parameter);
        if(parameter == null) {
            return null;
        }

        Collection<PhpNamedElement> phpClasses = new ArrayList<>();

        Map<String, Collection<String>> coreAliasMap = LaravelDicUtil.getDicMap(project);
        if(!coreAliasMap.containsKey(parameter) || coreAliasMap.get(parameter).size() == 0) {
            // support class name "Foo\Class"
            PhpClass phpClass = PhpElementsUtil.getClassInterface(project, parameter);
            if(phpClass != null) {
                phpClasses.add(phpClass);
            }
        } else {
            // find on our dic
            for (String clazz : coreAliasMap.get(parameter)) {
                PhpClass phpClass = PhpElementsUtil.getClassInterface(project, clazz);
                if(phpClass != null) {
                    phpClasses.add(phpClass);
                }
            }
        }

        return !phpClasses.isEmpty() ? phpClasses : null;
    }
}
