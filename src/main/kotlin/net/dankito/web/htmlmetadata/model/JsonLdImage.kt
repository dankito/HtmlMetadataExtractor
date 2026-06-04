package net.dankito.web.htmlmetadata.model

data class JsonLdImage(
    val url: String,
    val width: Int? = null,
    val height: Int? = null,
    val caption: String? = null,
)