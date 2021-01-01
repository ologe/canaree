package dev.olog.data.local.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import dev.olog.domain.entity.Sort
import dev.olog.domain.prefs.SortDetail
import dev.olog.domain.prefs.SortPreferencesGateway
import javax.inject.Inject

internal class SortPreferencesGatewayImpl @Inject constructor(
    private val preferences: SharedPreferences,
    private val detailSortingHelper: SortDetailImpl

) : SortPreferencesGateway, SortDetail by detailSortingHelper {

    companion object {
        const val TAG = "AppPreferencesDataStoreImpl"

        private const val ALL_SONGS_SORT_ORDER = "$TAG.ALL_SONG_SORT_ORDER"
        private const val ALL_ALBUMS_SORT_ORDER = "$TAG.ALL_ALBUMS_SORT_ORDER"
        private const val ALL_ARTISTS_SORT_ORDER = "$TAG.ALL_ARTISTS_SORT_ORDER"

        private const val ALL_ALBUMS_SORT_ARRANGING = "$TAG.ALL_ALBUMS_SORT_ARRANGING"
        private const val ALL_SONGS_SORT_ARRANGING = "$TAG.ALL_SONGS_SORT_ARRANGING"
        private const val ALL_ARTISTS_SORT_ARRANGING = "$TAG.ALL_ARTISTS_SORT_ARRANGING"
    }

    override fun getAllTracksSort(): Sort {
        val sort = preferences.getInt(ALL_SONGS_SORT_ORDER, Sort.Type.TITLE.ordinal)
        val arranging =
            preferences.getInt(ALL_SONGS_SORT_ARRANGING, Sort.Arranging.ASCENDING.ordinal)
        return Sort(
            Sort.Type.values()[sort],
            Sort.Arranging.values()[arranging]
        )
    }

    override fun getAllAlbumsSort(): Sort {
        val sort = preferences.getInt(ALL_ALBUMS_SORT_ORDER, Sort.Type.TITLE.ordinal)
        val arranging =
            preferences.getInt(ALL_ALBUMS_SORT_ARRANGING, Sort.Arranging.ASCENDING.ordinal)
        return Sort(
            Sort.Type.values()[sort],
            Sort.Arranging.values()[arranging]
        )
    }

    override fun getAllArtistsSort(): Sort {
        val sort = preferences.getInt(ALL_ARTISTS_SORT_ORDER, Sort.Type.ARTIST.ordinal)
        val arranging =
            preferences.getInt(ALL_ARTISTS_SORT_ARRANGING, Sort.Arranging.ASCENDING.ordinal)
        return Sort(
            Sort.Type.values()[sort],
            Sort.Arranging.values()[arranging]
        )
    }

    override fun setAllTracksSort(sortType: Sort) {
        preferences.edit {
            putInt(ALL_SONGS_SORT_ORDER, sortType.type.ordinal)
            putInt(ALL_SONGS_SORT_ARRANGING, sortType.arranging.ordinal)
        }
    }

    override fun setAllAlbumsSort(sortType: Sort) {
        preferences.edit {
            putInt(ALL_ALBUMS_SORT_ORDER, sortType.type.ordinal)
            putInt(ALL_ALBUMS_SORT_ARRANGING, sortType.arranging.ordinal)
        }
    }

    override fun setAllArtistsSort(sortType: Sort) {
        preferences.edit {
            putInt(ALL_ARTISTS_SORT_ORDER, sortType.type.ordinal)
            putInt(ALL_ARTISTS_SORT_ARRANGING, sortType.arranging.ordinal)
        }
    }

    // TODO reset all??
    override fun reset() {

    }
}