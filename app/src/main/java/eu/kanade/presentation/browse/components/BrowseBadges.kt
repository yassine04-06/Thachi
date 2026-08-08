package eu.kanade.presentation.browse.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.components.Badge

@Composable
internal fun InLibraryBadge(enabled: Boolean) {
    if (enabled) {
        Badge(
            imageVector = Icons.Outlined.CollectionsBookmark,
        )
    }
}

/**
 * Badge showing the manga's chapter count, fetched automatically as the cover scrolls
 * into view (throttled in BrowseSourceViewModel), and re-triggerable by tapping.
 * - Not yet requested: shows a "chapters?" prompt (brief, until the auto-fetch kicks in).
 * - Requested and loading: shows a small spinner.
 * - Loaded: shows the chapter count.
 * - Failed: shows a dash, tappable again to retry.
 * Tapping never opens the manga; it only triggers/consumes the count fetch.
 *
 * [chapterCount] uses -1 as a sentinel for "fetch failed" (see
 * BrowseSourceViewModel.CHAPTER_COUNT_FAILED), since a plain null is
 * already used to mean "currently loading".
 */
@Composable
internal fun ChapterCountBadge(
    chapterCount: Int?,
    isRequested: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    when {
        !isRequested || chapterCount == -1 -> {
            Badge(
                text = if (isRequested) "—" else "capitoli?",
                modifier = Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = onClick,
                ),
            )
        }
        chapterCount == null -> {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 1.5.dp,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        else -> {
            Badge(text = "$chapterCount cap.")
        }
    }
}
