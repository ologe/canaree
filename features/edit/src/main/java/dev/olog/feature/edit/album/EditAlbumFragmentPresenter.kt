package dev.olog.feature.edit.album

import dev.olog.domain.entity.LastFmAlbum
import dev.olog.domain.entity.track.Album
import dev.olog.domain.gateway.ImageRetrieverGateway
import dev.olog.domain.gateway.base.Id
import dev.olog.domain.gateway.podcast.PodcastAlbumGateway
import dev.olog.domain.gateway.track.AlbumGateway
import dev.olog.domain.interactor.songlist.GetSongListByParamUseCase
import dev.olog.domain.mediaid.MediaId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class EditAlbumFragmentPresenter @Inject constructor(
    private val albumGateway: AlbumGateway,
    private val podcastAlbumGateway: PodcastAlbumGateway,
    private val lastFmGateway: ImageRetrieverGateway,
    private val getSongListByParamUseCase: GetSongListByParamUseCase

) {

    suspend fun getAlbum(mediaId: MediaId.Category): Album {
        val album = if (mediaId.isPodcastAlbum) {
            podcastAlbumGateway.getByParam(mediaId.categoryValue.toLong())!!
        } else {
            albumGateway.getByParam(mediaId.categoryValue.toLong())!!
        }
        return Album(
            id = album.id,
            artistId = album.artistId,
            albumArtist = album.albumArtist,
            title = album.title,
            artist = if (album.hasUnknownArtist) "" else album.artist,
            hasSameNameAsFolder = album.hasSameNameAsFolder,
            songs = album.songs,
            isPodcast = album.isPodcast
        )
    }

    suspend fun getPath(mediaId: MediaId): String = withContext(Dispatchers.IO) {
        getSongListByParamUseCase(mediaId).first().path
    }

    suspend fun fetchData(id: Id): LastFmAlbum? {
        return lastFmGateway.getAlbum(id)
    }

}