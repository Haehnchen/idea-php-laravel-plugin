package de.espend.idea.laravel.stub;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.stubs.indexes.PhpConstantNameIndex;
import gnu.trove.THashMap;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class BladeTemplateUsageStubIndex extends FileBasedIndexExtension<String, Void> {

    public static final ID<String, Void> KEY = ID.create("fr.adrienbrault.idea.symfony2plugin.twig_php_usage");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();
    private static int MAX_FILE_BYTE_SIZE = 2097152;


    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Void> map = new THashMap<>();

            PsiFile psiFile = inputData.getPsiFile();
            if(!(inputData.getPsiFile() instanceof PhpFile) && isValidForIndex(inputData)) {
                return map;
            }

            psiFile.accept(new MyRecursiveBladeTemplateElementWalkingVisitor(map));

            return map;
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
        return PhpConstantNameIndex.PHP_INPUT_FILTER;
    }


    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 2;
    }

    public static boolean isValidForIndex(FileContent inputData) {
        return inputData.getFile().getLength() < MAX_FILE_BYTE_SIZE;
    }

    private static class MyRecursiveBladeTemplateElementWalkingVisitor extends PsiRecursiveElementWalkingVisitor {
        private final Map<String, Void> map;

        public MyRecursiveBladeTemplateElementWalkingVisitor(Map<String, Void> map) {
            this.map = map;
        }

        @Override
        public void visitElement(PsiElement element) {
            if(element instanceof FunctionReference) {
                visitFunctionReference((FunctionReference) element);
            }
            super.visitElement(element);
        }

        private void visitFunctionReference(FunctionReference func) {
            if(!"view".equalsIgnoreCase(func.getName())) {
                return;
            }

            PsiElement[] parameters = func.getParameters();
            if(parameters.length == 0 || !(parameters[0] instanceof StringLiteralExpression)) {
                return;
            }

            String contents = ((StringLiteralExpression) parameters[0]).getContents();
            if(StringUtils.isBlank(contents)) {
                return;
            }

            // replace done by Laravel before rendering
            map.put(contents.replace(".", "/").toLowerCase(), null);
        }
    }
}



