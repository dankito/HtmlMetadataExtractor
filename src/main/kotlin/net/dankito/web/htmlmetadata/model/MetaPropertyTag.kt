package net.dankito.web.htmlmetadata.model

data class MetaPropertyTag(
    val property: String,
    val content: String,
    val tag: MetaTag,
) {
    override fun toString() = "$property: $content"
}
