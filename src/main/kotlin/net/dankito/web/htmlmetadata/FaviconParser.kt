package net.dankito.web.htmlmetadata

import net.dankito.web.htmlmetadata.extensions.attrOrNull
import net.dankito.web.htmlmetadata.model.Favicon
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

open class FaviconParser {

    open fun parseFavicons(html: String, baseUrl: String): List<Favicon> =
        parseFavicons(Jsoup.parse(html, baseUrl))

    open fun parseFavicons(doc: Document): List<Favicon> {
        val rels = setOf(
            "icon", "shortcut icon",
            "apple-touch-icon", "apple-touch-icon-precomposed",
            "mask-icon",         // Safari pinned tab SVG
            "fluid-icon",        // Fluid app
        )

        return doc.select("link[rel]")
            .filter { el -> el.attr("rel").lowercase() in rels }
            .mapNotNull { el ->
                val href = el.absUrl("href").takeUnless { it.isBlank() } ?: return@mapNotNull null
                Favicon(
                    href = href,
                    rel = el.attr("rel").lowercase(),
                    sizes = el.attrOrNull("sizes"),
                    type = el.attrOrNull("type"),
                )
            }
    }

}