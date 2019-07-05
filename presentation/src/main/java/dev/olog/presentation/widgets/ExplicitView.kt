package dev.olog.presentation.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import dev.olog.presentation.model.DisplayableItem
import dev.olog.shared.extensions.toggleVisibility
import kotlinx.coroutines.*

class ExplicitView (
    context: Context,
    attrs: AttributeSet
) : AppCompatImageView(context, attrs), CoroutineScope by MainScope() {

    private var job: Job? = null

    fun onItemChanged(item: DisplayableItem){
        // TODO check compuitation cost
        toggleVisibility(visible = false, gone = true)

        job?.cancel()
        job = launch(Dispatchers.Default) {
            val show = item.title.contains("explicit", ignoreCase = true)
            withContext(Dispatchers.Main){
                toggleVisibility(visible = show, gone = true)
            }
        }
    }

}