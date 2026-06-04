package net.dankito.web.htmlmetadata.model

data class OpenGraphImage(
    val url: String,
    val secureUrl: String? = null,
    val type: String? = null,   // mime type
    val width: Int? = null,
    val height: Int? = null,
    val alt: String? = null,
)