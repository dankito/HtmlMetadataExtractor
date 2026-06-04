package net.dankito.web.htmlmetadata.model

data class FeedLink(
    val url: String,
    val type: String,   // "application/rss+xml" or "application/atom+xml"
    val title: String?, // e.g. "Comments for this post", "Technology News"
)