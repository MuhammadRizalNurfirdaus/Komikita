package com.komikita.app.data.remote.parser

import android.util.Log
import com.komikita.app.domain.model.Chapter
import com.komikita.app.domain.model.ComicSource
import com.komikita.app.domain.model.Komik
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ShinigamiJsoupParser"

@Singleton
class ShinigamiJsoupParser @Inject constructor() {

    fun parseComicList(html: String): Result<List<Komik>> {
        return runCatching {
            val doc: Document = Jsoup.parse(html)
            val comics = mutableListOf<Komik>()

            // CSS selectors for 11.shinigami.asia target markup
            val items = doc.select(".page-item-detail, .manga-item, .utao .uta, .bsx, .item-summary, .animepos-grid-item")

            for (item in items) {
                try {
                    val titleElement = item.selectFirst(".post-title a, .series-title, h3 a, .tt a, .title a")
                    val title = titleElement?.text()?.trim() ?: continue
                    val link = titleElement.attr("href")
                    val endpoint = link.trimEnd('/').substringAfterLast('/')

                    val imgElement = item.selectFirst("img.src, img.data-src, img.lazyload, img")
                    val coverUrl = imgElement?.attr("data-src")?.ifBlank { null }
                        ?: imgElement?.attr("src")?.ifBlank { null }
                        ?: ""

                    val rating = item.selectFirst(".rating, .num, .score, .numscore")?.text()?.trim() ?: "0.0"
                    val latestChapter = item.selectFirst(".chapter, .epxs, .latest-chapter, .ep-name")?.text()?.trim() ?: ""

                    comics.add(
                        Komik(
                            title = title,
                            endpoint = endpoint,
                            coverUrl = coverUrl,
                            type = "Manga",
                            status = "Ongoing",
                            latestChapter = latestChapter,
                            rating = rating,
                            source = ComicSource.SCRAPER
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing item element: ${e.message}", e)
                }
            }
            comics
        }.onFailure { e ->
            Log.e(TAG, "parseComicList failed: ${e.message}", e)
        }
    }

    fun parseComicDetail(html: String, endpoint: String): Result<Komik> {
        return runCatching {
            val doc: Document = Jsoup.parse(html)
            val title = doc.selectFirst(".post-title h1, .entry-title, h1.entry-title")?.text()?.trim() ?: ""
            val coverUrl = doc.selectFirst(".summary_image img, .thumb img, .series-thumb img")?.attr("src") ?: ""
            val synopsis = doc.selectFirst(".entry-content, .synopsis, .summary__content")?.text()?.trim() ?: ""
            val rating = doc.selectFirst(".post-total-rating .score, .rating .num")?.text()?.trim() ?: "0.0"
            val status = doc.selectFirst(".post-status .summary-content, .status")?.text()?.trim() ?: "Ongoing"

            val chapters = mutableListOf<Chapter>()
            val chapterElements = doc.select(".wp-manga-chapter a, .eplister li a, #chapterlist li a")
            for (ch in chapterElements) {
                val chTitle = ch.text().trim()
                val chLink = ch.attr("href")
                val chEndpoint = chLink.trimEnd('/').substringAfterLast('/')
                chapters.add(Chapter(title = chTitle, endpoint = chEndpoint))
            }

            Komik(
                title = title,
                endpoint = endpoint,
                coverUrl = coverUrl,
                type = "Manga",
                status = status,
                latestChapter = chapters.firstOrNull()?.title ?: "",
                rating = rating,
                source = ComicSource.SCRAPER,
                synopsis = synopsis,
                chapters = chapters
            )
        }.onFailure { e ->
            Log.e(TAG, "parseComicDetail failed for $endpoint: ${e.message}", e)
        }
    }
}
