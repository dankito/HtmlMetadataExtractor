package net.dankito.web.htmlmetadata.model

data class WebPageMetadata(
    val sourceUrl: String?,
    val standard: StandardMetadata,
    val openGraph: OpenGraphMetadata,
    val twitter: TwitterCardMetadata,
    val jsonLd: List<JsonLdMetadata> = emptyList(), // a page can have multiple JSON-LD blocks
    val favicons: List<Favicon> = emptyList(),
    val feeds: List<FeedLink> = emptyList(),
)