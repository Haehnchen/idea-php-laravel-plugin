package de.espend.idea.laravel.tests.asset;

import de.espend.idea.laravel.asset.AssetUtil;
import de.espend.idea.laravel.tests.LaravelTempCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see de.espend.idea.laravel.asset.AssetUtil
 */
public class AssetUtilTempTest extends LaravelTempCodeInsightFixtureTestCase {
    /**
     * @see AssetUtil#getLookupElements
     */
    public void testGetLookupElements() {
        createFiles(".htaccess", "public/foo/bar.css", "public/bar.js");

        assertEquals(2, AssetUtil.getLookupElements(getProject()).size());

        assertNotNull(AssetUtil.getLookupElements(getProject())
            .stream()
            .filter(lookupElement -> "foo/bar.css".equals(lookupElement.getLookupString()))
            .findFirst()
            .orElseGet(null)
        );
    }

    /**
     * @see AssetUtil#resolveAsset
     */
    public void testResolveAsset() {
        createFiles("public/foo/bar.css", "public/bar.js");

        assertNotNull(AssetUtil.resolveAsset(getProject(), "foo\\bar.css")
            .stream()
            .filter(virtualFile -> "bar.css".equals(virtualFile.getName()))
            .findFirst()
            .orElseGet(null)
        );

        assertNotNull(AssetUtil.resolveAsset(getProject(), "bar.js")
            .stream()
            .filter(virtualFile -> "bar.js".equals(virtualFile.getName()))
            .findFirst()
            .orElseGet(null)
        );
    }
}
