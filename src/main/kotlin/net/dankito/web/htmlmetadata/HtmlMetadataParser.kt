package net.dankito.web.htmlmetadata

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.dankito.web.htmlmetadata.extensions.attrOrNull
import net.dankito.web.htmlmetadata.model.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class HtmlMetadataParser(
    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerKotlinModule()
        findAndRegisterModules()
    },
) {

    fun parse(html: String, sourceUrl: String? = null): WebPageMetadata {
        val doc = doc(html, sourceUrl)

        return WebPageMetadata(
            sourceUrl = sourceUrl,
            standard = parseStandard(doc),
            openGraph = parseOpenGraph(doc),
            twitter = parseTwitter(doc),
            jsonLd = parseJsonLd(doc),
            favicons = parseFavicons(doc),
            feeds = parseFeeds(doc),
        )
    }


    fun tryToFindUrl(html: String): String? {
        val doc = doc(html)

        return getCanonicalUrl(doc)
            ?: doc.head().selectFirst("base")?.attrOrNull("href")
            ?: doc.head().selectFirst("meta[property^=og:url]")?.absUrl("content")
    }


    // ── Standard ──────────────────────────────────────────────────────────────

    private fun parseStandard(doc: Document): StandardMetadata {
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

    private fun getCanonicalUrl(doc: Document): String? =
        doc.selectFirst("link[rel=canonical]")?.absUrl("href")?.takeUnless { it.isBlank() }

    private fun meta(doc: Document, name: String) =
        doc.selectFirst("meta[name=$name]")?.attrOrNull("content")

    // ── Open Graph ────────────────────────────────────────────────────────────

    private fun parseOpenGraph(doc: Document): OpenGraphMetadata {
        // Collect all og: meta tags into a multimap (property -> list of values)
        // We need a list because og:image, og:image:width etc. can repeat
        val props = mutableMapOf<String, MutableList<String>>()
        doc.select("meta[property^=og:], meta[property^=article:], meta[property^=profile:]")
            .forEach { el ->
                val key = el.attr("property").lowercase()
                val value = el.attr("content")
                if (value.isNotBlank()) {
                    props.getOrPut(key) { mutableListOf() }.add(value)
                }
            }

        fun first(key: String) = props[key]?.firstOrNull()
        fun all(key: String) = props[key] ?: emptyList()

        // Parse grouped og:image blocks (each og:image starts a new group)
        val images = buildList {
            var current: MutableMap<String, String>? = null
            doc.select("meta[property^=og:image]").forEach { el ->
                val prop = el.attr("property").lowercase()
                val value = el.attr("content")
                if (prop == "og:image") {
                    current?.let { add(it.toOpenGraphImage()) }
                    current = mutableMapOf("url" to value)
                } else {
                    current?.put(prop.removePrefix("og:image:"), value)
                }
            }
            current?.let { add(it.toOpenGraphImage()) }
        }

        val videos = buildList {
            var current: MutableMap<String, String>? = null
            doc.select("meta[property^=og:video]").forEach { el ->
                val prop = el.attr("property").lowercase()
                val value = el.attr("content")
                if (prop == "og:video") {
                    current?.let { add(it.toOpenGraphVideo()) }
                    current = mutableMapOf("url" to value)
                } else {
                    current?.put(prop.removePrefix("og:video:"), value)
                }
            }
            current?.let { add(it.toOpenGraphVideo()) }
        }

        val audios = buildList {
            var current: MutableMap<String, String>? = null
            doc.select("meta[property^=og:audio]").forEach { el ->
                val prop = el.attr("property").lowercase()
                val value = el.attr("content")
                if (prop == "og:audio") {
                    current?.let { add(it.toOpenGraphAudio()) }
                    current = mutableMapOf("url" to value)
                } else {
                    current?.put(prop.removePrefix("og:audio:"), value)
                }
            }
            current?.let { add(it.toOpenGraphAudio()) }
        }

        return OpenGraphMetadata(
            title = first("og:title"),
            description = first("og:description"),
            url = first("og:url"),
            siteName = first("og:site_name"),
            locale = first("og:locale"),
            localeAlternates = all("og:locale:alternate"),
            type = first("og:type"),
            images = images,
            videos = videos,
            audios = audios,
            articleAuthor = all("article:author"),
            articlePublishedTime = first("article:published_time"),
            articleModifiedTime = first("article:modified_time"),
            articleExpirationTime = first("article:expiration_time"),
            articleSection = first("article:section"),
            articleTags = all("article:tag"),
            profileFirstName = first("profile:first_name"),
            profileLastName = first("profile:last_name"),
            profileUsername = first("profile:username"),
        )
    }

    private fun Map<String, String>.toOpenGraphImage() = OpenGraphImage(
        url = get("url") ?: "",
        secureUrl = get("secure_url"),
        type = get("type"),
        width = get("width")?.toIntOrNull(),
        height = get("height")?.toIntOrNull(),
        alt = get("alt"),
    )

    private fun Map<String, String>.toOpenGraphVideo() = OpenGraphVideo(
        url = get("url") ?: "",
        secureUrl = get("secure_url"),
        type = get("type"),
        width = get("width")?.toIntOrNull(),
        height = get("height")?.toIntOrNull(),
    )

    private fun Map<String, String>.toOpenGraphAudio() = OpenGraphAudio(
        url = get("url") ?: "",
        secureUrl = get("secure_url"),
        type = get("type"),
    )

    // ── Twitter Card ──────────────────────────────────────────────────────────

    private fun parseTwitter(doc: Document): TwitterCardMetadata {
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

    // ── JSON-LD ───────────────────────────────────────────────────────────────

    private fun parseJsonLd(doc: Document): List<JsonLdMetadata> {
        return doc.select("script[type=application/ld+json]")
            .mapNotNull { el ->
                runCatching { objectMapper.readTree(el.data()) }.getOrNull()
            }
            .flatMap { node ->
                // Support both single object and @graph arrays
                when {
                    node.has("@graph") -> node["@graph"].toList()
                    node.isArray -> node.toList()
                    else -> listOf(node)
                }
            }
            .mapNotNull { node -> parseJsonLdNode(node) }
    }

    private fun parseJsonLdNode(node: JsonNode): JsonLdMetadata? {
        val type = node["@type"]?.asText() ?: return null

        fun str(key: String) = node[key]?.asText()?.takeIf { it.isNotBlank() }

        // author can be a single object or an array
        val authors = when {
            node["author"]?.isArray == true ->
                node["author"].mapNotNull { it["name"]?.asText()?.let { n -> JsonLdPerson(name = n, url = it["url"]?.asText()) } }
            node["author"] != null ->
                listOfNotNull(node["author"]["name"]?.asText()?.let { JsonLdPerson(name = it, url = node["author"]["url"]?.asText()) })
            else -> emptyList()
        }

        val publisher = node["publisher"]?.let { p ->
            JsonLdOrganization(
                name = p["name"]?.asText(),
                url = p["url"]?.asText(),
                logoUrl = p["logo"]?.let { l -> if (l.isTextual) l.asText() else l["url"]?.asText() },
            )
        }

        // image: string | ImageObject | array thereof
        val images = when {
            node["image"] == null -> emptyList()
            node["image"].isTextual -> listOf(JsonLdImage(url = node["image"].asText()))
            node["image"].isArray -> node["image"].mapNotNull { img ->
                when {
                    img.isTextual -> JsonLdImage(url = img.asText())
                    img.has("url") -> JsonLdImage(
                        url = img["url"].asText(),
                        width = img["width"]?.asInt(),
                        height = img["height"]?.asInt(),
                        caption = img["caption"]?.asText(),
                    )
                    else -> null
                }
            }
            node["image"].has("url") -> listOf(
                JsonLdImage(
                    url = node["image"]["url"].asText(),
                    width = node["image"]["width"]?.asInt(),
                    height = node["image"]["height"]?.asInt(),
                    caption = node["image"]["caption"]?.asText(),
                )
            )
            else -> emptyList()
        }

        val keywords = str("keywords")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        return JsonLdMetadata(
            type = type,
            headline = str("headline"),
            description = str("description"),
            url = str("url"),
            datePublished = str("datePublished"),
            dateModified = str("dateModified"),
            authors = authors,
            publisher = publisher,
            images = images,
            keywords = keywords,
            articleSection = str("articleSection"),
            inLanguage = str("inLanguage"),
        )
    }

    // ── Favicons ──────────────────────────────────────────────────────────────

    private fun parseFavicons(doc: Document): List<Favicon> {
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


    fun parseFeeds(html: String, sourceUrl: String) =
        parseFeeds(doc(html, sourceUrl))

    private fun parseFeeds(doc: Document): List<FeedLink> = doc.select("link[rel=alternate][type*=xml]")
        .filter { it.attr("type").let { t -> "rss" in t || "atom" in t } }
        .mapNotNull { el ->
            val href = el.absUrl("href").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            FeedLink(
                url = href,
                type = el.attr("type"),
                title = el.attrOrNull("title"),
            )
        }


    private fun doc(html: String, baseUrl: String? = null): Document =
        Jsoup.parse(html, baseUrl ?: "")

}