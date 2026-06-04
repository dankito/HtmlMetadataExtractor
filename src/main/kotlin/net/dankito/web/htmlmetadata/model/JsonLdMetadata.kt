package net.dankito.web.htmlmetadata.model

data class JsonLdMetadata(
    val type: String? = null,           // @type: "Article", "NewsArticle", "BlogPosting", …
    val headline: String? = null,
    val description: String? = null,
    val url: String? = null,
    val datePublished: String? = null,
    val dateModified: String? = null,
    val authors: List<JsonLdPerson> = emptyList(),
    val publisher: JsonLdOrganization? = null,
    val images: List<JsonLdImage> = emptyList(),
    val keywords: List<String> = emptyList(),
    val articleSection: String? = null,
    val inLanguage: String? = null,
)