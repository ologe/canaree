package dev.olog.presentation.search

import android.content.Context
import dev.olog.presentation.PresentationId.Companion.headerId
import dev.olog.presentation.R
import dev.olog.presentation.dagger.PerFragment
import dev.olog.presentation.model.DisplayableHeader
import dev.olog.presentation.model.DisplayableItem
import dev.olog.presentation.model.DisplayableNestedListPlaceholder
import dev.olog.shared.ApplicationContext
import javax.inject.Inject

@PerFragment
class SearchFragmentHeaders @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val recents: List<DisplayableItem> = listOf(
        DisplayableHeader(
            type = R.layout.item_search_recent_header,
            mediaId = headerId("recent searches header id"),
            title = context.getString(R.string.search_recent_searches)
        )
    )

    fun trackHeaders(size: Int, showPodcast: Boolean): DisplayableItem = DisplayableHeader(
        type = R.layout.item_search_header,
        mediaId = headerId("songs header id"),
        title = context.getString(if (showPodcast) R.string.search_podcasts else R.string.search_songs),
        subtitle = context.resources.getQuantityString(R.plurals.search_xx_results, size, size)
    )

    fun albumsHeaders(size: Int): List<DisplayableItem> = listOf(
        DisplayableHeader(
            type = R.layout.item_search_header,
            mediaId = headerId("albums header id"),
            title = context.getString(R.string.search_albums),
            subtitle = context.resources.getQuantityString(R.plurals.search_xx_results, size, size)
        ),
        DisplayableNestedListPlaceholder(
            type = R.layout.item_search_list_albums,
            mediaId = headerId("albums list id")
        )
    )

    fun artistsHeaders(size: Int, showPodcast: Boolean): List<DisplayableItem> = listOf(
        DisplayableHeader(
            type = R.layout.item_search_header,
            mediaId = headerId("artists header id"),
            title = context.getString(if (showPodcast) R.string.search_podcast_authors else R.string.search_artists),
            subtitle = context.resources.getQuantityString(R.plurals.search_xx_results, size, size)
        ),
        DisplayableNestedListPlaceholder(
            type = R.layout.item_search_list_artists,
            mediaId = headerId("artists list id")
        )
    )

    fun foldersHeaders(size: Int): List<DisplayableItem> = listOf(
        DisplayableHeader(
            type = R.layout.item_search_header,
            mediaId = headerId("folders header id"),
            title = context.getString(R.string.search_folders),
            subtitle = context.resources.getQuantityString(R.plurals.search_xx_results, size, size)
        ),
        DisplayableNestedListPlaceholder(
            type = R.layout.item_search_list_folder,
            mediaId = headerId("folders list id")
        )
    )

    fun playlistsHeaders(size: Int): List<DisplayableItem> = listOf(
        DisplayableHeader(
            type = R.layout.item_search_header,
            mediaId = headerId("playlists header id"),
            title = context.getString(R.string.search_playlists),
            subtitle = context.resources.getQuantityString(R.plurals.search_xx_results, size, size)
        ),
        DisplayableNestedListPlaceholder(
            type = R.layout.item_search_list_playlists,
            mediaId = headerId("playlists list id")
        )
    )

    fun genreHeaders(size: Int): List<DisplayableItem> = listOf(
        DisplayableHeader(
            type = R.layout.item_search_header,
            mediaId = headerId("genres header id"),
            title = context.getString(R.string.search_genres),
            subtitle = context.resources.getQuantityString(R.plurals.search_xx_results, size, size)
        ),
        DisplayableNestedListPlaceholder(
            type = R.layout.item_search_list_genre,
            mediaId = headerId("genres list id")
        )
    )

}