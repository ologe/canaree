package dev.olog.feature.library.library

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.preference.PreferenceManager
import dev.olog.core.mediaid.MediaIdCategory
import dev.olog.feature.library.R
import dev.olog.feature.library.folder.tree.FolderTreeFragment
import dev.olog.feature.library.tab.TabFragment
import dev.olog.shared.isInBounds

@Suppress("DEPRECATION") // the newer version has problems with scroll helper when using 'BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT'
class LibraryFragmentAdapter(
    private val context: Context,
    fragmentManager: FragmentManager,
    private val categories : List<LibraryFragmentCategoryState>

) : FragmentPagerAdapter(fragmentManager) {

    fun getCategoryAtPosition(position: Int): MediaIdCategory? {
        if (categories.isNotEmpty() && categories.isInBounds(position)){
            return categories[position].category
        }
        return null
    }

    override fun getItem(position: Int): Fragment {
        val category = categories[position].category

        return if (category == MediaIdCategory.FOLDERS && showFolderAsHierarchy()){
            FolderTreeFragment.newInstance()
        } else TabFragment.newInstance(category)
    }

    fun showFolderAsHierarchy(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        return prefs.getBoolean(context.getString(R.string.prefs_folder_tree_view_key), false)
    }

    override fun getCount(): Int = categories.size

    override fun getPageTitle(position: Int): CharSequence {
        return categories[position].asString(context)
    }

    fun isEmpty() = categories.isEmpty()

}