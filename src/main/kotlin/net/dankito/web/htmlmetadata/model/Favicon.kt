package net.dankito.web.htmlmetadata.model

data class Favicon(
    val href: String,
    val rel: String,          // "icon", "apple-touch-icon", "apple-touch-icon-precomposed", etc.
    val sizes: String? = null,
    val type: String? = null, // "image/png", "image/svg+xml", etc.
)