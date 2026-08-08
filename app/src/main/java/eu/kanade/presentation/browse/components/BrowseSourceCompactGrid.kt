package eu.kanade.presentation.browse.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import eu.kanade.presentation.library.components.CommonMangaItemDefaults
import eu.kanade.presentation.library.components.MangaCompactGridItem
import kotlinx.coroutines.flow.StateFlow
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaCover
import tachiyomi.presentation.core.util.plus

@Composable
fun BrowseSourceCompactGrid(
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

        // Pre-filter by index (peek doesn't trigger Paging's prefetch, unlike get/[]) so the
        // grid only allocates slots for matches - no empty cells left behind by hidden manga.
        val visibleIndices = (0 until mangaList.itemCount).filter { index ->
            val chapterCount = mangaList.peek(index)?.value?.let { chapterCounts[it.id] }
            matchesMinChapterCount(chapterCount, minChapterCount)
        }

        items(count = visibleIndices.size, key = { visibleIndices[it] }) { i ->
            val index = visibleIndices[i]
            val manga by mangaList[index]?.collectAsState() ?: return@items
            BrowseSourceCompactGridItem(
                manga = manga,
                chapterCount = chapterCounts[manga.id],
                isChapterCountRequested = chapterCounts.containsKey(manga.id),
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
private fun BrowseSourceCompactGridItem(
    manga: Manga,
    chapterCount: Int?,
    isChapterCountRequested: Boolean,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = onClick,
    onRequestChapterCount: () -> Unit = {},
) {
    MangaCompactGridItem(
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
