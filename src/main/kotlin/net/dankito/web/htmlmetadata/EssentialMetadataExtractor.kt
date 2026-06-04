package net.dankito.web.htmlmetadata

import net.dankito.web.htmlmetadata.model.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Distills [WebPageMetadata] into [EssentialPageMetadata] using a
 * priority-based resolution strategy for each field.
 */
open class EssentialMetadataExtractor(
    protected val metadataParser: HtmlMetadataParser = HtmlMetadataParser(),
) {

    open fun extract(html: String, sourceUrl: String? = null): EssentialPageMetadata =
        extract(metadataParser.parse(html, sourceUrl))

    open fun extract(metadata: WebPageMetadata): EssentialPageMetadata {
        // Prefer the first JSON-LD block that looks like an article/content type
        val jsonLd = metadata.jsonLd.firstOrNull { it.type?.contains("article", ignoreCase = true) == true }
            ?: metadata.jsonLd.firstOrNull { it.type?.contains("blog", ignoreCase = true) == true }
            ?: metadata.jsonLd.firstOrNull()

        return EssentialPageMetadata(
            sourceUrl = metadata.sourceUrl,
            title = resolveTitle(metadata, jsonLd),
            description = resolveDescription(metadata, jsonLd),
            imageUrl = resolveImageUrl(metadata, jsonLd),
            imageAlt = metadata.openGraph.images.firstOrNull()?.alt,
            author = resolveAuthor(metadata, jsonLd),
            publishedAt = resolveDate(
                metadata.openGraph.articlePublishedTime,
                jsonLd?.datePublished,
            ),
            updatedAt = resolveDate(
                metadata.openGraph.articleModifiedTime,
                jsonLd?.dateModified,
            ),
            type = metadata.openGraph.type ?: jsonLd?.type,
            section = metadata.openGraph.articleSection ?: jsonLd?.articleSection,
            tags = resolveTags(metadata, jsonLd),
            siteName = metadata.openGraph.siteName ?: jsonLd?.publisher?.name,
            canonicalUrl = metadata.standard.canonicalUrl ?: metadata.openGraph.url,
            language = metadata.standard.language,
            locale = metadata.openGraph.locale,
            faviconUrl = resolveFavicon(metadata),
        )
    }

    // ── Field resolvers ───────────────────────────────────────────────────────

    protected open  fun resolveTitle(raw: WebPageMetadata, jsonLd: JsonLdMetadata?): String? =
        raw.openGraph.title
            ?: jsonLd?.headline
            ?: raw.standard.title

    protected open  fun resolveDescription(raw: WebPageMetadata, jsonLd: JsonLdMetadata?): String? =
        raw.openGraph.description
            ?: raw.standard.description
            ?: jsonLd?.description

    protected open  fun resolveImageUrl(raw: WebPageMetadata, jsonLd: JsonLdMetadata?): String? =
        raw.openGraph.images.firstOrNull()?.url
            ?: jsonLd?.images?.firstOrNull()?.url

    protected open  fun resolveAuthor(raw: WebPageMetadata, jsonLd: JsonLdMetadata?): String? {
        // JSON-LD authors as comma-joined names if multiple
        val jsonLdAuthor = jsonLd?.authors
            ?.mapNotNull { it.name }
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(", ")

        return jsonLdAuthor
            ?: raw.openGraph.articleAuthor.firstOrNull()
            ?: raw.standard.author
    }

    protected open  fun resolveTags(raw: WebPageMetadata, jsonLd: JsonLdMetadata?): List<String> {
        val tags = (raw.openGraph.articleTags + (jsonLd?.keywords ?: emptyList()))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
        return tags
        // Note: we intentionally skip meta[keywords] as it's unreliable
    }

    protected open  fun resolveFavicon(raw: WebPageMetadata): String? {
        val favicons = raw.favicons
        if (favicons.isEmpty()) return null

        // Prefer SVG (scalable), then apple-touch-icon (usually 180x180),
        // then largest sized icon, then any icon
        return favicons.firstOrNull { it.type == "image/svg+xml" }?.href
            ?: favicons.firstOrNull { it.rel == "apple-touch-icon" }?.href
            ?: favicons.maxByOrNull { it.sizes?.substringBefore("x")?.toIntOrNull() ?: 0 }?.href
            ?: favicons.first().href
    }

    protected open  fun resolveDate(vararg candidates: String?): OffsetDateTime? {
        for (candidate in candidates) {
            if (candidate.isNullOrBlank()) continue
            val parsed = tryParseDate(candidate)
            if (parsed != null) return parsed
        }
        return null
    }

    protected open  fun tryParseDate(value: String): OffsetDateTime? {
        val formats = listOf(
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ISO_DATE,
        )
        for (fmt in formats) {
            try {
                return OffsetDateTime.parse(value, fmt)
            } catch (_: DateTimeParseException) {
                // try next
            }
        }
        return null
    }

}