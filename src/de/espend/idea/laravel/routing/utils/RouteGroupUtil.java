package de.espend.idea.laravel.routing.utils;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RouteGroupUtil
{
    private static MethodMatcher.CallToSignature[] ROUTE_GROUP = new MethodMatcher.CallToSignature[] {
            new MethodMatcher.CallToSignature("\\Illuminate\\Routing\\Router", "group"),
    };

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

        PsiElement routeGroup = PsiTreeUtil.findFirstParent(element, true, psiElement ->
                MethodMatcher.getMatchedSignatureWithDepth(psiElement, ROUTE_GROUP, 1) != null
        );

        while (routeGroup != null) {
            ArrayCreationExpression arrayCreation = PsiTreeUtil.getChildOfType(routeGroup.getParent(), ArrayCreationExpression.class);

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

            routeGroup = PsiTreeUtil.findFirstParent(routeGroup, true, psiElement ->
                    MethodMatcher.getMatchedSignatureWithDepth(psiElement, ROUTE_GROUP, 1) != null
            );
        }

        return Lists.reverse(values);
    }
}