package de.espend.idea.laravel.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import de.espend.idea.laravel.LaravelSettings;
import de.espend.idea.laravel.ui.LaravelProjectSettingsForm;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class IdeHelper {
    public static void notifyEnableMessage(final Project project) {
        Notification notification = new Notification("Laravel Plugin", "Laravel Plugin", "Enable the Laravel Plugin <a href=\"enable\">with auto configuration now</a>, open <a href=\"config\">Project Settings</a> or <a href=\"dismiss\">dismiss</a> further messages", NotificationType.INFORMATION, (notification1, event) -> {
            // handle html click events
            if("config".equals(event.getDescription())) {

                // open settings dialog and show panel
                LaravelProjectSettingsForm.show(project);
            } else if("enable".equals(event.getDescription())) {
                enablePluginAndConfigure(project);
                Notifications.Bus.notify(new Notification("Laravel Plugin", "Laravel Plugin", "Plugin enabled", NotificationType.INFORMATION), project);
            } else if("dismiss".equals(event.getDescription())) {
                // user dont want to show notification again
                LaravelSettings.getInstance(project).dismissEnableNotification = true;
            }

            notification1.expire();
        });

        Notifications.Bus.notify(notification, project);
    }

    private static void enablePluginAndConfigure(@NotNull Project project) {
        LaravelSettings.getInstance(project).pluginEnabled = true;
    }
}
