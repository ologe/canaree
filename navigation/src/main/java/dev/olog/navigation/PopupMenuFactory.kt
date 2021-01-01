package dev.olog.navigation

import android.view.View
import dev.olog.domain.mediaid.MediaId

interface PopupMenuFactory {

    fun show(view: View, mediaId: MediaId)

}