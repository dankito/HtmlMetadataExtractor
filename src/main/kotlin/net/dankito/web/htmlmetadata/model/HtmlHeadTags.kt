package net.dankito.web.htmlmetadata.model

data class HtmlHeadTags(
    val metaTags: List<MetaTag>,
    val linkTags: List<LinkTag>,
) {

    val metaNameTags: List<MetaNameTag> by lazy { metaTags.filter { it.name != null }.map {
        MetaNameTag(it.name!!, it.content, it)
    } }

    val metaNameTagValues: Map<String, String?> by lazy { metaNameTags.associate { it.name to it.content } }

    val metaPropertyTags: List<MetaPropertyTag> by lazy { metaTags.filter { it.property != null }.map {
        MetaPropertyTag(it.property!!, it.content, it)
    } }

    val metaPropertyTagValues: Map<String, String?> by lazy { metaPropertyTags.associate { it.property to it.content } }

    val linkTagsByRel: Map<String?, LinkTag> by lazy { linkTags.associateBy { it.rel } }


    fun nameMetaValue(name: String) = metaNameTagValues[name]

    fun propertyMetaValue(property: String) = metaPropertyTagValues[property]

    fun linkTag(rel: String): LinkTag? = linkTagsByRel[rel]

}