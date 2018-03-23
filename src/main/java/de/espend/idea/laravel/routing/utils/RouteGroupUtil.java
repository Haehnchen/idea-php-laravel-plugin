package de.espend.idea.laravel.routing.utils;

import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class RouteGroupUtil {
    /**
     * Analyzes Route::group elements and returns string values for specified property.
     * Route::group(['namespace' => 'Foo'], function() {
     *      Route::group(['namespace' => 'Bar'], function() {
     *          Route::get(...
     *      });
     * });
     *
     * getRouteGroupPropertiesCollection(Route::get element, "namespace") will return list with 'Foo', 'Bar'
     */
    @NotNull
    public static List<String> getRouteGroupPropertiesCollection(PsiElement element, String propertyName)
    {
        List<String> values = new ArrayList<>();

        RouteGroupCondition routeGroupCondition = new RouteGroupCondition();

        PsiElement routeGroup = PsiTreeUtil.findFirstParent(element, true, routeGroupCondition);

        while (routeGroup != null) {
            ArrayCreationExpression arrayCreation = PsiTreeUtil.getChildOfType(((MethodReference)routeGroup).getParameterList(), ArrayCreationExpression.class);

            if (arrayCreation != null) {
                for (ArrayHashElement hashElement : arrayCreation.getHashElements()) {
                    if (hashElement.getKey() instanceof StringLiteralExpression) {
                        if (propertyName.equals(((StringLiteralExpression) hashElement.getKey()).getContents())) {
                            if (hashElement.getValue() instanceof StringLiteralExpression) {
                                values.add(((StringLiteralExpression) hashElement.getValue()).getContents());
                            }
                            break;
                        }
                    }
                }
            }

            routeGroup = PsiTreeUtil.findFirstParent(routeGroup, true, routeGroupCondition);
        }

        Collections.reverse(values);
        return values;
    }

    private static class RouteGroupCondition implements Condition<PsiElement> {
        private static Set<String> availableClasses = new HashSet<>(Arrays.asList(
                "\\Illuminate\\Routing\\Router",
                "\\Illuminate\\Support\\Facades\\Route",
                "\\Route"));

        @Override
        public boolean value(PsiElement psiElement) {

            if(!(psiElement instanceof MethodReference)) {
                return false;
            }

            MethodReference methodReference = (MethodReference) psiElement;

            if (!"group".equals(methodReference.getName())) {
                return false;
            }

            if(methodReference.getClassReference() == null || methodReference.getClassReference().getName() == null) {
                return false;
            }

            return availableClasses.contains(PhpElementsUtil.getFullClassName(methodReference, methodReference.getClassReference().getName()));
        }
    }
}