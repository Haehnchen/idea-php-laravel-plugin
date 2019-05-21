package de.espend.idea.laravel.stub;

import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.io.VoidDataExternalizer;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.psi.BladeFileImpl;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import de.espend.idea.laravel.stub.processor.BladeDirectivePsiElementWalkingVisitor;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class BladeSectionStubIndex extends FileBasedIndexExtension<String, Void> {

    public static final ID<String, Void> KEY = ID.create("de.espend.idea.laravel.blade.section");

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return fileContent -> {
            Map<String, Void> map = new THashMap<>();
            PsiFile psiFile = fileContent.getPsiFile();

            if(!(psiFile instanceof BladeFileImpl)) {
                return map;
            }

            psiFile.acceptChildren(new BladeDirectivePsiElementWalkingVisitor(BladeTokenTypes.SECTION_DIRECTIVE, map));

            return map;
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @NotNull
    @Override
    public DataExternalizer<Void> getValueExternalizer() {
        return VoidDataExternalizer.INSTANCE;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> file.getFileType() == BladeFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
