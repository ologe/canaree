package dev.olog.presentation.base.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import dev.olog.presentation.model.BaseModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

abstract class ObservableAdapter<T : BaseModel>(
    itemCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, DataBoundViewHolder>(itemCallback){

    private val flow = MutableStateFlow(currentList)

    fun observeData(): Flow<List<T>> = flow

    @CallSuper
    override fun onViewAttachedToWindow(holder: DataBoundViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onAppear()
    }

    @CallSuper
    override fun onViewDetachedFromWindow(holder: DataBoundViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onDisappear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(viewType, parent, false)
        val viewHolder = DataBoundViewHolder(view)
        initViewHolderListeners(viewHolder, viewType)
        return viewHolder
    }

    protected abstract fun initViewHolderListeners(viewHolder: DataBoundViewHolder, viewType: Int)

    override fun getItemViewType(position: Int): Int = getItem(position).type

    override fun onBindViewHolder(holder: DataBoundViewHolder, position: Int) {
        val item = getItem(position)
        bind(holder, item, position)
    }

    protected abstract fun bind(holder: DataBoundViewHolder, item: T, position: Int)

    fun lastIndex(): Int = currentList.lastIndex
    fun indexOf(predicate: (T) -> Boolean): Int = currentList.indexOfFirst(predicate)

    override fun getCurrentList(): List<T> = super.getCurrentList()

    public override fun getItem(position: Int): T = super.getItem(position)

    override fun submitList(list: List<T>?) {
        flow.value = list.orEmpty().toList()
        super.submitList(list)
    }

    override fun submitList(list: List<T>?, commitCallback: Runnable?) {
        flow.value = list.orEmpty().toList()
        super.submitList(list, commitCallback)
    }

}