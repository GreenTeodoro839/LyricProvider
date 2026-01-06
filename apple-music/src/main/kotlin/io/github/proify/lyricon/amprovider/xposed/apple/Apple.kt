package io.github.proify.lyricon.amprovider.xposed.apple

import android.app.Application
import android.media.MediaMetadata
import android.os.Handler
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.VagueType
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import de.robv.android.xposed.XposedHelpers.callMethod
import io.github.proify.lyricon.amprovider.xposed.Constants
import io.github.proify.lyricon.amprovider.xposed.apple.PlaybackState.PLAYING
import io.github.proify.lyricon.amprovider.xposed.apple.parser.AppleSongParser
import io.github.proify.lyricon.provider.LyriconProvider
import io.github.proify.lyricon.provider.ProviderLogo
import io.github.proify.lyricon.provider.common.util.ScreenStateMonitor
import io.github.proify.lyricon.provider.remote.ConnectionListener
import io.github.proify.lyricon.provider.remote.RemotePlayer
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy
import java.util.Timer
import java.util.TimerTask

object Apple : YukiBaseHooker() {
    private const val TAG = "Apple"
    private const val ICON: String =
        "iVBORw0KGgoAAAANSUhEUgAAAGAAAABhCAQAAACDzWwWAAADyUlEQVR42u2bTWxUVRTHf3fK0EShjCEVagyKFRaWtjOFdlNBFiYYwgKNmFgTFpoQP1YG4xqjbiQS3aqJH3HlAgyJWzEagXlEgxui2PLRYPADM0VGm5bK3wVjTJwB7r3zXt+d5J7lzDvnnt+955x73n3vQYeLCdc1raXMEAM8wHpmeY+Xzd/Bz6eKGtJuHdARzej/sjfYFdAyhilTpsIGum942TdmU/OPS3J0exWVhtv3U7BQuLfVj4sMIEM/FYapUKHPUbkrNwAV2dBwe5iedG1nCKAeyo0QGaCY1SipA+iuRmSX6V+MEpEKgAqsa4RImVWLm1VL2nb9SZ5mjGV51bK2ANTNQbbnu4cU2tLem7f77QLs7vBmTtcWtRWZMXekugIqhdBJFehwiQARIAJEgAgQ6g2Np1zjFFVOU+IFm7u3cAB+5jgJCYm50tjpT/N++ACzfMtxEqrmfNN/X4QbQuIHqiRU+c4s3Lh5Cw/gElWqJCSm1klJPMfJ646byc6qQpMkVKly0sx1UhmtkVx33FwKfAtRqen8eFrbVMjMfi17gM2Z2q9l3Upc5Vhn90J/3qSmx2YuAkSACBABIkAEiAARIAJEgAgQASJABIgAESAC2Eqx0wH6Ox1gT8b2szw9Vrdebzo6bmk/sMesup0yGxnjEVbaaQQCoDvZwjgPUmn9gnHAAOpjgl2M5pCN7eeA7tNHmpOt1IICkNFLmpWLhJTE6uJDnsp7J767Dd030nG/PYAd3vO/kxfzL3yDuuyXAyrqrHwkraeUKmhQr3LM+1uMR1t/DeMnDkmsexhjlFE2srytMXd56tW9AGQY4WHGGWV1SpM27qn3lzOA1vAcE6xJNXeWOn+99K9cdALQCl7j2Qz2iV5vzVMOABriU9ZmUr7q3ppH7Ze50uJLxtR6FV30KqLz6rUsoypxiBUZbiGJl9Zh85vt/L8tpbfRtLD/hIftBQ3but/r2CP+J7/Y1iFNO9s+YB//z8tXzliP8Zij5a+11L6Z2+od27/bXmgO8rGD3RPsNPP2AP7F86zDtc9w2G6xeIeHrNMXQBe8Q+gVt1sa7dfCLSx+ri3uW/05b4DHncca0Seab2Gpri+1TwNW4dhk9ASbvAJogT6fN0XVw2bWUaKLea5QY5pJLhj5N1sfeM7/Z/ncWDUn8RFPS/tDOdzrUd1j/g+FdD65z9n9n7Q6JIDbNOXk/oxGCEtUdminpzRIeKIRnbfq0d/ScsIUrdS7unoT58/pTfXn76e5xUHKBFtZT4kS8Ad1fuUMU/zIV+b7MCb6H3sPrs7pWSU4AAAAAElFTkSuQmCC"

    private lateinit var provider: LyriconProvider
    private lateinit var player: RemotePlayer

    private var exoMediaPlayerInstance: Any? = null
    private var getPositionMethod: Method? = null
    private lateinit var application: Application
    private lateinit var classLoader: ClassLoader
    private var playerLyricsViewModel: Any? = null
    private val handler: Handler by lazy { Handler(application.mainLooper) }

    private var playing = false
    private var currentId: String? = null

    override fun onHook() {
        onAppLifecycle {
            onCreate { onAppCreate() }
        }
    }

    private fun onAppCreate() {
        application = appContext ?: return
        classLoader = application.classLoader
        DiskSongManager.init(application)
        initProvider()
        initScreenStateMonitor()
        startHooks()
    }

    private fun startHooks() {
        hookExoMediaPlayer()

        classLoader.loadClass("com.apple.android.music.player.viewmodel.PlayerLyricsViewModel")
            .resolve()
            .firstMethod {
                name = "buildTimeRangeToLyricsMap"
            }
            .hook {
                after {
                    val arg: Any = args[0] ?: return@after
                    val songNative = callMethod(arg, "get")
                    val song = AppleSongParser.parser(songNative)
                    val id = song.musicId ?: return@after
                    DiskSongManager.save(song)
                    changeSong(id)
                }
            }
        hookMediaMetadataChange()
    }

    private fun hookMediaMetadataChange() {
        findMediaMetadataChangeMethod()?.hook {
            after {
                val mediaMetadata = args[0] as? MediaMetadata ?: return@after
                val metadata = MediaMetadataStore.addMetadata(mediaMetadata) ?: return@after
                val id = metadata.id
                val title = metadata.title
                val artist = metadata.artist
                val duration = metadata.duration
                if (id == currentId) return@after

                currentId = id
                changeSong(id)
            }
        }
    }

    private fun hookExoMediaPlayer() {
        val clazz =
            classLoader.loadClass("com.apple.android.music.playback.player.ExoMediaPlayer")
        clazz.declaredConstructors.forEach { constructor ->
            constructor.hook {
                after {
                    exoMediaPlayerInstance = instanceOrNull
                    getPositionMethod =
                        exoMediaPlayerInstance?.javaClass?.getDeclaredMethod("getCurrentPosition")
                }
            }
        }
        clazz.resolve()
            .firstMethod {
                name = "seekToPosition"
                parameters(Long::class)
            }
            .hook {
                after {
                    val position = args(0).cast<Long>() ?: 0
                    seekToPosition(position.toInt())
                }
            }

        classLoader.loadClass("com.apple.android.music.playback.controller.LocalMediaPlayerController")
            .resolve()
            .method {
                name = "onPlaybackStateChanged"
                parameters(VagueType, Int::class, Int::class)
            }
            .first()
            .hook {
                after {
                    when (PlaybackState.of(args[2] as Int)) {
                        PLAYING -> onPlay()
                        else -> onPause()
                    }
                }
            }

    }

    private fun callDownloadLyric(mediaId: String) {
        val playbackItemClass = classLoader.loadClass("com.apple.android.music.model.PlaybackItem")

        val playbackItem = Proxy.newProxyInstance(
            classLoader,
            arrayOf<Class<*>>(playbackItemClass)
        ) { _, method, _ ->
            when (method.name) {
                "hasLyrics" -> true
                "getId" -> mediaId
                "getQueueId" -> 0L
                else -> null
            }
        }

        if (playerLyricsViewModel == null) {
            playerLyricsViewModel =
                classLoader.loadClass("com.apple.android.music.player.viewmodel.PlayerLyricsViewModel")
                    .getConstructor(Application::class.java)
                    .newInstance(application)
        }
        callMethod(playerLyricsViewModel, "loadLyrics", playbackItem)
    }

    private fun findMediaMetadataChangeMethod() =
        classLoader.loadClass("android.support.v4.media.MediaMetadataCompat")
            .declaredMethods
            .filter {
                it.runCatching {
                    Modifier.isPublic(modifiers) &&
                            Modifier.isStatic(modifiers) &&
                            parameterCount == 1 &&
                            returnType.simpleName.contains("MediaMetadata")
                }.getOrDefault(false)
            }
            .getOrNull(0)

    private var timer: Timer? = null

    private fun onPlay() {
        if (playing) return
        playing = true
        player.setPlaybackState(true)

        if (timer != null) timer?.cancel()

        timer = Timer().apply {
            val timerTask = object : TimerTask() {
                override fun run() {
                    setPosition(getPosition())
                }
            }
            schedule(timerTask, 0, 50)
        }

        YLog.debug("onPlay")
    }

    private fun onPause() {
        playing = false
        player.setPlaybackState(false)
        if (timer != null) timer?.cancel()

        YLog.debug("onPause")
    }

    private fun setPosition(position: Int) {
        if (playing.not()) return
        player.setPosition(position)
    }

    private fun seekToPosition(position: Int) {
        if (playing.not()) return
        player.seekTo(position)
    }

    private fun changeSong(id: String?) {
        if (id == null) return

        if (DiskSongManager.hasCache(id)) {
            val song = DiskSongManager.load(id)
            song?.let {
                YLog.debug("changeSong: ${song.name}, isActivated=${player.isActivated}")
                player.setSong(song.toSong())
            }
        } else {
            YLog.debug("callDownloadLyric")
            callDownloadLyric(id)
        }
    }

    private fun getPosition(): Int {
        val position = getPositionMethod?.invoke(exoMediaPlayerInstance) as Long
        return position.toInt()
    }

    private fun initProvider() {
        provider = LyriconProvider(
            application,
            Constants.APP_PACKAGE_NAME,
            Constants.APPLE_MUSIC_PACKAGE_NAME,
            ProviderLogo.fromBase64(ICON)
        )

        val service = provider.service
        service.addConnectionListener(object : ConnectionListener {
            override fun onConnected(provider: LyriconProvider) {
                YLog.debug("onConnected")
            }

            override fun onReconnected(provider: LyriconProvider) {
                if (playing) {
                    player.setPlaybackState(true)
                    changeSong(currentId)
                    YLog.debug("onConnected: $currentId")
                }
            }

            override fun onDisconnected(provider: LyriconProvider) {
                YLog.debug("onDisconnected")
            }

            override fun onConnectTimeout(provider: LyriconProvider) {
                YLog.debug("onConnectTimeout")
            }
        })

        player = service.player
        provider.register()
    }

    private fun initScreenStateMonitor() {
        ScreenStateMonitor.initialize(application)
        ScreenStateMonitor.addListener(object : ScreenStateMonitor.ScreenStateListener {
            override fun onScreenOn() {
            }

            override fun onScreenOff() {

            }

            override fun onScreenUnlocked() {

            }
        })
    }

}