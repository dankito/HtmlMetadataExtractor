package net.dankito.web.htmlmetadata.model

import java.time.OffsetDateTime

data class EssentialPageMetadata(
    val sourceUrl: String?,

    // ── Display ───────────────────────────────────────────────────────────────
    /** Best available title for the page. */
    val title: String?,

    /** Short teaser/description for list previews. */
    val description: String?,

    /**
     * Primary image URL (og:image / JSON-LD image).
     * Use this as the hero image on the detail screen.
     * Tip: store the URL and let your backend produce thumbnails on demand
     * rather than relying on separate metadata-provided thumbnail URLs,
     * since sites rarely expose a dedicated small variant.
     */
    val imageUrl: String?,
    val imageAlt: String?,              // og:image:alt if available

    // ── Authorship & dates ────────────────────────────────────────────────────
    val author: String?,
    val publishedAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,

    // ── Classification ────────────────────────────────────────────────────────
    /** "article", "website", "video.movie", … – from og:type */
    val type: String?,
    /** Editorial section, e.g. "Technology" */
    val section: String?,
    /** Topic tags from article:tag and/or JSON-LD keywords */
    val tags: List<String> = emptyList(),

    // ── Site ──────────────────────────────────────────────────────────────────
    val siteName: String?,
    /** Canonical URL – prefer this over the URL you fetched from */
    val canonicalUrl: String?,
    /** From <html lang="…">, e.g. "en", "de", "en-US" */
    val language: String?,
    /**
     * From og:locale, e.g. "en_US", "de_DE".
     * Different from [language]: includes region and uses underscore separator.
     * Store both – they answer different questions.
     */
    val locale: String?,

    // ── Favicon ───────────────────────────────────────────────────────────────
    /** Best favicon URL (highest-res or SVG, resolved to absolute URL) */
    val faviconUrl: String?,
)
