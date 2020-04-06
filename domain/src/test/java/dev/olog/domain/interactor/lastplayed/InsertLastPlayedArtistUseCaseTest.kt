package dev.olog.domain.interactor.lastplayed

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import dev.olog.domain.MediaId.Category
import dev.olog.domain.MediaIdCategory
import dev.olog.domain.MediaIdCategory.ARTISTS
import dev.olog.domain.MediaIdCategory.PODCASTS_AUTHORS
import dev.olog.domain.catchIaeOnly
import dev.olog.domain.gateway.podcast.PodcastAuthorGateway
import dev.olog.domain.gateway.track.ArtistGateway
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class InsertLastPlayedArtistUseCaseTest {

    private val gateway = mock<ArtistGateway>()
    private val podcastGateway = mock<PodcastAuthorGateway>()
    private val sut = InsertLastPlayedArtistUseCase(gateway, podcastGateway)

    @Test
    fun testInvokeWithTrack() = runBlockingTest {
        // given
        val id = 1L
        val category = Category(ARTISTS, id)

        // when
        sut(category)

        // then
        verify(gateway).addLastPlayed(id)
    }

    @Test
    fun testInvokeWithPodcast() = runBlockingTest {
        // given
        val id = 1L
        val category = Category(PODCASTS_AUTHORS, id)

        // when
        sut(category)

        // then
        verify(podcastGateway).addLastPlayed(id)
    }

    @Test
    fun testInvokeWithOtherCategories() = runBlockingTest {
        // given
        val id = 1L
        val allowed = listOf(ARTISTS, PODCASTS_AUTHORS)

        val sut = InsertLastPlayedArtistUseCase(mock(), mock())

        // when
        MediaIdCategory.values().catchIaeOnly(allowed) { value ->
            sut(Category(value, id))
        }
    }

}