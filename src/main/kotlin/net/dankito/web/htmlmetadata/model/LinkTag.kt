package net.dankito.web.htmlmetadata.model

/**
 * Represents an HTML <link> tag.
 * href and rel are the most commonly required pair, but both are technically optional per spec.
 */
data class LinkTag(
    // URL of the linked resource
    val href: String? = null,
    // Relationship type (e.g. "stylesheet", "canonical", "icon") — practically required
    val rel: String? = null,
    // MIME type of the linked resource (e.g. "text/css")
    val type: String? = null,
    // Media query (e.g. "screen", "print")
    val media: String? = null,
    // Language of the linked resource (used with rel="alternate")
    val hreflang: String? = null,
    // Hint for how much to prefetch ("preload" resources)
    val `as`: String? = null,
    // Subresource Integrity hash
    val integrity: String? = null,
    // Cross-origin policy ("anonymous", "use-credentials")
    val crossorigin: String? = null,
    // Fetch priority hint ("high", "low", "auto")
    val fetchpriority: String? = null,
    // Image sizes for rel="icon" (e.g. "32x32")
    val sizes: String? = null,
    // Whether to block rendering until resource is loaded
    val blocking: String? = null,
    // Referrer policy for the fetch
    val referrerpolicy: String? = null,
    // Color hint for rel="mask-icon" (Safari)
    val color: String? = null,
    // Title of the linked resource (used with alternate stylesheets)
    val title: String? = null,
    // Disabled state (only relevant for stylesheet links)
    val disabled: Boolean = false,
    // Whether link is marked as imagesrcset
    val imagesrcset: String? = null,
    val imagesizes: String? = null,
) {
    override fun toString() = "$rel $type $title $href"
}