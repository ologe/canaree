package dev.olog.feature.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.ColorCallback
import com.afollestad.materialdialogs.color.colorChooser
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.olog.domain.prefs.TutorialPreferenceGateway
import dev.olog.feature.settings.last.fm.LastFmCredentialsFragment
import dev.olog.lib.image.provider.GlideApp
import dev.olog.lib.image.provider.creator.ImagesFolderUtils
import dev.olog.navigation.Navigator
import dev.olog.scrollhelper.layoutmanagers.OverScrollLinearLayoutManager
import dev.olog.shared.android.extensions.isDarkMode
import dev.olog.shared.android.extensions.launch
import dev.olog.shared.android.extensions.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(),
    ColorCallback,
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    internal lateinit var tutorialPrefs: TutorialPreferenceGateway

    @Inject
    internal lateinit var navigator: Navigator

    private lateinit var libraryCategories: Preference
    private lateinit var podcastCategories: Preference
    private lateinit var blacklist: Preference
    private lateinit var iconShape: Preference
    private lateinit var deleteCache: Preference
    private lateinit var lastFmCredentials: Preference
    private lateinit var autoCreateImages: Preference
    private lateinit var accentColorChooser: Preference
    private lateinit var resetTutorial: Preference
    // TODO add reset all prefs

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)
        libraryCategories = preferenceScreen.findPreference(getString(R.string.prefs_library_categories_key))!!
        podcastCategories = preferenceScreen.findPreference(getString(R.string.prefs_podcast_library_categories_key))!!
        blacklist = preferenceScreen.findPreference(getString(R.string.prefs_blacklist_key))!!
        iconShape = preferenceScreen.findPreference(getString(R.string.prefs_icon_shape_key))!!
        deleteCache = preferenceScreen.findPreference(getString(R.string.prefs_delete_cached_images_key))!!
        lastFmCredentials = preferenceScreen.findPreference(getString(R.string.prefs_last_fm_credentials_key))!!
        autoCreateImages = preferenceScreen.findPreference(getString(R.string.prefs_auto_create_images_key))!!
        accentColorChooser = preferenceScreen.findPreference(getString(R.string.prefs_color_accent_key))!!
        resetTutorial = preferenceScreen.findPreference(getString(R.string.prefs_reset_tutorial_key))!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = view.findViewById<RecyclerView>(R.id.recycler_view)
        list.layoutManager = OverScrollLinearLayoutManager(list)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        libraryCategories.setOnPreferenceClickListener {
            navigator.toLibraryPreferences(false)
            true
        }
        podcastCategories.setOnPreferenceClickListener {
            navigator.toLibraryPreferences(true)
            true
        }
        blacklist.setOnPreferenceClickListener {
            navigator.toBlacklist()
            true
        }

        deleteCache.setOnPreferenceClickListener {
            showDeleteAllCacheDialog()
            true
        }
        lastFmCredentials.setOnPreferenceClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(LastFmCredentialsFragment.newInstance(), LastFmCredentialsFragment.TAG)
            }
            true
        }
        accentColorChooser.setOnPreferenceClickListener {
            val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val key = getString(R.string.prefs_color_accent_key)
            val defaultColor = ContextCompat.getColor(requireContext(), R.color.defaultColorAccent)

            MaterialDialog(requireContext())
                .colorChooser(
                    colors = ColorPalette.getAccentColors(requireContext().isDarkMode),
                    subColors = ColorPalette.getAccentColorsSub(requireContext().isDarkMode),
                    initialSelection = prefs.getInt(key, defaultColor),
                    selection = this
                ).show()
            true
        }
        resetTutorial.setOnPreferenceClickListener {
            showResetTutorialDialog()
            true
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        libraryCategories.onPreferenceClickListener = null
        podcastCategories.onPreferenceClickListener = null
        blacklist.onPreferenceClickListener = null
        deleteCache.onPreferenceClickListener = null
        lastFmCredentials.onPreferenceClickListener = null
        accentColorChooser.onPreferenceClickListener = null
        resetTutorial.onPreferenceClickListener = null
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        if (context == null) {
            return
            // crash workaround, don't know if crashes because of a leak or what else
        }
        when (key) {
            getString(R.string.prefs_folder_tree_view_key) -> {
                requireActivity().recreate()
            }
            getString(R.string.prefs_show_podcasts_key) -> {
//                presentationPrefs.bottomNavigationPage = BottomNavigationPage.LIBRARY_TRACKS TODO
                requireActivity().recreate()
            }
        }
    }

    private fun showDeleteAllCacheDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.prefs_delete_cached_images_title)
            .setMessage(R.string.are_you_sure)
            .setPositiveButton(R.string.popup_positive_ok) { _, _ -> launch { clearGlideCache() } }
            .setNegativeButton(R.string.popup_negative_no, null)
            .show()
    }

    private suspend fun clearGlideCache() {
        GlideApp.get(requireContext()).clearMemory()

        withContext(Dispatchers.IO) {
            GlideApp.get(requireContext()).clearDiskCache()
            ImagesFolderUtils.getImageFolderFor(requireContext(), ImagesFolderUtils.FOLDER).listFiles()
                ?.forEach { it.delete() }
            ImagesFolderUtils.getImageFolderFor(requireContext(), ImagesFolderUtils.PLAYLIST).listFiles()
                ?.forEach { it.delete() }
            ImagesFolderUtils.getImageFolderFor(requireContext(), ImagesFolderUtils.GENRE).listFiles()
                ?.forEach { it.delete() }
        }
        requireContext().toast(R.string.prefs_delete_cached_images_success)
    }

    private fun showResetTutorialDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.prefs_reset_tutorial_title)
            .setMessage(R.string.are_you_sure)
            .setPositiveButton(R.string.popup_positive_ok) { _, _ -> tutorialPrefs.reset() }
            .setNegativeButton(R.string.popup_negative_no, null)
            .show()
    }

    override fun invoke(dialog: MaterialDialog, color: Int) {
        val realColor = ColorPalette.getRealAccentSubColor(requireContext().isDarkMode, color)
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val key = getString(R.string.prefs_color_accent_key)
        prefs.edit {
            putInt(key, realColor)
        }
        requireActivity().recreate()
    }
}