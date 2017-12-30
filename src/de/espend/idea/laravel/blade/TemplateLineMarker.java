package de.espend.idea.laravel.blade;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ConstantFunction;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.psi.BladePsiDirectiveParameter;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import de.espend.idea.laravel.LaravelIcons;
import de.espend.idea.laravel.LaravelProjectComponent;
import de.espend.idea.laravel.blade.util.BladePsiUtil;
import de.espend.idea.laravel.blade.util.BladeTemplateUtil;
import de.espend.idea.laravel.stub.*;
import de.espend.idea.laravel.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class TemplateLineMarker implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements, @NotNull Collection<LineMarkerInfo> collection) {

        // we need project element; so get it from first item
        if(psiElements.size() == 0) {
            return;
        }

        Project project = psiElements.get(0).getProject();
        if(!LaravelProjectComponent.isEnabled(project)) {
            return;
        }

        for(PsiElement psiElement: psiElements) {
            if(psiElement instanceof PsiFile) {
                // template file like rendering:

                collectTemplateFileRelatedFiles((PsiFile) psiElement, collection);
            } else if(psiElement.getNode().getElementType() == BladeTokenTypes.SECTION_DIRECTIVE) {
                Pair<PsiElement, String> section = extractSectionParameter(psiElement);
                if(section != null) {
                    collectOverwrittenSection(section.getFirst(), collection, section.getSecond());
                    collectImplementsSection(section.getFirst(), collection, section.getSecond());
                }
            } else if(psiElement.getNode().getElementType() == BladeTokenTypes.YIELD_DIRECTIVE) {
                Pair<PsiElement, String> section = extractSectionParameter(psiElement);
                if(section != null) {
                    collectImplementsSection(section.getFirst(), collection, section.getSecond());
                }
            } else if(psiElement.getNode().getElementType() == BladeTokenTypes.STACK_DIRECTIVE) {
                Pair<PsiElement, String> section = extractSectionParameter(psiElement);
                if(section != null) {
                    collectStackImplements(section.getFirst(), collection, section.getSecond());
                }
            } else if(psiElement.getNode().getElementType() == BladeTokenTypes.PUSH_DIRECTIVE) {
                Pair<PsiElement, String> section = extractSectionParameter(psiElement);
                if(section != null) {
                    collectPushOverwrites(section.getFirst(), collection, section.getSecond());
                }
            } else if(psiElement.getNode().getElementType() == BladeTokenTypes.SLOT_DIRECTIVE) {
                // @slot('foobar')
                Pair<PsiElement, String> section = extractSectionParameter(psiElement);
                if(section != null) {
                    collectSlotOverwrites(section.getFirst(), collection, section.getSecond());
                }
            }
        }
    }

    /**
     * Extract parameter: @foobar('my_value')
     */
    @Nullable
    private Pair<PsiElement, String> extractSectionParameter(@NotNull PsiElement psiElement) {
        PsiElement nextSibling = psiElement.getNextSibling();

        if(nextSibling instanceof BladePsiDirectiveParameter) {
            String sectionName = BladePsiUtil.getSection(nextSibling);
            if (sectionName != null && StringUtils.isNotBlank(sectionName)) {
                return Pair.create(nextSibling, sectionName);
            }
        }

        return null;
    }

    /**
     * like this @section('sidebar')
     */
    private void collectOverwrittenSection(final PsiElement psiElement, @NotNull Collection<LineMarkerInfo> collection, final String sectionName) {

        final List<GotoRelatedItem> gotoRelatedItems = new ArrayList<>();

        for(PsiElement psiElement1 : psiElement.getContainingFile().getChildren()) {
            PsiElement extendDirective = psiElement1.getFirstChild();
            if(extendDirective != null && extendDirective.getNode().getElementType() == BladeTokenTypes.EXTENDS_DIRECTIVE) {
                PsiElement bladeParameter = extendDirective.getNextSibling();
                if(bladeParameter instanceof BladePsiDirectiveParameter) {
                    String extendTemplate = BladePsiUtil.getSection(bladeParameter);
                    if(extendTemplate != null) {
                        Set<VirtualFile> virtualFiles = BladeTemplateUtil.resolveTemplateName(psiElement.getProject(), extendTemplate);
                        for(VirtualFile virtualFile: virtualFiles) {
                            PsiFile psiFile = PsiManager.getInstance(psiElement.getProject()).findFile(virtualFile);
                            if(psiFile != null) {
                                visitOverwrittenTemplateFile(psiFile, gotoRelatedItems, sectionName);
                            }

                        }
                    }
                }
            }
        }

        if(gotoRelatedItems.size() == 0) {
            return;
        }

        collection.add(getRelatedPopover("Parent Section", "Blade Section", psiElement, gotoRelatedItems, PhpIcons.OVERRIDES));
    }

    private void collectTemplateFileRelatedFiles(@NotNull PsiFile psiFile, @NotNull Collection<LineMarkerInfo> collection) {
        Collection<String> collectedTemplates = BladeTemplateUtil.resolveTemplateName(psiFile);
        if(collectedTemplates.size() == 0) {
            return;
        }

        // lowercase for index
        Set<String> templateNames = new HashSet<>();
        for (String templateName : collectedTemplates) {
            templateNames.add(templateName);
            templateNames.add(templateName.toLowerCase());
        }

        // normalize all template names and support both: "foo.bar" and "foo/bar"
        templateNames.addAll(new HashSet<>(templateNames)
            .stream().map(templateName -> templateName.replace(".", "/"))
            .collect(Collectors.toList())
        );

        AtomicBoolean includeLineMarker = new AtomicBoolean(false);
        for(ID<String, Void> key : Arrays.asList(BladeExtendsStubIndex.KEY, BladeSectionStubIndex.KEY, BladeIncludeStubIndex.KEY, BladeEachStubIndex.KEY)) {
            for(String templateName: templateNames) {
                FileBasedIndex.getInstance().getFilesWithKey(key, new HashSet<>(Collections.singletonList(templateName)), virtualFile -> {
                    includeLineMarker.set(true);

                    // stop on first file match
                    return false;
                }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(psiFile.getProject()), BladeFileType.INSTANCE));
            }

            // found an element; stop iteration for all index keys
            if(includeLineMarker.get()) {
                break;
            }
        }

        if(includeLineMarker.get()) {
            NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                .create(PhpIcons.IMPLEMENTED)
                .setTargets(new TemplateIncludeCollectionNotNullLazyValue(psiFile.getProject(), templateNames))
                .setTooltipText("Navigate to Blade file");

            collection.add(builder.createLineMarkerInfo(psiFile));
        }

        // try to find at least von controller target; lazyly load target later via click
        boolean controllerLineMarker = false;
        for(String templateName: templateNames) {
            Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(PhpTemplateUsageStubIndex.KEY, templateName, GlobalSearchScope.allScope(psiFile.getProject()));
            if(files.size() > 0) {
                controllerLineMarker = true;
                break;
            }
        }

        if(controllerLineMarker) {
            NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                .create(LaravelIcons.TEMPLATE_CONTROLLER_LINE_MARKER)
                .setTargets(new ControllerRenderViewCollectionNotNullLazyValue(psiFile.getProject(), templateNames))
                .setTooltipText("Navigate to controller");

            collection.add(builder.createLineMarkerInfo(psiFile));
        }
    }

    private LineMarkerInfo getRelatedPopover(String singleItemTitle, String singleItemTooltipPrefix, PsiElement lineMarkerTarget, Collection<GotoRelatedItem> gotoRelatedItems, Icon icon) {

        // single item has no popup
        String title = singleItemTitle;
        if(gotoRelatedItems.size() == 1) {
            String customName = gotoRelatedItems.iterator().next().getCustomName();
            if(customName != null) {
                title = String.format(singleItemTooltipPrefix, customName);
            }
        }

        return new LineMarkerInfo<>(
            lineMarkerTarget,
            lineMarkerTarget.getTextRange(),
            icon,
            6,
            new ConstantFunction<>(title),
            new RelatedPopupGotoLineMarker.NavigationHandler(gotoRelatedItems),
            GutterIconRenderer.Alignment.RIGHT
        );
    }

    private void visitOverwrittenTemplateFile(final PsiFile psiFile, final List<GotoRelatedItem> gotoRelatedItems, final String sectionName) {
        visitOverwrittenTemplateFile(psiFile, gotoRelatedItems, sectionName, 10);
    }

    private void visitOverwrittenTemplateFile(final PsiFile psiFile, final List<GotoRelatedItem> gotoRelatedItems, final String sectionName, int depth) {
        // simple secure recursive calls
        if(depth-- <= 0) {
            return;
        }

        BladeTemplateUtil.DirectiveParameterVisitor visitor = parameter -> {
            if (sectionName.equalsIgnoreCase(parameter.getContent())) {
                gotoRelatedItems.add(new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(parameter.getPsiElement()).withIcon(LaravelIcons.LARAVEL, LaravelIcons.LARAVEL));
            }
        };

        BladeTemplateUtil.visitSection(psiFile, visitor);
        BladeTemplateUtil.visitYield(psiFile, visitor);

        final int finalDepth = depth;
        BladeTemplateUtil.visitExtends(psiFile, parameter -> {
            Set<VirtualFile> virtualFiles = BladeTemplateUtil.resolveTemplateName(psiFile.getProject(), parameter.getContent());
            for (VirtualFile virtualFile : virtualFiles) {
                PsiFile templatePsiFile = PsiManager.getInstance(psiFile.getProject()).findFile(virtualFile);
                if (templatePsiFile != null) {
                    visitOverwrittenTemplateFile(templatePsiFile, gotoRelatedItems, sectionName, finalDepth);
                }
            }
        });

    }

    /**
     * Find all sub implementations of a section that are overwritten by an extends tag
     * Possible targets are: @section('sidebar')
     */
    private void collectImplementsSection(PsiElement psiElement, @NotNull Collection<LineMarkerInfo> collection, @NotNull String sectionName) {
        Collection<String> templateNames = BladeTemplateUtil.resolveTemplateName(psiElement.getContainingFile());
        if(templateNames.size() == 0) {
            return;
        }

        Collection<GotoRelatedItem> gotoRelatedItems = new ArrayList<>();

        Set<VirtualFile> virtualFiles = BladeTemplateUtil.getExtendsImplementations(psiElement.getProject(), templateNames);
        if(virtualFiles.size() == 0) {
            return;
        }

        for(VirtualFile virtualFile: virtualFiles) {
            PsiFile psiFile = PsiManager.getInstance(psiElement.getProject()).findFile(virtualFile);
            if(psiFile != null) {
                BladeTemplateUtil.visitSection(psiFile, parameter -> {
                    if (sectionName.equalsIgnoreCase(parameter.getContent())) {
                        gotoRelatedItems.add(new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(parameter.getPsiElement()).withIcon(LaravelIcons.LARAVEL, LaravelIcons.LARAVEL));
                    }
                });
            }
        }

        if(gotoRelatedItems.size() == 0) {
            return;
        }

        collection.add(getRelatedPopover("Template", "Blade File", psiElement, gotoRelatedItems, PhpIcons.IMPLEMENTED));
    }

    /**
     * Support: @stack('foobar')
     */
    private void collectStackImplements(final PsiElement psiElement, @NotNull Collection<LineMarkerInfo> collection, final String sectionName) {
        Collection<String> templateNames = BladeTemplateUtil.resolveTemplateName(psiElement.getContainingFile());
        if(templateNames.size() == 0) {
            return;
        }

        final List<GotoRelatedItem> gotoRelatedItems = new ArrayList<>();

        Set<VirtualFile> virtualFiles = BladeTemplateUtil.getExtendsImplementations(psiElement.getProject(), templateNames);
        if(virtualFiles.size() == 0) {
            return;
        }

        for(VirtualFile virtualFile: virtualFiles) {
            PsiFile psiFile = PsiManager.getInstance(psiElement.getProject()).findFile(virtualFile);
            if(psiFile != null) {
                BladeTemplateUtil.visit(psiFile, BladeTokenTypes.PUSH_DIRECTIVE, parameter -> {
                    if (sectionName.equalsIgnoreCase(parameter.getContent())) {
                        gotoRelatedItems.add(new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(parameter.getPsiElement()).withIcon(LaravelIcons.LARAVEL, LaravelIcons.LARAVEL));
                    }
                });
            }
        }

        if(gotoRelatedItems.size() == 0) {
            return;
        }

        collection.add(getRelatedPopover("Push Implementation", "Push Implementation", psiElement, gotoRelatedItems, PhpIcons.IMPLEMENTED));
    }

    /**
     * Support: @push('foobar')
     */
    private void collectPushOverwrites(final PsiElement psiElement, @NotNull Collection<LineMarkerInfo> collection, final String sectionName) {
        final List<GotoRelatedItem> gotoRelatedItems = new ArrayList<>();

        BladeTemplateUtil.visitUpPath(psiElement.getContainingFile(), 10, parameter -> {
            if(sectionName.equalsIgnoreCase(parameter.getContent())) {
                gotoRelatedItems.add(new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(parameter.getPsiElement()).withIcon(LaravelIcons.LARAVEL, LaravelIcons.LARAVEL));
            }
        }, BladeTokenTypes.STACK_DIRECTIVE);

        if(gotoRelatedItems.size() == 0) {
            return;
        }

        collection.add(getRelatedPopover("Stack Section", "Stack Overwrites", psiElement, gotoRelatedItems, PhpIcons.OVERRIDES));
    }

    /**
     * Support: @slot('foobar')
     */
    private void collectSlotOverwrites(final PsiElement psiElement, @NotNull Collection<LineMarkerInfo> collection, final String sectionName) {
        if(!(psiElement instanceof BladePsiDirectiveParameter)) {
            return;
        }

        String component = BladePsiUtil.findComponentForSlotScope((BladePsiDirectiveParameter) psiElement);
        if(component == null) {
            return;
        }

        List<GotoRelatedItem> gotoRelatedItems = new ArrayList<>();

        for (VirtualFile virtualFile : BladeTemplateUtil.resolveTemplateName(psiElement.getProject(), component)) {
            PsiFile file = PsiManager.getInstance(psiElement.getProject()).findFile(virtualFile);
            if(file == null) {
                continue;
            }

            gotoRelatedItems.addAll(BladePsiUtil.collectPrintBlockVariableTargets(file, sectionName).stream()
                .map((Function<PsiElement, GotoRelatedItem>) element ->
                    new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(element).withIcon(LaravelIcons.LARAVEL, LaravelIcons.LARAVEL))
                .collect(Collectors.toList()
            ));
        }

        if(gotoRelatedItems.size() == 0) {
            return;
        }

        collection.add(getRelatedPopover("Slot Overwrites", "Slot Overwrites", psiElement, gotoRelatedItems, PhpIcons.OVERRIDES));
    }

    /**
     * Provide navigation for all rendering calls in php controller of given template names
     */
    private static class ControllerRenderViewCollectionNotNullLazyValue extends NotNullLazyValue<Collection<? extends PsiElement>> {
        @NotNull
        private final Project project;

        @NotNull
        private final Collection<String> templateNames;

        private ControllerRenderViewCollectionNotNullLazyValue(@NotNull Project project, @NotNull Collection<String> templateNames) {
            this.project = project;
            this.templateNames = templateNames;
        }

        @NotNull
        @Override
        protected Collection<? extends PsiElement> compute() {
            Collection<VirtualFile> files = new HashSet<>();

            // find template usages of controller
            for (String templateName: templateNames) {
                files.addAll(FileBasedIndex.getInstance().getContainingFiles(
                    PhpTemplateUsageStubIndex.KEY,
                    templateName,
                    GlobalSearchScope.allScope(project)
                ));
            }

            Collection<PsiElement> targets = new ArrayList<>();

            for (PsiFile psiFile : PsiElementUtils.convertVirtualFilesToPsiFiles(project, files)) {
                Collection<Pair<String, PsiElement>> pairs = BladeTemplateUtil.getViewTemplatesPairScope(psiFile);

                for (String templateName : templateNames) {
                    for (Pair<String, PsiElement> pair : pairs) {
                        if (templateName.equalsIgnoreCase(pair.first)) {
                            targets.add(pair.getSecond());
                        }
                    }
                }
            }

            return targets;
        }
    }

    /**
     * Provide navigation for all rendering calls in php controller of given template names
     */
    private static class TemplateIncludeCollectionNotNullLazyValue extends NotNullLazyValue<Collection<? extends PsiElement>> {
        @NotNull
        private final Project project;

        @NotNull
        private final Collection<String> templateNames;

        private TemplateIncludeCollectionNotNullLazyValue(@NotNull Project project, @NotNull Collection<String> templateNames) {
            this.project = project;
            this.templateNames = templateNames;
        }

        @NotNull
        @Override
        protected Collection<? extends PsiElement> compute() {
            Collection<VirtualFile> virtualFiles = new ArrayList<>();

            for(ID<String, Void> key : Arrays.asList(BladeExtendsStubIndex.KEY, BladeSectionStubIndex.KEY, BladeIncludeStubIndex.KEY, BladeEachStubIndex.KEY)) {
                for(String templateName: templateNames) {
                    FileBasedIndex.getInstance().getFilesWithKey(key, new HashSet<>(Collections.singletonList(templateName)), virtualFile -> {
                        virtualFiles.add(virtualFile);
                        return true;
                    }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(project), BladeFileType.INSTANCE));
                }
            }

            return PsiElementUtils.convertVirtualFilesToPsiFiles(project, virtualFiles);
        }
    }
}
