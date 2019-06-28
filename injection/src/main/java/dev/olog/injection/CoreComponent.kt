package dev.olog.injection

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import dagger.BindsInstance
import dagger.Component
import dev.olog.core.IEncrypter
import dev.olog.core.dagger.ApplicationContext
import dev.olog.core.executor.ComputationScheduler
import dev.olog.core.executor.IoScheduler
import dev.olog.core.gateway.*
import dev.olog.core.prefs.*
import dev.olog.data.DataModule
import dev.olog.data.PreferenceModule
import dev.olog.data.RepositoryHelperModule
import dev.olog.data.RepositoryModule
import dev.olog.data.api.lastfm.LastFmModule
import dev.olog.injection.equalizer.EqualizerModule
import dev.olog.injection.equalizer.IBassBoost
import dev.olog.injection.equalizer.IEqualizer
import dev.olog.injection.equalizer.IVirtualizer
import javax.inject.Singleton

@Component(
    modules = arrayOf(
        CoreModule::class,
        SchedulersModule::class,
        LastFmModule::class,

//        // data
        RepositoryModule::class,
        RepositoryHelperModule::class,
        PreferenceModule::class,
        DataModule::class,
        EqualizerModule::class
//
//        // presentation
//        ActivityBindingsModule ::class,
//        AboutActivityInjector::class,
//        PreferencesActivityInjector::class,
//        PlaylistChooserActivityInjector::class

    )
)
@Singleton
interface CoreComponent {

    fun provideAlarmManager(): AlarmManager

    @ApplicationContext
    fun context(): Context
    fun resources(): Resources

    fun lastFmGateway(): LastFmGateway

    fun prefs(): AppPreferencesGateway
    fun musicPrefs(): MusicPreferencesGateway
    fun tutorialPrefs(): TutorialPreferenceGateway
    fun equalizerPrefs(): EqualizerPreferencesGateway
    fun sortPrefs(): SortPreferences
    fun blacklistPrefs(): BlacklistPreferences

    fun usedImageGateway(): UsedImageGateway
    fun playingQueueGateway(): PlayingQueueGateway
    fun favoriteGateway(): FavoriteGateway
    fun recentSearches(): RecentSearchesGateway
    fun offlineLyrics(): OfflineLyricsGateway

    fun sharedPreferences(): SharedPreferences

    fun equalizer(): IEqualizer
    fun virtualizer(): IVirtualizer
    fun bassBoost(): IBassBoost

    fun cpuDispatcher(): ComputationScheduler
    fun ioDispatcher(): IoScheduler

    fun folderGateway2(): FolderGateway
    fun playlistGateway2(): PlaylistGateway
    fun songGateway2(): SongGateway
    fun albumGateway2(): AlbumGateway
    fun artistGateway2(): ArtistGateway
    fun genreGateway2(): GenreGateway
    fun podcastPlaylistGateway2(): PodcastPlaylistGateway
    fun podcastGateway2(): PodcastGateway
    fun podcastAlbumGateway2(): PodcastAlbumGateway
    fun podcastArtistGateway2(): PodcastArtistGateway

    fun encrypter(): IEncrypter

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance instance: Application): CoreComponent
    }

    companion object {
        private var component: CoreComponent? = null

        fun coreComponent(application: Application): CoreComponent {
            if (component == null) {
                component = DaggerCoreComponent.factory().create(application)
            }
            return component!!
        }
    }

}