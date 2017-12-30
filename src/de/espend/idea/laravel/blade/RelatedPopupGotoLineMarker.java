package de.espend.idea.laravel.blade;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class RelatedPopupGotoLineMarker {

    public static class NavigationHandler implements GutterIconNavigationHandler<PsiElement> {

        private Collection<GotoRelatedItem> items;

        public NavigationHandler(Collection<GotoRelatedItem> items){
            this.items = items;
        }

        public void navigate(MouseEvent e, PsiElement elt) {
            Collection<GotoRelatedItem>  items = this.items;
            if (items.size() == 1) {
                items.iterator().next().navigate();
            } else {
                NavigationUtil.getRelatedItemsPopup(new ArrayList<>(items), "Go to Related Files").show(new RelativePoint(e));
            }
        }
    }

    public static class PopupGotoRelatedItem extends GotoRelatedItem {

        private String customName;
        private Icon icon;
        private Icon smallIcon;

        public PopupGotoRelatedItem(@NotNull PsiElement element) {
            super(element);
        }

        public PopupGotoRelatedItem(@NotNull PsiElement element, String customName) {
            super(element);
            this.customName = customName;
        }

        @Nullable
        @Override
        public String getCustomName() {
            return customName;
        }

        @Nullable
        @Override
        public Icon getCustomIcon() {
            if(this.icon != null) {
                return this.icon;
            }

            return super.getCustomIcon();
        }

        public PopupGotoRelatedItem withIcon(Icon icon, Icon smallIcon) {
            this.icon = icon;
            this.smallIcon = smallIcon;
            return this;
        }

        public Icon getSmallIcon() {
            return smallIcon;
        }

    }

}
