package dev.olog.presentation.player

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import dev.olog.core.MediaId
import dev.olog.media.MediaProvider
import dev.olog.media.model.PlayerMetadata
import dev.olog.media.model.PlayerPlaybackState
import dev.olog.media.model.PlayerState
import dev.olog.presentation.BindingsAdapter
import dev.olog.presentation.R
import dev.olog.presentation.base.adapter.*
import dev.olog.presentation.base.drag.IDragListener
import dev.olog.presentation.base.drag.TouchableAdapter
import dev.olog.presentation.interfaces.slidingPanel
import dev.olog.presentation.model.DisplayableItem
import dev.olog.presentation.model.DisplayableTrack
import dev.olog.presentation.navigator.Navigator
import dev.olog.presentation.player.volume.PlayerVolumeFragment
import dev.olog.presentation.utils.isCollapsed
import dev.olog.presentation.utils.isExpanded
import dev.olog.presentation.widgets.StatusBarView
import dev.olog.presentation.widgets.imageview.PlayerImageView
import dev.olog.presentation.widgets.swipeableview.SwipeableView
import dev.olog.shared.TextUtils
import dev.olog.shared.android.extensions.findActivity
import dev.olog.shared.android.extensions.toggleVisibility
import dev.olog.shared.android.theme.playerAppearanceAmbient
import dev.olog.shared.swapped
import kotlinx.android.synthetic.main.item_mini_queue.view.*
import kotlinx.android.synthetic.main.layout_view_switcher.view.*
import kotlinx.android.synthetic.main.player_controls_default.view.*
import kotlinx.android.synthetic.main.player_layout_default.view.*
import kotlinx.android.synthetic.main.player_toolbar_default.view.*
import kotlinx.coroutines.flow.*

internal class PlayerFragmentAdapter(
    private val mediaProvider: MediaProvider,
    private val navigator: Navigator,
    private val viewModel: PlayerFragmentViewModel,
    private val presenter: PlayerFragmentPresenter,
    private val dragListener: IDragListener,
    private val playerAppearanceAdaptiveBehavior: IPlayerAppearanceAdaptiveBehavior

) : ObservableAdapter<DisplayableItem>(DiffCallbackDisplayableItem),
    TouchableAdapter {

    private val playerViewTypes = listOf(
        R.layout.player_layout_default,
        R.layout.player_layout_spotify,
        R.layout.player_layout_flat,
        R.layout.player_layout_big_image,
        R.layout.player_layout_fullscreen,
        R.layout.player_layout_clean,
        R.layout.player_layout_mini
    )

    override fun initViewHolderListeners(viewHolder: DataBoundViewHolder, viewType: Int) {
        when (viewType) {
            R.layout.item_mini_queue -> {
                viewHolder.setOnClickListener(this) { item, _, _ ->
                    require(item is DisplayableTrack)
                    mediaProvider.skipToQueueItem(item.idInPlaylist)
                }
                viewHolder.setOnLongClickListener(this) { item, _, _ ->
                    navigator.toDialog(item.mediaId, viewHolder.itemView)
                }
                viewHolder.setOnClickListener(R.id.more, this) { item, _, view ->
                    navigator.toDialog(item.mediaId, view)
                }
                viewHolder.elevateAlbumOnTouch()

                viewHolder.setOnDragListener(R.id.dragHandle, dragListener)
            }
            R.layout.player_layout_default,
            R.layout.player_layout_spotify,
            R.layout.player_layout_fullscreen,
            R.layout.player_layout_flat,
            R.layout.player_layout_big_image,
            R.layout.player_layout_clean,
            R.layout.player_layout_mini -> {
                setupListeners(viewHolder)

                viewHolder.setOnClickListener(R.id.more, this) { _, _, view ->
                    try {
                        val mediaId = MediaId.songId(viewModel.getCurrentTrackId())
                        navigator.toDialog(mediaId, view)
                    } catch (ex: NullPointerException){
                        ex.printStackTrace()
                    }
                }
            }
        }

    }

    override fun onViewAttachedToWindow(holder: DataBoundViewHolder) {
        super.onViewAttachedToWindow(holder)

        val viewType = holder.itemViewType

        if (viewType in playerViewTypes) {

            val view = holder.itemView
            view.imageSwitcher?.let {
                it.observeProcessorColors()
                    .onEach(presenter::updateProcessorColors)
                    .launchIn(holder.coroutineScope)
                it.observePaletteColors()
                    .onEach(presenter::updatePaletteColors)
                    .launchIn(holder.coroutineScope)
            }
            view.findViewById<PlayerImageView>(R.id.miniCover)?.let {
                it.observeProcessorColors()
                    .onEach(presenter::updateProcessorColors)
                    .launchIn(holder.coroutineScope)
                it.observePaletteColors()
                    .onEach(presenter::updatePaletteColors)
                    .launchIn(holder.coroutineScope)
            }

            bindPlayerControls(holder, view)

            playerAppearanceAdaptiveBehavior(holder, presenter)
        }
    }

    private fun setupListeners(holder: DataBoundViewHolder) {
        val view = holder.itemView
        view.repeat.setOnClickListener { mediaProvider.toggleRepeatMode() }
        view.shuffle.setOnClickListener { mediaProvider.toggleShuffleMode() }
        view.favorite.setOnClickListener {
            view.favorite.toggleFavorite()
            mediaProvider.togglePlayerFavorite()
        }
        view.lyrics.setOnClickListener { navigator.toOfflineLyrics() }
        view.next.setOnClickListener { mediaProvider.skipToNext() }
        view.playPause.setOnClickListener { mediaProvider.playPause() }
        view.previous.setOnClickListener { mediaProvider.skipToPrevious() }

        view.replay.setOnClickListener {
            it.rotate(-30f)
            mediaProvider.replayTenSeconds()
        }

        view.replay30.setOnClickListener {
            it.rotate(-50f)
            mediaProvider.replayThirtySeconds()
        }

        view.forward.setOnClickListener {
            it.rotate(30f)
            mediaProvider.forwardTenSeconds()
        }

        view.forward30.setOnClickListener {
            it.rotate(50f)
            mediaProvider.forwardThirtySeconds()
        }

        view.playbackSpeed.setOnClickListener { openPlaybackSpeedPopup(it) }

        view.seekBar.setListener(
            onProgressChanged = {
                view.bookmark.text = TextUtils.formatMillis(it)
            }, onStartTouch = {

            }, onStopTouch = {
                mediaProvider.seekTo(it.toLong())
            }
        )
    }

    private fun bindPlayerControls(holder: DataBoundViewHolder, view: View) {
        val playerAppearanceAmbient = view.context.playerAppearanceAmbient

        if (!playerAppearanceAmbient.isSpotify() && !playerAppearanceAmbient.isBigImage()){
            view.next.setDefaultColor()
            view.previous.setDefaultColor()
            view.playPause.setDefaultColor()
        }

        mediaProvider.metadata
            .onEach {
                viewModel.updateCurrentTrackId(it.id)
                updateMetadata(view, it)
                updateImage(view, it)
            }.launchIn(holder.coroutineScope)

        view.volume?.setOnClickListener {
            val outLocation = intArrayOf(0, 0)
            it.getLocationInWindow(outLocation)
            val yLocation = (outLocation[1] - StatusBarView.viewHeight).toFloat()
            view.findActivity().supportFragmentManager.commit { // TODO move to a navigator
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                add(android.R.id.content, PlayerVolumeFragment.newInstance(
                    R.layout.player_volume,
                    yLocation
                ), PlayerVolumeFragment.TAG)
                addToBackStack(PlayerVolumeFragment.TAG)
            }
        }

        mediaProvider.playbackState
            .onEach {
                onPlaybackStateChanged(view, it)
                view.seekBar.onStateChanged(it)
            }
            .launchIn(holder.coroutineScope)

        mediaProvider.repeat
            .onEach(view.repeat::cycle)
            .launchIn(holder.coroutineScope)

        mediaProvider.shuffle
            .onEach(view.shuffle::cycle)
            .launchIn(holder.coroutineScope)

        view.swipeableView?.setOnSwipeListener(object : SwipeableView.SwipeListener {
            override fun onSwipedLeft() {
                mediaProvider.skipToNext()
            }

            override fun onSwipedRight() {
                mediaProvider.skipToPrevious()
            }

            override fun onClick() {
                mediaProvider.playPause()
            }

            override fun onLeftEdgeClick() {
                mediaProvider.skipToPrevious()
            }

            override fun onRightEdgeClick() {
                mediaProvider.skipToNext()
            }
        })

        viewModel.onFavoriteStateChanged
            .onEach(view.favorite::onNextState)
            .launchIn(holder.coroutineScope)

        viewModel.skipToNextVisibility
            .onEach(view.next::updateVisibility)
            .launchIn(holder.coroutineScope)

        viewModel.skipToPreviousVisibility
            .onEach(view.previous::updateVisibility)
            .launchIn(holder.coroutineScope)

        presenter.observePlayerControlsVisibility()
            .filter { !playerAppearanceAmbient.isFullscreen()
                    && !playerAppearanceAmbient.isMini()
                    && !playerAppearanceAmbient.isSpotify()
                    && !playerAppearanceAmbient.isBigImage()
            }
            .onEach { visible ->
                view.findViewById<View>(R.id.playerControls)
                    ?.findViewById<View>(R.id.player)
                    ?.toggleVisibility(visible, true)
            }.launchIn(holder.coroutineScope)


        mediaProvider.playbackState
            .filter { it.isSkipTo }
            .map { it.state == PlayerState.SKIP_TO_NEXT }
            .onEach { animateSkipTo(view, it) }
            .launchIn(holder.coroutineScope)

        mediaProvider.playbackState
            .filter { it.isPlayOrPause }
            .map { it.state }
            .distinctUntilChanged()
            .onEach { state ->
                when (state) {
                    PlayerState.PLAYING -> playAnimation(view)
                    PlayerState.PAUSED -> pauseAnimation(view)
                    else -> error("invalid state $state")
                }
            }.launchIn(holder.coroutineScope)
    }

    private fun updateMetadata(view: View, metadata: PlayerMetadata) {
        if (view.context.playerAppearanceAmbient.isFlat()){
            // WORKAROUND, all caps attribute is not working for some reason
            view.title.text = metadata.title.toUpperCase()
        } else {
            view.title.text = metadata.title
        }
        view.artist.text = metadata.artist

        val duration = metadata.duration

        val readableDuration = metadata.readableDuration
        view.duration.text = readableDuration
        view.seekBar.max = duration.toInt()

        val isPodcast = metadata.isPodcast
        val playerControlsRoot = view.findViewById<ViewGroup>(R.id.playerControls)
        playerControlsRoot.podcast_controls.toggleVisibility(isPodcast, true)
    }

    private fun updateImage(view: View, metadata: PlayerMetadata) {
        view.imageSwitcher?.loadImage(metadata)
        view.findViewById<PlayerImageView>(R.id.miniCover)?.loadImage(metadata.mediaId)
    }

    private fun openPlaybackSpeedPopup(view: View) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.dialog_playback_speed)
        popup.menu.getItem(viewModel.getPlaybackSpeed()).isChecked = true
        popup.setOnMenuItemClickListener {
            viewModel.setPlaybackSpeed(it.itemId)
            true
        }
        popup.show()
    }

    private fun onPlaybackStateChanged(view: View, playbackState: PlayerPlaybackState) {
        val isPlaying = playbackState.isPlaying

        if (isPlaying || playbackState.isPaused) {
            view.nowPlaying?.isActivated = isPlaying
            view.imageSwitcher?.setChildrenActivated(isPlaying)
        }
    }

    private fun animateSkipTo(view: View, toNext: Boolean) {
        if (view.slidingPanel.isCollapsed()) {
            return
        }

        if (toNext) {
            view.next.playAnimation()
        } else {
            view.previous.playAnimation()
        }
    }

    private fun playAnimation(view: View) {
        val isPanelExpanded = view.slidingPanel.isExpanded()
        view.playPause.animationPlay(isPanelExpanded)
    }

    private fun pauseAnimation(view: View) {
        val isPanelExpanded = view.slidingPanel.isExpanded()
        view.playPause.animationPause(isPanelExpanded)
    }

    override fun bind(holder: DataBoundViewHolder, item: DisplayableItem, position: Int) {
        if (item is DisplayableTrack){
            holder.itemView.apply {
                BindingsAdapter.loadSongImage(holder.imageView!!, item.mediaId)
                firstText.text = item.title
                secondText.text = item.artist
                explicit.onItemChanged(item.title)
            }
        }
    }

    override fun canInteractWithViewHolder(viewType: Int): Boolean {
        return viewType == R.layout.item_mini_queue
    }

    override fun onMoved(from: Int, to: Int) {
        val realFrom = from - 1
        val realTo = to - 1
        mediaProvider.swapRelative(realFrom, realTo)

        submitList(currentList.swapped(from, to))
    }

    override fun onSwipedRight(viewHolder: RecyclerView.ViewHolder) {
        val realPosition = viewHolder.adapterPosition - 1
        mediaProvider.removeRelative(realPosition)
    }

    override fun afterSwipeRight(viewHolder: RecyclerView.ViewHolder) {
        val newList = currentList.toMutableList()
        newList.removeAt(viewHolder.adapterPosition)
        submitList(newList)
    }

    override fun afterSwipeLeft(viewHolder: RecyclerView.ViewHolder) {
        val realPosition = viewHolder.adapterPosition - 1
        mediaProvider.moveRelative(realPosition)
        notifyItemChanged(viewHolder.adapterPosition)
    }

}