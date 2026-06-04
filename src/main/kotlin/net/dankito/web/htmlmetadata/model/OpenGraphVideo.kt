package net.dankito.web.htmlmetadata.model

data class OpenGraphVideo(
    val url: String,
    val secureUrl: String? = null,
    val type: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)