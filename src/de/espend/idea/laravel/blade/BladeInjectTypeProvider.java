package de.espend.idea.laravel.blade;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.blade.injection.BladeVariableTypeProvider;
import com.jetbrains.php.blade.psi.BladeFileImpl;
import com.jetbrains.php.blade.psi.BladePsiDirective;
import com.jetbrains.php.blade.psi.BladePsiDirectiveParameter;
import com.jetbrains.php.blade.psi.BladePsiLanguageInjectionHost;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider3;
import de.espend.idea.laravel.LaravelSettings;
import de.espend.idea.laravel.blade.util.BladePsiUtil;
import de.espend.idea.laravel.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class BladeInjectTypeProvider implements PhpTypeProvider3 {
    @Override
    public char getKey() {
        return '\u0177';
    }

    @Nullable
    @Override
    public PhpType getType(PsiElement element) {
        if(!(element instanceof Variable) || !LaravelSettings.getInstance(element.getProject()).pluginEnabled){
            return null;
        }

        String name = ((Variable) element).getName();

        PsiFile bladeFile = getHostBladeFileForInjectionIfExists(element);
        if(bladeFile == null) {
            return null;
        }

        PhpType phpType = new PhpType();

        PsiTreeUtil.findChildrenOfType(bladeFile, BladePsiDirective.class).stream()
            .filter(bladePsiDirective -> "@inject".equals(bladePsiDirective.getName()))
            .forEach(bladePsiDirective -> {
                BladePsiDirectiveParameter parameter = PsiTreeUtil.findChildOfType(bladePsiDirective, BladePsiDirectiveParameter.class);
                if(parameter == null) {
                    return;
                }

                List<String> strings = ContainerUtil.map(BladePsiUtil.extractParameters(parameter.getText()), PsiElementUtils::trimQuote);
                if(strings.size() > 1 && name.equals(strings.get(0))) {
                    phpType.add("\\" + StringUtils.stripStart(strings.get(1), "\\"));
                }
            });

        return !phpType.isEmpty() ? phpType : null;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String expression, Set<String> visited, int depth, Project project) {
        return null;
    }

    /**
     *  Resolve PHP language injection in Blade
     *
     *  @see BladeVariableTypeProvider#getHostPhpFileForInjectedIfExists
     */
    @Nullable
    private static BladeFileImpl getHostBladeFileForInjectionIfExists(PsiElement element) {
        PsiFile file = element.getContainingFile();
        InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(element.getProject());
        if (injectedLanguageManager.isInjectedFragment(file)) {
            PsiLanguageInjectionHost host = injectedLanguageManager.getInjectionHost(element);
            if (host instanceof BladePsiLanguageInjectionHost && host.isValidHost()) {
                PsiFile bladeFile = host.getContainingFile();
                if (bladeFile instanceof BladeFileImpl) {
                    return (BladeFileImpl) bladeFile;
                }
            }
        }

        return null;
    }
}