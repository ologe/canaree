package dev.olog.feature.library.tab.model

import androidx.annotation.LayoutRes
import dev.olog.core.MediaId
import dev.olog.core.entity.sort.SortType
import dev.olog.feature.library.R
import dev.olog.shared.android.DisplayableItemUtils
import kotlin.time.Duration

sealed class TabFragmentModel(
    @LayoutRes open val layoutType: Int
) {

    interface Scrollable {

        fun sortBy(type: SortType): String?

        fun letter(type: SortType): String = sortBy(type)?.firstOrNull()?.toUpperCase()?.toString() ?: ""

    }

    data class Album(
        @LayoutRes private val layoutId: Int,
        val mediaId: MediaId,
        val title: String,
        val subtitle: String?,
    ) : TabFragmentModel(layoutId), Scrollable {

        override fun sortBy(type: SortType): String? {
            if (mediaId.isAlbum) {
                return when (type) {
                    SortType.TITLE -> title
                    else -> subtitle // artist
                }
            }
            return title
        }
    }

    data class Track(
        val mediaId: MediaId,
        val title: String,
        val artist: String,
        val album: String,
    ) : TabFragmentModel(R.layout.item_tab_track), Scrollable {

        val subtitle: String
            get() = DisplayableItemUtils.trackSubtitle(artist, album)

        override fun sortBy(type: SortType): String? = when (type) {
            SortType.TITLE -> title
            SortType.ALBUM -> album
            SortType.ARTIST -> artist
            else -> title
        }

    }

    data class Podcast(
        val mediaId: MediaId,
        val title: String,
        val artist: String,
        val album: String,
        val duration: Duration,
    ) : TabFragmentModel(R.layout.item_tab_podcast), Scrollable {

        val subtitle: String
            get() = DisplayableItemUtils.trackSubtitle(artist, album)

        override fun sortBy(type: SortType): String? = when (type) {
            SortType.TITLE -> title
            SortType.ALBUM -> album
            SortType.ARTIST -> artist
            else -> title
        }

        val formattedDuration: String
            get() = "${duration.inSeconds.toInt()}m"

    }

    data class Header(
        val title: String
    ) : TabFragmentModel(R.layout.item_tab_header)

    object Shuffle : TabFragmentModel(R.layout.item_tab_shuffle)

    object RecentlyPlayedAlbumsList : TabFragmentModel(R.layout.item_tab_last_played_album_list)
    object RecentlyPlayedArtistList : TabFragmentModel(R.layout.item_tab_last_played_artist_list)

    object RecentlyAddedAlbumsList : TabFragmentModel(R.layout.item_tab_recently_added_album_list)
    object RecentlyAddedArtistList : TabFragmentModel(R.layout.item_tab_recently_added_artist_list)

}