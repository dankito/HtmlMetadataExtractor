package net.dankito.web.htmlmetadata.model

data class MetaNameTag(
    val name: String,
    val content: String?,
    val tag: MetaTag,
) {
    override fun toString() = "$name: $content"
}
