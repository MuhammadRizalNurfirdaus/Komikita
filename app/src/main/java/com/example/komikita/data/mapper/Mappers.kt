package com.example.komikita.data.mapper

import com.example.komikita.data.model.*
import com.example.komikita.domain.model.*
import com.example.komikita.domain.model.Genre as DomainGenre

// ============================================================
// Mapper: Mengkonversi DTO (Data Transfer Object) ke Domain Model.
// Ini adalah bagian penting Clean Architecture - memisahkan
// representasi data eksternal dari model bisnis internal.
// ============================================================

// --- Scraper DTO -> Domain ---

/**
 * Konversi ScraperKomikItem ke domain Komik (source = SCRAPER).
 */
fun ScraperKomikItem.toDomain(): Komik {
    return Komik(
        slug = slug.orEmpty(),
        title = title.orEmpty(),
        poster = poster,
        type = type,
        chapter = chapter ?: latestChapter,
        date = date ?: updateTime,
        score = score ?: rating,
        source = KomikSource.SCRAPER
    )
}

/**
 * Konversi list ScraperKomikItem ke list domain Komik.
 */
fun List<ScraperKomikItem>?.toDomainList(): List<Komik> {
    return this?.map { it.toDomain() } ?: emptyList()
}

/**
 * Konversi ScraperDetailResponse ke domain KomikDetail.
 */
fun ScraperDetailResponse.toDomain(slug: String): KomikDetail {
    val d = data
    return KomikDetail(
        slug = slug,
        title = d?.title.orEmpty(),
        poster = d?.poster,
        author = d?.author,
        description = d?.description,
        genres = d?.genres ?: emptyList(),
        status = d?.status,
        type = d?.type,
        releaseDate = d?.releaseDate,
        updatedOn = d?.updatedOn,
        totalChapter = d?.totalChapter,
        chapters = d?.chapters?.map { ch ->
            ChapterItem(
                chapterId = ch.chapterId.orEmpty(),
                chapterNumber = ch.chapter.orEmpty(),
                date = ch.date
            )
        } ?: emptyList(),
        source = KomikSource.SCRAPER
    )
}

/**
 * Konversi ScraperChapterResponse ke domain ChapterPages.
 */
fun ScraperChapterResponse.toDomain(): ChapterPages {
    val d = data
    return ChapterPages(
        title = d?.title,
        images = d?.images ?: emptyList(),
        prevChapterId = d?.prevChapterId,
        nextChapterId = d?.nextChapterId,
        source = KomikSource.SCRAPER
    )
}

/**
 * Konversi ScraperGenre ke domain Genre.
 */
fun ScraperGenre.toDomain(): DomainGenre {
    return DomainGenre(
        name = name.orEmpty(),
        slug = slug.orEmpty(),
        url = url
    )
}

// --- Backend DTO -> Domain ---

/**
 * Konversi CustomComicDto ke domain Komik (source = CUSTOM).
 * Digunakan untuk tampilan list (Home Screen).
 */
fun CustomComicDto.toDomainKomik(): Komik {
    return Komik(
        slug = slug.orEmpty(),
        title = title.orEmpty(),
        poster = coverUrl,
        type = type,
        chapter = chapters?.lastOrNull()?.chapterNumber?.let { "Ch. $it" },
        date = createdAt,
        score = null,
        source = KomikSource.CUSTOM
    )
}

/**
 * Konversi list CustomComicDto ke list domain Komik.
 */
fun List<CustomComicDto>?.toDomainKomikList(): List<Komik> {
    return this?.map { it.toDomainKomik() } ?: emptyList()
}

/**
 * Konversi CustomComicDto ke domain CustomComic (lengkap).
 */
fun CustomComicDto.toDomainCustomComic(): CustomComic {
    return CustomComic(
        id = id,
        title = title.orEmpty(),
        slug = slug.orEmpty(),
        coverUrl = coverUrl,
        type = type,
        chapters = chapters?.map { ch ->
            CustomChapter(
                id = ch.id,
                comicId = ch.comicId,
                chapterNumber = ch.chapterNumber.orEmpty(),
                title = ch.title,
                pages = ch.pages?.map { it.imageUrl.orEmpty() } ?: emptyList(),
                createdAt = System.currentTimeMillis()
            )
        } ?: emptyList(),
        isHidden = isHidden ?: false,
        createdAt = System.currentTimeMillis(),
        authorUid = authorUid
    )
}

/**
 * Konversi CustomComicDto ke domain KomikDetail.
 */
fun CustomComicDto.toDomainDetail(): KomikDetail {
    return KomikDetail(
        slug = slug.orEmpty(),
        title = title.orEmpty(),
        poster = coverUrl,
        author = null,
        description = null,
        genres = emptyList(),
        status = null,
        type = type,
        releaseDate = createdAt,
        updatedOn = null,
        totalChapter = chapters?.size?.toString(),
        chapters = chapters?.map { ch ->
            ChapterItem(
                chapterId = ch.id.orEmpty(),
                chapterNumber = "Chapter ${ch.chapterNumber}",
                date = ch.createdAt
            )
        } ?: emptyList(),
        source = KomikSource.CUSTOM
    )
}

/**
 * Konversi CustomChapterDto ke domain ChapterPages.
 */
fun CustomChapterDto.toDomainChapterPages(): ChapterPages {
    return ChapterPages(
        title = title,
        images = pages?.map { it.imageUrl.orEmpty() } ?: emptyList(),
        prevChapterId = null,  // Backend bisa tambahkan ini nanti
        nextChapterId = null,
        source = KomikSource.CUSTOM
    )
}

/**
 * Konversi BackendUserDto ke domain User.
 */
fun BackendUserDto.toDomainUser(): com.example.komikita.domain.model.User {
    return com.example.komikita.domain.model.User(
        uid = firebaseUid.orEmpty(),
        email = email.orEmpty(),
        displayName = displayName,
        photoUrl = photoUrl,
        role = com.example.komikita.domain.model.UserRole.fromString(role.orEmpty()),
        isEmailVerified = false
    )
}
