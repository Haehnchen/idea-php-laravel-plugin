package de.espend.idea.laravel.factory.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModelFactoryTypeProviderUtil {
    private final static String COLLECTION_SIG = "\\Illuminate\\Database\\Eloquent\\Collection.class";

    @Nullable
    public static FunctionReferenceImpl resolveFunctionRef(@NotNull VariableImpl variable) {
        AssignmentExpressionImpl assignment = getAssignmentExpr(variable);

        return assignment != null ? (FunctionReferenceImpl) assignment.getValue() : null;
    }

    @Nullable
    public static String getRefSig(FunctionReference functionReference, char trimKey, char splitKey) {

        String refSignature = functionReference.getSignature();

        if (StringUtil.isEmpty(refSignature)) {
            return null;
        }

        PsiElement[] parameters = functionReference.getParameters();
        if (parameters.length == 0) {
            return null;
        }

        PsiElement parameter = parameters[0];

        if (parameters.length > 1) {
            return refSignature + trimKey + "#K#C" + COLLECTION_SIG;
        }

        return refSignature + trimKey + getRefSigFromParameter(parameter);
    }

    @Nullable
    private static String getRefSigFromParameter(PsiElement parameter) {
        if (parameter instanceof StringLiteralExpression) {
            String param = ((StringLiteralExpression)parameter).getContents();

            if (StringUtil.isEmpty(param)) {
                return null;
            }

            return param;
        }

        if (parameter instanceof ClassConstantReference || parameter instanceof FieldReference) {
            String signature = ((PhpReference) parameter).getSignature();

            if (StringUtil.isEmpty(signature)) {
                return null;
            }

            return signature;
        }

        if (parameter instanceof VariableImpl) {
            AssignmentExpressionImpl assignment = getAssignmentExpr((VariableImpl) parameter);

            if (assignment == null || assignment.getValue() == null) {
                return null;
            }

            return getRefSigFromParameter(assignment.getValue());
        }

        return null;
    }

    @Nullable
    private static AssignmentExpressionImpl getAssignmentExpr(@NotNull VariableImpl variable) {
        PsiReference reference = variable.getReference();
        if (reference == null) {
            return null;
        }

        PsiElement target = reference.resolve();
        if (target == null) {
            return null;
        }

        PsiElement targetParent = target.getParent();

        return targetParent instanceof AssignmentExpressionImpl ? (AssignmentExpressionImpl) targetParent : null;
    }
}
