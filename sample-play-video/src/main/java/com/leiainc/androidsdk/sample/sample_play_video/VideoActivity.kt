package com.leiainc.androidsdk.sample.sample_play_video

import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Bundle
import android.view.Surface
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.leiainc.androidsdk.core.QuadView
import com.leiainc.androidsdk.core.SurfaceTextureReadyCallback
import com.leiainc.androidsdk.display.LeiaDisplayManager
import com.leiainc.androidsdk.display.LeiaSDK

class VideoActivity : AppCompatActivity(), SurfaceTextureReadyCallback {

    private lateinit var exoPlayer: SimpleExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        val quadView: QuadView = findViewById(R.id.quad_view)
        quadView.getInputSurfaceTexture(this)

        exoPlayer = SimpleExoPlayer.Builder(this).build()
    }

    override fun onSurfaceTextureReady(surfaceTexture: SurfaceTexture?) {

        exoPlayer.setVideoSurface(Surface(surfaceTexture))

        val mp4VideoUri =
            Uri.parse("asset:///cat_2x2.mp4")

        val userAgent = Util.getUserAgent(this, "exoplayerQuadExample")
        val dataSourceFactory = DefaultDataSourceFactory(this, userAgent)

        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mp4VideoUri)
        val loopingSource = LoopingMediaSource(videoSource)

        exoPlayer.prepare(loopingSource)
    }

    override fun onPause() {
        super.onPause()
        val displayManager: LeiaDisplayManager? = LeiaSDK.getDisplayManager(applicationContext)
        displayManager?.requestBacklightMode(LeiaDisplayManager.BacklightMode.MODE_2D)
        exoPlayer.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        val displayManager: LeiaDisplayManager? = LeiaSDK.getDisplayManager(applicationContext)
        displayManager?.requestBacklightMode(LeiaDisplayManager.BacklightMode.MODE_3D)

        /*  Make app full screen */
        setFullScreenImmersive()
        exoPlayer.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

    private fun setFullScreenImmersive() {
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        val decorView = window.decorView
        decorView.clearFocus()
        decorView.systemUiVisibility = flags

        // Code below is to handle presses of Volume up or Volume down.
        // Without this, after pressing volume buttons, the navigation bar will
        // show up and won't hide
        decorView.setOnSystemUiVisibilityChangeListener { visibility: Int ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                decorView.systemUiVisibility = flags
            }
        }
    }
}