package net.dankito.web.htmlmetadata.model

data class OpenGraphMetadata(
    // Basic
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val siteName: String? = null,
    val locale: String? = null,         // e.g. "en_US"
    val localeAlternates: List<String> = emptyList(),
    val type: String? = null,           // "article", "website", "video.movie", …

    // Media
    val images: List<OpenGraphImage> = emptyList(),
    val videos: List<OpenGraphVideo> = emptyList(),
    val audios: List<OpenGraphAudio> = emptyList(),

    // Article-specific
    val articleAuthor: List<String> = emptyList(),
    val articlePublishedTime: String? = null,
    val articleModifiedTime: String? = null,
    val articleExpirationTime: String? = null,
    val articleSection: String? = null,
    val articleTags: List<String> = emptyList(),

    // Profile-specific
    val profileFirstName: String? = null,
    val profileLastName: String? = null,
    val profileUsername: String? = null,
)