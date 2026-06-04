package net.dankito.web.htmlmetadata.model

/**
 * Represents an HTML <meta> tag.
 * All attributes are optional per spec; at least one of name/httpEquiv/charset/property should be present.
 *
 * In practice a <meta> tag with none of name, property, charset, or http-equiv is meaningless — but the spec won't reject it.
 */
data class MetaTag(
    // Metadata name (e.g. "description", "viewport")
    val name: String? = null,
    // HTTP header equivalent (e.g. "content-type", "refresh")
    val httpEquiv: String? = null,
    // The actual value — required when name or httpEquiv is set
    val content: String? = null,
    // Declares the character encoding (e.g. "UTF-8"); standalone, no content needed
    val charset: String? = null,
    // Open Graph / custom property (e.g. "og:title")
    val property: String? = null,
    // Which media/device the metadata applies to
    val media: String? = null,
    // Referrer policy shorthand via meta
    val referrerpolicy: String? = null,
) {
    override fun toString() = "$name $httpEquiv $content $property"
}