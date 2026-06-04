package net.dankito.web.htmlmetadata

import net.dankito.web.htmlmetadata.extensions.attrOrNull
import net.dankito.web.htmlmetadata.model.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

open class HtmlMetadataParser(
    protected val openGraphParser: OpenGraphParser = OpenGraphParser(),
    protected val jsonLdParser: JsonLdParser = JsonLdParser(),
) {

    /**
     * Parses HTML and returns metadata.
     *
     * @param html HTML to parse
     * @param sourceUrl URL of the page the HTML was fetched from. Only required to resolve relative URLs to absolute URLs.
     * @return Metadata parsed from the HTML
     */
    open fun parse(html: String, sourceUrl: String? = null): HtmlMetadata {
        val doc = doc(html, sourceUrl)

        return HtmlMetadata(
            sourceUrl = sourceUrl,
            standard = parseStandard(doc),
            openGraph = openGraphParser.parseOpenGraph(doc),
            twitter = parseTwitter(doc),
            jsonLd = jsonLdParser.parseJsonLd(doc),
            favicons = parseFavicons(doc),
            feeds = parseFeeds(doc),
        )
    }


    open fun tryToFindSourceUrl(html: String): String? =
        tryToFindSourceUrl(doc(html))

    open fun tryToFindSourceUrl(doc: Document): String? =
        getCanonicalUrl(doc)
            ?: doc.head().selectFirst("base")?.attrOrNull("href")
            ?: doc.head().selectFirst("meta[property^=og:url]")?.absUrl("content")


    // ── Standard ──────────────────────────────────────────────────────────────

    protected open fun parseStandard(doc: Document): StandardMetadata {
        val keywords = meta(doc, "keywords")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        return StandardMetadata(
            title = doc.title().takeIf { it.isNotBlank() },
            description = meta(doc, "description"),
            keywords = keywords,
            author = meta(doc, "author"),
            robots = meta(doc, "robots"),
            canonicalUrl = getCanonicalUrl(doc),
            language = doc.selectFirst("html")?.attrOrNull("lang"),
        )
    }

    protected open fun getCanonicalUrl(doc: Document): String? =
        doc.selectFirst("link[rel=canonical]")?.absUrl("href")?.takeUnless { it.isBlank() }

    protected open fun meta(doc: Document, name: String) =
        doc.selectFirst("meta[name=$name]")?.attrOrNull("content")


    // ── Twitter Card ──────────────────────────────────────────────────────────

    protected open fun parseTwitter(doc: Document): TwitterCardMetadata {
        // Twitter uses both name= and property= depending on the site
        fun meta(name: String): String? =
            (doc.selectFirst("meta[name=$name]") ?: doc.selectFirst("meta[property=$name]"))
                ?.attrOrNull("content")

        return TwitterCardMetadata(
            card = meta("twitter:card"),
            site = meta("twitter:site"),
            siteId = meta("twitter:site:id"),
            creator = meta("twitter:creator"),
            creatorId = meta("twitter:creator:id"),
            title = meta("twitter:title"),
            description = meta("twitter:description"),
            image = meta("twitter:image"),
            imageAlt = meta("twitter:image:alt"),
        )
    }


    // ── Favicons ──────────────────────────────────────────────────────────────

    protected open fun parseFavicons(doc: Document): List<Favicon> {
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


    open fun parseFeeds(html: String, sourceUrl: String) =
        parseFeeds(doc(html, sourceUrl))

    protected open fun parseFeeds(doc: Document): List<FeedLink> = doc.select("link[rel=alternate][type*=xml]")
        .filter { it.attr("type").let { t -> "rss" in t || "atom" in t } }
        .mapNotNull { el ->
            val href = el.absUrl("href").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            FeedLink(
                url = href,
                type = el.attr("type"),
                title = el.attrOrNull("title"),
            )
        }


    protected open fun doc(html: String, baseUrl: String? = null): Document =
        Jsoup.parse(html, baseUrl ?: "")

}