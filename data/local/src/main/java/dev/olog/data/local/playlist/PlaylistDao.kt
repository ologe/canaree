package dev.olog.data.local.playlist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.olog.domain.entity.track.Track
import dev.olog.domain.entity.track.toPlaylistSong
import dev.olog.domain.gateway.track.SongGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
abstract class PlaylistDao {

    @Query("""
        SELECT playlist.*, count(*) as size
        FROM playlist playlist JOIN playlist_tracks tracks
            ON playlist.id = tracks.playlistId
        GROUP BY playlistId
    """)
    abstract suspend fun getAllPlaylists(): List<PlaylistEntity>

    @Query("""
        SELECT playlist.*, count(*) as size
        FROM playlist playlist JOIN playlist_tracks tracks
            ON playlist.id = tracks.playlistId
        GROUP BY playlistId
    """)
    abstract fun observeAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("""
        SELECT playlist.*, count(*) as size
        FROM playlist playlist JOIN playlist_tracks tracks
            ON playlist.id = tracks.playlistId
        where playlist.id = :id
        GROUP BY playlistId
    """)
    abstract suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Query("""
        SELECT playlist.*, count(*) as size
        FROM playlist playlist JOIN playlist_tracks tracks
            ON playlist.id = tracks.playlistId
        where playlist.id = :id
        GROUP BY playlistId
    """)
    abstract fun observePlaylistById(id: Long): Flow<PlaylistEntity?>

    @Query("""
        SELECT tracks.*
        FROM playlist playlist JOIN playlist_tracks tracks
            ON playlist.id = tracks.playlistId
        WHERE playlistId = :playlistId
        ORDER BY idInPlaylist
    """)
    abstract suspend fun getPlaylistTracksImpl(playlistId: Long): List<PlaylistTrackEntity>

    suspend fun getPlaylistTracks(
        playlistId: Long,
        songGateway: SongGateway
    ): List<Track.PlaylistSong> {
        val trackList = getPlaylistTracksImpl(playlistId)
        val songList : Map<Long, List<Track>> = songGateway.getAll().groupBy { it.id }
        return trackList.mapNotNull { entity ->
            val track = songList[entity.trackId]?.first()
            track?.toPlaylistSong(
                playlistId = playlistId,
                idInPlaylist = entity.idInPlaylist
            )
        }
    }

    @Query("""
        SELECT tracks.*
        FROM playlist playlist JOIN playlist_tracks tracks
            ON playlist.id = tracks.playlistId
        WHERE playlistId = :playlistId
        ORDER BY idInPlaylist
    """)
    abstract fun observePlaylistTracksImpl(playlistId: Long): Flow<List<PlaylistTrackEntity>>

    fun observePlaylistTracks(
        playlistId: Long,
        songGateway: SongGateway
    ): Flow<List<Track.PlaylistSong>> {
        return observePlaylistTracksImpl(playlistId)
            .map { trackList ->
                val songList : Map<Long, List<Track>> = songGateway.getAll().groupBy { it.id }
                trackList.mapNotNull { entity ->
                    val track = songList[entity.trackId]?.first()
                    track?.toPlaylistSong(
                        playlistId = playlistId,
                        idInPlaylist = entity.idInPlaylist
                    )
                }
            }
    }

    @Query("""
        SELECT max(idInPlaylist)
        FROM playlist playlist JOIN playlist_tracks tracks
            ON playlist.id = tracks.playlistId
        WHERE playlistId = :playlistId
    """)
    abstract suspend fun getPlaylistMaxId(playlistId: Long): Int?

    @Insert
    abstract suspend fun createPlaylist(playlist: PlaylistEntity): Long

    @Query("""
        UPDATE playlist SET name = :name WHERE id = :id
    """)
    abstract suspend fun renamePlaylist(id: Long, name: String)

    @Query("""DELETE FROM playlist WHERE id = :id""")
    abstract suspend fun deletePlaylist(id: Long)

    @Insert
    abstract suspend fun insertTracks(tracks: List<PlaylistTrackEntity>)

    @Query("""
        DELETE FROM playlist_tracks
        WHERE playlistId = :playlistId AND idInPlaylist = :idInPlaylist
    """)
    abstract suspend fun deleteTrack(playlistId: Long, idInPlaylist: Long)

    @Query("""
        DELETE FROM playlist_tracks
        WHERE playlistId = :playlistId
    """)
    abstract suspend fun deletePlaylistTracks(playlistId: Long)

    @Query("""
        DELETE FROM playlist_tracks WHERE playlistId = :id
    """)
    abstract suspend fun clearPlaylist(id: Long)

    @Update
    abstract suspend fun updateTrackList(list: List<PlaylistTrackEntity>)

}