package net.dankito.web.htmlmetadata

import net.dankito.web.htmlmetadata.model.HtmlHeadTags
import net.dankito.web.htmlmetadata.model.LinkTag
import net.dankito.web.htmlmetadata.model.MetaTag
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class HtmlHeadTagExtractor {

    fun extract(html: String, baseUrl: String? = null) =
        extract(if (baseUrl == null) Jsoup.parse(html) else Jsoup.parse(html, baseUrl))

    fun extract(doc: Document): HtmlHeadTags {

        // do not use doc.head(), will not work with real life HTML that e.g. contains <span> elements in <head>

        val metaTags = doc.select("meta").map { el ->
            MetaTag(
                name = el.attr("name").takeIf { it.isNotEmpty() },
                httpEquiv = el.attr("http-equiv").takeIf { it.isNotEmpty() },
                content = el.attr("content").takeIf { it.isNotEmpty() },
                charset = el.attr("charset").takeIf { it.isNotEmpty() },
                property = el.attr("property").takeIf { it.isNotEmpty() },
                media = el.attr("media").takeIf { it.isNotEmpty() },
                referrerpolicy = el.attr("referrerpolicy").takeIf { it.isNotEmpty() },
            )
        }

        val linkTags = doc.select("link").map { el ->
            LinkTag(
                href = el.absUrl("href").takeIf { it.isNotEmpty() },
                rel = el.attr("rel").takeIf { it.isNotEmpty() },
                type = el.attr("type").takeIf { it.isNotEmpty() },
                media = el.attr("media").takeIf { it.isNotEmpty() },
                hreflang = el.attr("hreflang").takeIf { it.isNotEmpty() },
                `as` = el.attr("as").takeIf { it.isNotEmpty() },
                integrity = el.attr("integrity").takeIf { it.isNotEmpty() },
                crossorigin = el.attr("crossorigin").takeIf { it.isNotEmpty() },
                fetchpriority = el.attr("fetchpriority").takeIf { it.isNotEmpty() },
                sizes = el.attr("sizes").takeIf { it.isNotEmpty() },
                blocking = el.attr("blocking").takeIf { it.isNotEmpty() },
                referrerpolicy = el.attr("referrerpolicy").takeIf { it.isNotEmpty() },
                color = el.attr("color").takeIf { it.isNotEmpty() },
                title = el.attr("title").takeIf { it.isNotEmpty() },
                disabled = el.hasAttr("disabled"),
                imagesrcset = el.attr("imagesrcset").takeIf { it.isNotEmpty() },
                imagesizes = el.attr("imagesizes").takeIf { it.isNotEmpty() },
            )
        }

        return HtmlHeadTags(metaTags, linkTags)
    }

}