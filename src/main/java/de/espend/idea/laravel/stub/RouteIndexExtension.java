package de.espend.idea.laravel.stub;

import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.io.VoidDataExternalizer;
import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.laravel.routing.utils.RoutingUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class RouteIndexExtension extends FileBasedIndexExtension<String, Void> {

    public static final ID<String, Void> KEY = ID.create("de.espend.idea.laravel.routes");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return fileContent -> {

            PsiFile psiFile = fileContent.getPsiFile();
            if(!isValidFileName(fileContent)) {
                return Collections.emptyMap();
            }

            Map<String, Void> map = new THashMap<>();

            for (String s : RoutingUtil.getRoutesAsNames(psiFile)) {
                map.put(s, null);
            }

            return map;
        };
    }

    private boolean isValidFileName(FileContent psiFile) {
        if("routes.php".equals(psiFile.getFileName())) {
            return true;
        }

        return psiFile.getFile().getPath()
            .matches(".*/routes/(?:.+[/]+?)?([^/]+.\\w{1,4})$");
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return this.myKeyDescriptor;
    }

    @NotNull
    @Override
    public DataExternalizer<Void> getValueExternalizer() {
        return VoidDataExternalizer.INSTANCE;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> file.getFileType() == PhpFileType.INSTANCE;
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
