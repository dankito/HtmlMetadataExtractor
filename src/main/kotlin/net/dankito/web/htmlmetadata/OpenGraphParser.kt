package net.dankito.web.htmlmetadata

import net.dankito.web.htmlmetadata.model.OpenGraphAudio
import net.dankito.web.htmlmetadata.model.OpenGraphImage
import net.dankito.web.htmlmetadata.model.OpenGraphMetadata
import net.dankito.web.htmlmetadata.model.OpenGraphVideo
import org.jsoup.nodes.Document

open class OpenGraphParser {

    open fun parseOpenGraph(doc: Document): OpenGraphMetadata {
        // Collect all og: meta tags into a multimap (property -> list of values)
        // We need a list because og:image, og:image:width etc. can repeat
        val props = mutableMapOf<String, MutableList<String>>()
        doc.select("meta[property^=og:], meta[property^=article:], meta[property^=profile:]")
            .forEach { el ->
                val key = el.attr("property").lowercase()
                val value = el.attr("content")
                if (value.isNotBlank()) {
                    props.getOrPut(key) { mutableListOf() }.add(value)
                }
            }

        fun first(key: String) = props[key]?.firstOrNull()
        fun all(key: String) = props[key] ?: emptyList()

        // Parse grouped og:image blocks (each og:image starts a new group)
        val images = buildList {
            var current: MutableMap<String, String>? = null
            doc.select("meta[property^=og:image]").forEach { el ->
                val prop = el.attr("property").lowercase()
                val value = el.attr("content")
                if (prop == "og:image") {
                    current?.let { add(mapToOpenGraphImage(it)) }
                    current = mutableMapOf("url" to value)
                } else {
                    current?.put(prop.removePrefix("og:image:"), value)
                }
            }
            current?.let { add(mapToOpenGraphImage(it)) }
        }

        val videos = buildList {
            var current: MutableMap<String, String>? = null
            doc.select("meta[property^=og:video]").forEach { el ->
                val prop = el.attr("property").lowercase()
                val value = el.attr("content")
                if (prop == "og:video") {
                    current?.let { add(mapToOpenGraphVideo(it)) }
                    current = mutableMapOf("url" to value)
                } else {
                    current?.put(prop.removePrefix("og:video:"), value)
                }
            }
            current?.let { add(mapToOpenGraphVideo(it)) }
        }

        val audios = buildList {
            var current: MutableMap<String, String>? = null
            doc.select("meta[property^=og:audio]").forEach { el ->
                val prop = el.attr("property").lowercase()
                val value = el.attr("content")
                if (prop == "og:audio") {
                    current?.let { add(mapToOpenGraphAudio(it)) }
                    current = mutableMapOf("url" to value)
                } else {
                    current?.put(prop.removePrefix("og:audio:"), value)
                }
            }
            current?.let { add(mapToOpenGraphAudio(it)) }
        }

        return OpenGraphMetadata(
            title = first("og:title"),
            description = first("og:description"),
            url = first("og:url"),
            siteName = first("og:site_name"),
            locale = first("og:locale"),
            localeAlternates = all("og:locale:alternate"),
            type = first("og:type"),
            images = images,
            videos = videos,
            audios = audios,
            articleAuthor = all("article:author"),
            articlePublishedTime = first("article:published_time"),
            articleModifiedTime = first("article:modified_time"),
            articleExpirationTime = first("article:expiration_time"),
            articleSection = first("article:section"),
            articleTags = all("article:tag"),
            profileFirstName = first("profile:first_name"),
            profileLastName = first("profile:last_name"),
            profileUsername = first("profile:username"),
        )
    }

    protected open fun mapToOpenGraphImage(imageProperties: Map<String, String>) = with (imageProperties) { OpenGraphImage(
        url = get("url") ?: "",
        secureUrl = get("secure_url"),
        type = get("type"),
        width = get("width")?.toIntOrNull(),
        height = get("height")?.toIntOrNull(),
        alt = get("alt"),
    ) }

    protected open fun mapToOpenGraphVideo(videoProperties: Map<String, String>) = with (videoProperties) { OpenGraphVideo(
        url = get("url") ?: "",
        secureUrl = get("secure_url"),
        type = get("type"),
        width = get("width")?.toIntOrNull(),
        height = get("height")?.toIntOrNull(),
    ) }


    protected open fun mapToOpenGraphAudio(audioProperties: Map<String, String>) = with (audioProperties) { OpenGraphAudio(
        url = get("url") ?: "",
        secureUrl = get("secure_url"),
        type = get("type"),
    ) }

}