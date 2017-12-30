package de.espend.idea.laravel.stub.processor;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class CollectProjectUniqueKeys implements Processor<String> {

    @NotNull
    private final Project project;

    @NotNull
    private final ID id;

    @NotNull
    private final Set<String> stringSet;

    public CollectProjectUniqueKeys(@NotNull Project project, @NotNull ID id) {
        this.project = project;
        this.id = id;
        this.stringSet = new HashSet<>();
    }

    @Override
    public boolean process(String s) {
        this.stringSet.add(s);
        return true;
    }

    public Set<String> getResult() {
        Set<String> set = new HashSet<>();

        for (String key : stringSet) {
            Collection fileCollection = FileBasedIndex.getInstance().getContainingFiles(id, key, GlobalSearchScope.allScope(project));
            if (fileCollection.size() > 0) {
                set.add(key);
            }
        }

        return set;
    }

    @NotNull
    public static Set<String> collect(@NotNull Project project, @NotNull ID<String, ?> id) {
        CollectProjectUniqueKeys collector = new CollectProjectUniqueKeys(project, id);
        FileBasedIndex.getInstance().processAllKeys(id, collector, project);
        return collector.getResult();
    }
}