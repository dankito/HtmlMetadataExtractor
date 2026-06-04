package net.dankito.web.htmlmetadata

import net.dankito.web.htmlmetadata.extensions.attrOrNull
import net.dankito.web.htmlmetadata.model.FeedLink
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

open class RssFeedExtractor {

    open fun extractFeeds(html: String, sourceUrl: String) =
        extractFeeds(Jsoup.parse(html, sourceUrl))

    open fun extractFeeds(doc: Document): List<FeedLink> = doc.select("link[rel=alternate][type*=xml]")
        .filter { it.attr("type").let { t -> "rss" in t || "atom" in t } }
        .mapNotNull { el ->
            val href = el.absUrl("href").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            FeedLink(
                url = href,
                type = el.attr("type"),
                title = el.attrOrNull("title"),
            )
        }

}