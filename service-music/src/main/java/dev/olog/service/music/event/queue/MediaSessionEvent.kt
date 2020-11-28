package dev.olog.service.music.event.queue

import dev.olog.shared.android.BundleDictionary
import java.net.URI

// TODO show track not found when needed, toast? snackbar?
internal sealed class MediaSessionEvent {

    sealed class Prepare : MediaSessionEvent() {

        object LastQueue : Prepare()

        data class FromMediaId(
            val mediaId: String,
            val extras: BundleDictionary,
        ) : Prepare()

        data class FromSearch(
            val query: String?,
            val extras: BundleDictionary,
        ) : Prepare()

        data class FromUri(
            val uri: URI,
            val extras: BundleDictionary,
        ) : Prepare()

    }

    sealed class Play : MediaSessionEvent() {

        data class FromMediaId(
            val mediaId: String,
            val extras: BundleDictionary,
        ) : Play()

        data class FromSearch(
            val query: String?,
            val extras: BundleDictionary,
        ) : Play()

        data class FromUri(
            val uri: URI,
            val extras: BundleDictionary,
        ) : Play()

//        data class Shuffle(
//            val mediaId: MediaId,
//            val filter: String?
//        ) : MediaSessionEvent()
//
//        data class RecentlyAdded(
//            val mediaId: MediaId
//        ) : MediaSessionEvent()
//
//        data class MostPlayed(
//            val mediaId: MediaId
//        ) : MediaSessionEvent()

        @Suppress("RemoveRedundantQualifierName")
        fun isQueueAlreadyPrepared(prepare: Prepare?): Boolean {
            prepare ?: return false

            return when (this) {
                is FromMediaId -> prepare is Prepare.FromMediaId &&
                    this.mediaId == prepare.mediaId &&
                    this.extras == prepare.extras
                is FromSearch -> prepare is Prepare.FromSearch &&
                    this.query == prepare.query &&
                    this.extras == prepare.extras
                is FromUri -> prepare is Prepare.FromUri &&
                    this.uri == prepare.uri &&
                    this.extras == prepare.extras
            }
        }

        fun getPrepareQueueEvent(): Prepare = when (this) {
            is FromMediaId -> Prepare.FromMediaId(mediaId = this.mediaId, extras = this.extras)
            is FromSearch -> Prepare.FromSearch(query = this.query, extras = this.extras)
            is FromUri -> Prepare.FromUri(uri = this.uri, extras = this.extras)
        }

    }

    sealed class Queue : MediaSessionEvent() {

        data class Swap(
            val from: Int,
            val to: Int
        ) : Queue()

        data class SwapRelative(
            val from: Int,
            val to: Int
        ) : Queue()

        data class Remove(
            val position: Int
        ) : Queue()

        data class RemoveRelative(
            val position: Int
        ) : Queue()

        data class MoveRelative(
            val position: Int
        ) : Queue()

        data class AddToPlayLater(
            val ids: List<Long>,
            val isPodcast: Boolean,
        ) : Queue()

        data class AddToPlayNext(
            val ids: List<Long>,
            val isPodcast: Boolean,
        ) : Queue()

    }

    sealed class PlayerAction : MediaSessionEvent() {

        object Resume : PlayerAction()

        data class Pause(
            val stopService: Boolean,
            val releaseFocus: Boolean = true,
        ) : PlayerAction()

        data class SeekTo(
            val millis: Long,
        ) : PlayerAction()

        object SkipToPrevious : PlayerAction()

        data class SkipToNext(
            val ended: Boolean,
        ) : PlayerAction()

        data class SkipToItem(
            val id: Long,
        ) : PlayerAction()

        object Forward10Seconds : PlayerAction()
        object Forward30Seconds : PlayerAction()

        object Replay10Seconds : PlayerAction()
        object Replay30Seconds : PlayerAction()

    }

    // TODO
//    object ToggleFavorite : MediaSessionEvent()
//
//    object RepeatModeChanged : MediaSessionEvent()
//    object ShuffleModeChanged : MediaSessionEvent()

}