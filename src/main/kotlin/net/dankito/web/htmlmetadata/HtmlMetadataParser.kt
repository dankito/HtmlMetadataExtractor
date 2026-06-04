package net.dankito.web.htmlmetadata

import net.dankito.web.htmlmetadata.extensions.attrOrNull
import net.dankito.web.htmlmetadata.model.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

open class HtmlMetadataParser(
    protected val tagExtractor: HtmlHeadTagExtractor = HtmlHeadTagExtractor(),
    protected val openGraphExtractor: OpenGraphExtractor = OpenGraphExtractor(),
    protected val jsonLdParser: JsonLdParser = JsonLdParser(),
    protected val faviconExtractor: FaviconExtractor = FaviconExtractor(),
    protected val rssFeedExtractor: RssFeedExtractor = RssFeedExtractor(),
) {

    /**
     * Parses HTML and returns its metadata.
     *
     * @param html HTML to parse
     * @param sourceUrl URL of the page the HTML was fetched from. Only required to resolve relative URLs to absolute URLs.
     * @return Metadata parsed from the HTML
     */
    open fun parse(html: String, sourceUrl: String? = null): HtmlMetadata =
        parse(doc(html, sourceUrl), sourceUrl)

    open fun parse(doc: Document, sourceUrl: String? = null): HtmlMetadata {
        val tags = tagExtractor.extract(doc)

        return HtmlMetadata(
            sourceUrl = sourceUrl,
            standard = parseStandard(doc, tags),
            openGraph = openGraphExtractor.extractOpenGraph(doc),
            twitter = parseTwitter(tags),
            jsonLd = jsonLdParser.parseJsonLd(doc),
            // couldn't figure it out but using HtmlHeadTags for favicons did not work
            favicons = faviconExtractor.extractFavicons(doc),
            feeds = rssFeedExtractor.extractFeeds(doc),
        )
    }


    open fun tryToFindSourceUrl(html: String): String? =
        tryToFindSourceUrl(doc(html))

    open fun tryToFindSourceUrl(doc: Document): String? =
        getCanonicalUrl(doc)
            ?: doc.head().selectFirst("base")?.attrOrNull("href")
            ?: doc.head().selectFirst("meta[property^=og:url]")?.absUrl("content")


    // ── Standard ──────────────────────────────────────────────────────────────

    protected open fun parseStandard(doc: Document, tags: HtmlHeadTags): StandardMetadata {
        val keywords = tags.nameMetaValue("keywords")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        return StandardMetadata(
            title = doc.title().takeIf { it.isNotBlank() },
            description = tags.nameMetaValue("description"),
            keywords = keywords,
            author = tags.nameMetaValue("author"),
            robots = tags.nameMetaValue("robots"),
            canonicalUrl = getCanonicalUrl(doc),
            language = doc.selectFirst("html")?.attrOrNull("lang"),
        )
    }

    protected open fun getCanonicalUrl(doc: Document): String? =
        doc.selectFirst("link[rel=canonical]")?.absUrl("href")?.takeUnless { it.isBlank() }


    // ── Twitter Card ──────────────────────────────────────────────────────────

    protected open fun parseTwitter(tags: HtmlHeadTags): TwitterCardMetadata {
        // Twitter uses both name= and property= depending on the site
        fun meta(name: String): String? =
            tags.nameMetaValue(name) ?: tags.propertyMetaValue(name)

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


    protected open fun doc(html: String, baseUrl: String? = null): Document =
        Jsoup.parse(html, baseUrl ?: "")

}