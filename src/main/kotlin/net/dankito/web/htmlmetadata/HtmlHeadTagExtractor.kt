package net.dankito.web.htmlmetadata

import net.dankito.web.htmlmetadata.extensions.attrOrNull
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
                name = el.attrOrNull("name"),
                httpEquiv = el.attrOrNull("http-equiv"),
                content = el.attrOrNull("content"),
                charset = el.attrOrNull("charset"),
                property = el.attrOrNull("property"),
                media = el.attrOrNull("media"),
                referrerpolicy = el.attrOrNull("referrerpolicy"),
            )
        }

        val linkTags = doc.select("link").map { el ->
            LinkTag(
                href = el.absUrl("href"),
                rel = el.attrOrNull("rel"),
                type = el.attrOrNull("type"),
                media = el.attrOrNull("media"),
                hreflang = el.attrOrNull("hreflang"),
                `as` = el.attrOrNull("as"),
                integrity = el.attrOrNull("integrity"),
                crossorigin = el.attrOrNull("crossorigin"),
                fetchpriority = el.attrOrNull("fetchpriority"),
                sizes = el.attrOrNull("sizes"),
                blocking = el.attrOrNull("blocking"),
                referrerpolicy = el.attrOrNull("referrerpolicy"),
                color = el.attrOrNull("color"),
                title = el.attrOrNull("title"),
                disabled = el.hasAttr("disabled"),
                imagesrcset = el.attrOrNull("imagesrcset"),
                imagesizes = el.attrOrNull("imagesizes"),
            )
        }

        return HtmlHeadTags(metaTags, linkTags)
    }

}