package eu.kanade.presentation.browse.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import eu.kanade.presentation.library.components.CommonMangaItemDefaults
import eu.kanade.presentation.library.components.MangaComfortableGridItem
import kotlinx.coroutines.flow.StateFlow
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaCover
import tachiyomi.presentation.core.util.plus

@Composable
fun BrowseSourceComfortableGrid(
    mangaList: LazyPagingItems<StateFlow<Manga>>,
    columns: GridCells,
    contentPadding: PaddingValues,
    onMangaClick: (Manga) -> Unit,
    onMangaLongClick: (Manga) -> Unit,
    chapterCounts: Map<Long, Int?> = emptyMap(),
    onRequestChapterCount: (Manga) -> Unit = {},
    minChapterCount: Int? = null,
) {
    LazyVerticalGrid(
        columns = columns,
        contentPadding = contentPadding + PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(CommonMangaItemDefaults.GridVerticalSpacer),
        horizontalArrangement = Arrangement.spacedBy(CommonMangaItemDefaults.GridHorizontalSpacer),
    ) {
        if (mangaList.loadState.prepend is LoadState.Loading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                BrowseSourceLoadingItem()
            }
        }

        items(count = mangaList.itemCount) { index ->
            val manga by mangaList[index]?.collectAsState() ?: return@items
            BrowseSourceComfortableGridItem(
                manga = manga,
                chapterCount = chapterCounts[manga.id],
                isChapterCountRequested = chapterCounts.containsKey(manga.id),
                minChapterCount = minChapterCount,
                onClick = { onMangaClick(manga) },
                onLongClick = { onMangaLongClick(manga) },
                onRequestChapterCount = { onRequestChapterCount(manga) },
            )
        }

        if (mangaList.loadState.refresh is LoadState.Loading || mangaList.loadState.append is LoadState.Loading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                BrowseSourceLoadingItem()
            }
        }
    }
}

@Composable
private fun BrowseSourceComfortableGridItem(
    manga: Manga,
    chapterCount: Int?,
    isChapterCountRequested: Boolean,
    minChapterCount: Int?,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = onClick,
    onRequestChapterCount: () -> Unit = {},
) {
    // Auto-request the chapter count as soon as this item is composed (i.e. scrolled
    // into view, since LazyVerticalGrid only composes items near the viewport). Always runs,
    // even while hidden by minChapterCount below, so the filter can keep discovering matches
    // as covers scroll past instead of only working within an already-checked set.
    // Requests are still throttled in BrowseSourceViewModel so this doesn't burst the source.
    LaunchedEffect(manga.id) {
        onRequestChapterCount()
    }

    if (!matchesMinChapterCount(chapterCount, minChapterCount)) return

    MangaComfortableGridItem(
        title = manga.title,
        coverData = MangaCover(
            mangaId = manga.id,
            sourceId = manga.source,
            isMangaFavorite = manga.favorite,
            url = manga.thumbnailUrl,
            lastModified = manga.coverLastModified,
        ),
        coverAlpha = if (manga.favorite) CommonMangaItemDefaults.BrowseFavoriteCoverAlpha else 1f,
        coverBadgeStart = {
            InLibraryBadge(enabled = manga.favorite)
        },
        coverBadgeEnd = {
            ChapterCountBadge(
                chapterCount = chapterCount,
                isRequested = isChapterCountRequested,
                onClick = onRequestChapterCount,
            )
        },
        onLongClick = onLongClick,
        onClick = onClick,
    )
}
