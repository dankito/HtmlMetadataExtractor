package net.dankito.web.htmlmetadata.model

data class StandardMetadata(
    val title: String? = null,          // <title> tag
    val description: String? = null,
    val keywords: List<String> = emptyList(),
    val author: String? = null,
    val robots: String? = null,
    val canonicalUrl: String? = null,   // <link rel="canonical">
    val language: String? = null,       // <html lang="...">
)