package de.espend.idea.laravel.stub;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import de.espend.idea.laravel.config.AppConfigReferences;
import de.espend.idea.laravel.stub.processor.ArrayKeyVisitor;
import de.espend.idea.laravel.util.ArrayReturnPsiRecursiveVisitor;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class TranslationKeyStubIndex extends FileBasedIndexExtension<String, Void> {

    public static final ID<String, Void> KEY = ID.create("de.espend.idea.laravel.translation_keys");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return new DataIndexer<String, Void, FileContent>() {
            @NotNull
            @Override
            public Map<String, Void> map(@NotNull FileContent fileContent) {

                final Map<String, Void> map = new THashMap<String, Void>();

                PsiFile psiFile = fileContent.getPsiFile();
                if(!(psiFile instanceof PhpFile)) {
                    return map;
                }

                String path = fileContent.getFile().getPath();

                // app/lang/fr_FR/messages.php
                // app/lang/fr/messages.php
                // app/lang/packages/en/hearthfire/messages.php
                // app/lang/packages/fr_FR/hearthfire/messages.php
                if(path.matches(".*/lang/(\\w{2}|\\w{2}_\\w{2})/\\w+.php$") || path.matches(".*/lang/packages/(\\w{2}|\\w{2}_\\w{2})/\\w+/\\w+.php$")) {
                    psiFile.acceptChildren(new ArrayReturnPsiRecursiveVisitor(fileContent.getFile().getNameWithoutExtension(), new ArrayKeyVisitor() {
                        @Override
                        public void visit(String key, PsiElement psiKey, boolean isRootElement) {
                            map.put(key, null);
                        }
                    }));
                }

                return map;
            }
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return this.myKeyDescriptor;
    }

    @NotNull
    @Override
    public DataExternalizer<Void> getValueExternalizer() {
        return ScalarIndexExtension.VOID_DATA_EXTERNALIZER;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new FileBasedIndex.InputFilter() {
            @Override
            public boolean acceptInput(@NotNull VirtualFile file) {
                return file.getFileType() == PhpFileType.INSTANCE;
            }
        };
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
