package net.dankito.web.htmlmetadata

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import net.dankito.web.htmlmetadata.test.TestData
import kotlin.test.Test


class HtmlMetadataParserTest {

    private val underTest = HtmlMetadataParser()


    @Test
    fun parse() {
        val result = underTest.parse(TestData.HeiseNewsHeader, "https://www.heise.de/news/Digitale-Unabhaengigkeit-EU-plant-harten-Zugriff-auf-staatliche-IT-Strukturen-11317332.html")

        assertThat(result.standard.title).isEqualTo("Digitale Unabhängigkeit: EU plant harten Zugriff auf staatliche IT-Strukturen | heise online")
        assertThat(result.standard.description).isEqualTo("Mit einem neuen Paket für technologische Souveränität will die EU-Kommission die Abhängigkeit von US-Anbietern brechen. Sie fordert Hunderte Milliarden Einsatz.")
        assertThat(result.standard.author).isEqualTo("Stefan Krempl")
        assertThat(result.standard.canonicalUrl).isEqualTo("https://www.heise.de/news/Digitale-Unabhaengigkeit-EU-plant-harten-Zugriff-auf-staatliche-IT-Strukturen-11317332.html")

        assertThat(result.openGraph.url).isEqualTo("https://www.heise.de/news/Digitale-Unabhaengigkeit-EU-plant-harten-Zugriff-auf-staatliche-IT-Strukturen-11317332.html")
        assertThat(result.openGraph.title).isEqualTo("Digitale Unabhängigkeit: EU plant harten Zugriff auf staatliche IT-Strukturen")
        assertThat(result.openGraph.description).isEqualTo("Mit einem neuen Paket für technologische Souveränität will die EU-Kommission die Abhängigkeit von US-Anbietern brechen. Sie fordert Hunderte Milliarden Einsatz.")
        assertThat(result.openGraph.images.map { it.url }).contains("https://heise.cloudimg.io/bound/1200x1200/q85.png-lossy-85.webp-lossy-85.foil1/_www-heise-de_/imgs/18/5/0/9/4/2/5/4/shutterstock_606694724-d3ca5f9035527a8d.jpg")
        assertThat(result.openGraph.locale).isEqualTo("de_DE")
        assertThat(result.openGraph.siteName).isEqualTo("heise online")
        assertThat(result.openGraph.type).isEqualTo("website")

        assertThat(result.twitter.card).isEqualTo("summary_large_image")
        assertThat(result.twitter.site).isEqualTo("@heiseonline")
        assertThat(result.twitter.image).isEqualTo("https://heise.cloudimg.io/bound/1200x1200/q85.png-lossy-85.webp-lossy-85.foil1/_www-heise-de_/imgs/18/5/0/9/4/2/5/4/shutterstock_606694724-d3ca5f9035527a8d.jpg")
        assertThat(result.twitter.title).isEqualTo("Digitale Unabhängigkeit: EU plant harten Zugriff auf staatliche IT-Strukturen")
        assertThat(result.twitter.description).isEqualTo("Mit einem neuen Paket für technologische Souveränität will die EU-Kommission die Abhängigkeit von US-Anbietern brechen. Sie fordert Hunderte Milliarden Einsatz.")

        assertThat(result.feeds).hasSize(2)
        assertThat(result.feeds.map { it.url }).containsExactlyInAnyOrder(
            "https://www.heise.de/rss/heise-atom.xml",
            "https://www.heise.de/rss/heise.rdf"
        )

        assertThat(result.favicons).hasSize(6)

    }

}