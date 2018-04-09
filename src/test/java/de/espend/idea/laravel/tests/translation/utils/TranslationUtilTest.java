package de.espend.idea.laravel.tests.translation.utils;

import de.espend.idea.laravel.tests.LaravelLightCodeInsightFixtureTestCase;
import de.espend.idea.laravel.translation.utils.TranslationUtil;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.laravel.translation.utils.TranslationUtil
 */
public class TranslationUtilTest extends LaravelLightCodeInsightFixtureTestCase {
    /**
     * @see TranslationUtil#getNamespaceFromFilePath
     */
    public void testGetNamespaceFromFilePath() {
        assertEquals("messages", TranslationUtil.getNamespaceFromFilePath("app/lang/fr_FR/messages.php"));
        assertEquals("messages", TranslationUtil.getNamespaceFromFilePath("app/lang/fr/messages.php"));
        assertEquals("messages", TranslationUtil.getNamespaceFromFilePath("app/lang/packages/en/hearthfire/messages.php"));
        assertEquals("messages", TranslationUtil.getNamespaceFromFilePath("app/lang/packages/fr_FR/hearthfire/messages.php"));
        assertEquals("hearthfire/messages", TranslationUtil.getNamespaceFromFilePath("app/lang/packages/fr_FR/admin/hearthfire/messages.php"));

        assertEquals("messages", TranslationUtil.getNamespaceFromFilePath("app/lang/packages/fr-FR/hearthfire/messages.php"));
        assertEquals("messages", TranslationUtil.getNamespaceFromFilePath("app/lang/fr-FR/messages.php"));
        assertEquals("mess-ages", TranslationUtil.getNamespaceFromFilePath("app/lang/fr-FR/mess-ages.php"));
    }
}
