package net.dankito.web.htmlmetadata

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.dankito.web.htmlmetadata.model.JsonLdImage
import net.dankito.web.htmlmetadata.model.JsonLdMetadata
import net.dankito.web.htmlmetadata.model.JsonLdOrganization
import net.dankito.web.htmlmetadata.model.JsonLdPerson
import org.jsoup.nodes.Document
import kotlin.collections.mapNotNull

open class JsonLdParser(
    protected val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerKotlinModule()
        findAndRegisterModules()
    },
) {

    open fun parseJsonLd(doc: Document): List<JsonLdMetadata> {
        return doc.select("script[type=application/ld+json]")
            .mapNotNull { el ->
                runCatching { objectMapper.readTree(el.data()) }.getOrNull()
            }
            .flatMap { node ->
                // Support both single object and @graph arrays
                when {
                    node.has("@graph") -> node["@graph"].toList()
                    node.isArray -> node.toList()
                    else -> listOf(node)
                }
            }
            .mapNotNull { node -> parseJsonLdNode(node) }
    }

    protected open fun parseJsonLdNode(node: JsonNode): JsonLdMetadata? {
        val type = node["@type"]?.asText() ?: return null

        fun str(key: String) = node[key]?.asText()?.takeIf { it.isNotBlank() }

        // author can be a single object or an array
        val authors = when {
            node["author"]?.isArray == true ->
                node["author"].mapNotNull { it["name"]?.asText()?.let { n -> JsonLdPerson(name = n, url = it["url"]?.asText()) } }
            node["author"] != null ->
                listOfNotNull(node["author"]["name"]?.asText()?.let { JsonLdPerson(name = it, url = node["author"]["url"]?.asText()) })
            else -> emptyList()
        }

        val publisher = node["publisher"]?.let { p ->
            JsonLdOrganization(
                name = p["name"]?.asText(),
                url = p["url"]?.asText(),
                logoUrl = p["logo"]?.let { l -> if (l.isTextual) l.asText() else l["url"]?.asText() },
            )
        }

        // image: string | ImageObject | array thereof
        val images = when {
            node["image"] == null -> emptyList()
            node["image"].isTextual -> listOf(JsonLdImage(url = node["image"].asText()))
            node["image"].isArray -> node["image"].mapNotNull { img ->
                when {
                    img.isTextual -> JsonLdImage(url = img.asText())
                    img.has("url") -> JsonLdImage(
                        url = img["url"].asText(),
                        width = img["width"]?.asInt(),
                        height = img["height"]?.asInt(),
                        caption = img["caption"]?.asText(),
                    )
                    else -> null
                }
            }
            node["image"].has("url") -> listOf(
                JsonLdImage(
                    url = node["image"]["url"].asText(),
                    width = node["image"]["width"]?.asInt(),
                    height = node["image"]["height"]?.asInt(),
                    caption = node["image"]["caption"]?.asText(),
                )
            )
            else -> emptyList()
        }

        val keywords = str("keywords")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        return JsonLdMetadata(
            type = type,
            headline = str("headline"),
            description = str("description"),
            url = str("url"),
            datePublished = str("datePublished"),
            dateModified = str("dateModified"),
            authors = authors,
            publisher = publisher,
            images = images,
            keywords = keywords,
            articleSection = str("articleSection"),
            inLanguage = str("inLanguage"),
        )
    }

}