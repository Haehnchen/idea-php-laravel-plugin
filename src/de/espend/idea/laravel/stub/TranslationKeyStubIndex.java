package de.espend.idea.laravel.stub;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.psi.BladeDirectiveParameterPsiImpl;
import com.jetbrains.php.blade.psi.BladeFileImpl;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import de.espend.idea.laravel.config.AppConfigReferences;
import de.espend.idea.laravel.util.ArrayReturnPsiRecursiveVisitor;
import de.espend.idea.laravel.util.PsiElementUtils;
import gnu.trove.THashMap;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                    psiFile.acceptChildren(new ArrayReturnPsiRecursiveVisitor(fileContent.getFile().getNameWithoutExtension(), new AppConfigReferences.ConfigVisitor() {
                        @Override
                        public void visitConfig(String key, PsiElement psiKey, boolean isRootElement) {
                            if (!isRootElement) {
                                map.put(key, null);
                            }
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
