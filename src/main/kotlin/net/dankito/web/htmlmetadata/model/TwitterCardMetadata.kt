package net.dankito.web.htmlmetadata.model

data class TwitterCardMetadata(
    val card: String? = null,           // "summary", "summary_large_image", "app", "player"
    val site: String? = null,           // @username of website
    val siteId: String? = null,
    val creator: String? = null,        // @username of author
    val creatorId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val image: String? = null,
    val imageAlt: String? = null,
)