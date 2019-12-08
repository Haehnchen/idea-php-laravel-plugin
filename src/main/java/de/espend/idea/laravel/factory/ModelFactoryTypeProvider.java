package de.espend.idea.laravel.factory;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4;
import de.espend.idea.laravel.LaravelSettings;
import de.espend.idea.laravel.factory.utils.ModelFactoryTypeProviderUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PhpTypeProviderUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class ModelFactoryTypeProvider implements PhpTypeProvider4 {
    private final static char TRIM_KEY = '\u0253';
    private final static char SPLIT_KEY = '\u0254';

    @Override
    public char getKey() {
        return '\u0263';
    }

    private static boolean isElementValid(PsiElement e) {
        if (!(e instanceof MethodReference)) {
            return false;
        }

        String methodRefName = ((MethodReference) e).getName();

        if (methodRefName == null) {
            return false;
        }

        if (!Arrays.asList("create", "make").contains(methodRefName)) return false;

        if (StringUtils.isEmpty(((MethodReference) e).getSignature())) return false;

        PsiElement target = ((MethodReference) e).resolve();

        if (!(target instanceof Method)) {
            return false;
        }

        PhpClass containingClass = ((Method) target).getContainingClass();

        if (containingClass == null) {
            return false;
        }

        return "\\Illuminate\\Database\\Eloquent\\FactoryBuilder".equals(containingClass.getFQN());
    }

    @Override @Nullable
    public PhpType getType(PsiElement e) {
        if (DumbService.getInstance(e.getProject()).isDumb() || !LaravelSettings.getInstance(e.getProject()).pluginEnabled) {
            return null;
        }

        if (!isElementValid(e)) {
            return null;
        }

        PsiElement firstPsiChild = PsiTreeUtil.findChildOfAnyType(e, FunctionReferenceImpl.class, VariableImpl.class);

        if (firstPsiChild instanceof FunctionReferenceImpl) {
            String refSignature = ModelFactoryTypeProviderUtil.getRefSig((FunctionReference) firstPsiChild, TRIM_KEY, SPLIT_KEY);
            if (refSignature == null) {
                return null;
            }

            return new PhpType().add("#" + this.getKey() + TRIM_KEY + refSignature);
        } else if (firstPsiChild instanceof VariableImpl) {
            FunctionReference functionRef = ModelFactoryTypeProviderUtil.resolveFunctionRef((VariableImpl) firstPsiChild);

            if (functionRef == null) {
                return null;
            }

            String refSignature = ModelFactoryTypeProviderUtil.getRefSig(functionRef, TRIM_KEY, SPLIT_KEY);

            return new PhpType().add("#" + this.getKey() + TRIM_KEY + refSignature);
        }

        return null;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String sig, Set<String> set, int i, Project project) {
        int endIndex = sig.lastIndexOf(TRIM_KEY);
        if (endIndex == -1) {
            return null;
        }

        String parameter = sig.substring(endIndex + 1);

        PhpIndex index = PhpIndex.getInstance(project);

        parameter = PhpTypeProviderUtil.getResolvedParameter(index, parameter);

        if (parameter == null) {
            return null;
        }

        Collection<PhpClass> classes = index.getClassesByFQN(parameter);

        return classes.isEmpty() ? null : classes;
    }

    @Override @Nullable
    public PhpType complete(String s, Project project) { return null; }
}
