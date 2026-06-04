package net.dankito.web.htmlmetadata.model

data class WebPageMetadata(
    val sourceUrl: String?,
    val standard: StandardMetadata,
    val openGraph: OpenGraphMetadata,
    val twitter: TwitterCardMetadata,
    val favicons: List<Favicon> = emptyList(),
    val feeds: List<FeedLink> = emptyList(),
)