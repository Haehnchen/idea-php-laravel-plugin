package de.espend.idea.laravel;

import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpReference;
import com.jetbrains.php.lang.psi.resolve.PhpReferenceResolver2;

import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("UnstableApiUsage")
public class ReferenceResolver2 implements PhpReferenceResolver2 {
    @Override
    public Collection<? extends PhpNamedElement> resolve(PhpReference phpReference, Collection<? extends PhpNamedElement> candidates) {
        if (candidates.size()>1) {
            for (PhpNamedElement element : candidates) {
                if (element.getContainingFile().getVirtualFile().getName().equals("_ide_helper.php"))
                    return Collections.singleton(element);
            }
        }
        return candidates;
    }
}
