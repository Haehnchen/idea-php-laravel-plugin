package de.espend.idea.laravel.blade;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ConstantFunction;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.intellij.util.indexing.ID;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.psi.BladePsiDirectiveParameter;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import de.espend.idea.laravel.LaravelIcons;
import de.espend.idea.laravel.LaravelProjectComponent;
import de.espend.idea.laravel.blade.util.BladePsiUtil;
import de.espend.idea.laravel.blade.util.BladeTemplateUtil;
import de.espend.idea.laravel.stub.BladeExtendsStubIndex;
import de.espend.idea.laravel.stub.BladeIncludeStubIndex;
import de.espend.idea.laravel.stub.BladeSectionStubIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

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
                collectTemplateFileRelatedFiles((PsiFile) psiElement, collection);
            }

            if(psiElement.getNode().getElementType() == BladeTokenTypes.SECTION_DIRECTIVE) {
                PsiElement nextSibling = psiElement.getNextSibling();
                if(nextSibling instanceof BladePsiDirectiveParameter) {
                    String sectionName = BladePsiUtil.getSection(nextSibling);
                    if(sectionName != null) {
                        collectOverwrittenSection(nextSibling, collection, sectionName);
                        collectImplementsSection(nextSibling, collection, sectionName);
                    }
                }
            }

            if(psiElement.getNode().getElementType() == BladeTokenTypes.YIELD_DIRECTIVE) {
                PsiElement nextSibling = psiElement.getNextSibling();
                if(nextSibling instanceof BladePsiDirectiveParameter) {
                    String sectionName = BladePsiUtil.getSection(nextSibling);
                    if(sectionName != null) {
                        collectImplementsSection(nextSibling, collection, sectionName);
                    }
                }
            }

        }

    }

    /**
     * like this @section('sidebar')
     */
    private void collectOverwrittenSection(final PsiElement psiElement, @NotNull Collection<LineMarkerInfo> collection, final String sectionName) {

        final List<GotoRelatedItem> gotoRelatedItems = new ArrayList<GotoRelatedItem>();

        for(PsiElement psiElement1 : psiElement.getContainingFile().getChildren()) {
            PsiElement extendDirective = psiElement1.getFirstChild();
            if(extendDirective != null && extendDirective.getNode().getElementType() == BladeTokenTypes.EXTENDS_DIRECTIVE) {
                PsiElement bladeParameter = extendDirective.getNextSibling();
                if(bladeParameter instanceof BladePsiDirectiveParameter) {
                    String extendTemplate = BladePsiUtil.getSection(bladeParameter);
                    if(extendTemplate != null) {
                        VirtualFile virtualFile = BladeTemplateUtil.resolveTemplateName(psiElement.getProject(), extendTemplate);
                        if(virtualFile != null) {

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

    private void collectTemplateFileRelatedFiles(final PsiFile psiFile, @NotNull Collection<LineMarkerInfo> collection) {

        String templateName = BladeTemplateUtil.getFileTemplateName(psiFile.getProject(), psiFile.getVirtualFile());
        if(templateName == null) {
            return;
        }

        final List<GotoRelatedItem> gotoRelatedItems = new ArrayList<GotoRelatedItem>();

        for(ID<String, Void> key : Arrays.asList(BladeExtendsStubIndex.KEY, BladeSectionStubIndex.KEY, BladeIncludeStubIndex.KEY)) {
            FileBasedIndexImpl.getInstance().getFilesWithKey(key, new HashSet<String>(Arrays.asList(templateName)), new Processor<VirtualFile>() {
                @Override
                public boolean process(VirtualFile virtualFile) {
                    PsiFile psiFileTarget = PsiManager.getInstance(psiFile.getProject()).findFile(virtualFile);

                    if(psiFileTarget != null) {
                        gotoRelatedItems.add(new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(psiFileTarget).withIcon(LaravelIcons.LARAVEL, LaravelIcons.LARAVEL));
                    }

                    return true;
                }
            }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(psiFile.getProject()), BladeFileType.INSTANCE));
        }

        if(gotoRelatedItems.size() == 0) {
            return;
        }

        collection.add(getRelatedPopover("Template", "Blade File", psiFile, gotoRelatedItems, PhpIcons.IMPLEMENTED));
    }

    private LineMarkerInfo getRelatedPopover(String singleItemTitle, String singleItemTooltipPrefix, PsiElement lineMarkerTarget, List<GotoRelatedItem> gotoRelatedItems, Icon icon) {

        // single item has no popup
        String title = singleItemTitle;
        if(gotoRelatedItems.size() == 1) {
            String customName = gotoRelatedItems.get(0).getCustomName();
            if(customName != null) {
                title = String.format(singleItemTooltipPrefix, customName);
            }
        }

        return new LineMarkerInfo<PsiElement>(lineMarkerTarget, lineMarkerTarget.getTextOffset(), icon, 6, new ConstantFunction<PsiElement, String>(title), new RelatedPopupGotoLineMarker.NavigationHandler(gotoRelatedItems));
    }

    private void visitOverwrittenTemplateFile(final PsiFile psiFile, final List<GotoRelatedItem> gotoRelatedItems, final String sectionName) {
        visitOverwrittenTemplateFile(psiFile, gotoRelatedItems, sectionName, 10);
    }

    private void visitOverwrittenTemplateFile(final PsiFile psiFile, final List<GotoRelatedItem> gotoRelatedItems, final String sectionName, int depth) {

        // simple secure recursive calls
        if(depth-- <= 0) {
            return;
        }

        BladeTemplateUtil.DirectiveParameterVisitor visitor = new BladeTemplateUtil.DirectiveParameterVisitor() {
            @Override
            public void visit(@NotNull PsiElement psiElement, @NotNull String templateName) {
                if (sectionName.equalsIgnoreCase(templateName)) {
                    gotoRelatedItems.add(new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(psiElement).withIcon(LaravelIcons.LARAVEL, LaravelIcons.LARAVEL));
                }
            }
        };

        BladeTemplateUtil.visitSection(psiFile, visitor);
        BladeTemplateUtil.visitYield(psiFile, visitor);

        final int finalDepth = depth;
        BladeTemplateUtil.visitExtends(psiFile, new BladeTemplateUtil.DirectiveParameterVisitor() {
            @Override
            public void visit(@NotNull PsiElement psiElement, @NotNull String content) {
                VirtualFile virtualFile = BladeTemplateUtil.resolveTemplateName(psiFile.getProject(), content);
                if (virtualFile != null) {
                    PsiFile templatePsiFile = PsiManager.getInstance(psiFile.getProject()).findFile(virtualFile);
                    if (templatePsiFile != null) {
                        visitOverwrittenTemplateFile(templatePsiFile, gotoRelatedItems, sectionName, finalDepth);
                    }
                }
            }
        });

    }


    /**
     * Find all sub implementations of a section that are overwritten by an extends tag
     * Possible targets are: @section('sidebar')
     */
    private void collectImplementsSection(final PsiElement psiElement, @NotNull Collection<LineMarkerInfo> collection, final String sectionName) {

        final String templateName = BladeTemplateUtil.getFileTemplateName(psiElement.getProject(), psiElement.getContainingFile().getVirtualFile());
        if(templateName == null) {
            return;
        }

        final List<GotoRelatedItem> gotoRelatedItems = new ArrayList<GotoRelatedItem>();

        Set<VirtualFile> virtualFiles = BladeTemplateUtil.getExtendsImplementations(psiElement.getProject(), templateName);
        if(virtualFiles.size() == 0) {
            return;
        }

        for(VirtualFile virtualFile: virtualFiles) {
            PsiFile psiFile = PsiManager.getInstance(psiElement.getProject()).findFile(virtualFile);
            if(psiFile != null) {
                BladeTemplateUtil.visitSection(psiFile, new BladeTemplateUtil.SectionVisitor() {
                    @Override
                    public void visit(@NotNull PsiElement psiElement, @NotNull String templateName) {
                        if (sectionName.equalsIgnoreCase(templateName)) {
                            gotoRelatedItems.add(new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(psiElement).withIcon(LaravelIcons.LARAVEL, LaravelIcons.LARAVEL));
                        }
                    }
                });
            }

        }

        if(gotoRelatedItems.size() == 0) {
            return;
        }

        collection.add(getRelatedPopover("Template", "Blade File", psiElement, gotoRelatedItems, PhpIcons.IMPLEMENTED));
    }

    private void collectYieldImplementsSection(final PsiElement psiElement, @NotNull Collection<LineMarkerInfo> collection, final String sectionName) {

        final String templateName = BladeTemplateUtil.getFileTemplateName(psiElement.getProject(), psiElement.getContainingFile().getVirtualFile());
        if(templateName == null) {
            return;
        }

        final List<GotoRelatedItem> gotoRelatedItems = new ArrayList<GotoRelatedItem>();

        Set<VirtualFile> virtualFiles = BladeTemplateUtil.getExtendsImplementations(psiElement.getProject(), templateName);
        if(virtualFiles.size() == 0) {
            return;
        }

        for(VirtualFile virtualFile: virtualFiles) {
            PsiFile psiFile = PsiManager.getInstance(psiElement.getProject()).findFile(virtualFile);
            if(psiFile != null) {
                BladeTemplateUtil.visitYield(psiFile, new BladeTemplateUtil.DirectiveParameterVisitor() {
                    @Override
                    public void visit(@NotNull PsiElement psiElement, @NotNull String templateName) {
                        if (sectionName.equalsIgnoreCase(templateName)) {
                            gotoRelatedItems.add(new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(psiElement).withIcon(LaravelIcons.LARAVEL, LaravelIcons.LARAVEL));
                        }
                    }
                });
            }

        }

        if(gotoRelatedItems.size() == 0) {
            return;
        }

        collection.add(getRelatedPopover("Template", "Blade File", psiElement, gotoRelatedItems, PhpIcons.IMPLEMENTED));
    }

}
