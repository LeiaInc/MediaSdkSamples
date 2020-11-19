package com.leiainc.androidsdk.sample.sample_render_sbs

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.leiainc.androidsdk.core.QuadView
import com.leiainc.androidsdk.core.ScaleType
import com.leiainc.androidsdk.display.LeiaDisplayManager
import com.leiainc.androidsdk.display.LeiaSDK

class SbsRenderDemoActivity : AppCompatActivity() {

    private val sbsRenderViewModel by viewModels<SbsRenderViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sbs_render_demo)

        /*  Get reference to QuadView */
        val quadView: QuadView = findViewById(R.id.quad_view)

        /*  Set scale type to Fit center  */
        quadView.scaleType = ScaleType.FIT_CENTER

        val quadBitmapObserver = Observer<Bitmap> { quadBitmap ->
            // Observe LiveData to update UI when Quad Bitmap is available.
            if (quadBitmap != null) {
                quadView.setQuadBitmap(quadBitmap)
            } else {
                Toast.makeText(this, "Failed to retrieve Image", Toast.LENGTH_LONG).show()
            }
        }

        sbsRenderViewModel.quadBitmapLiveData.observe(this, quadBitmapObserver)
    }

    override fun onPause() {
        super.onPause()
        val displayManager: LeiaDisplayManager? = LeiaSDK.getDisplayManager(applicationContext)
        displayManager?.requestBacklightMode(LeiaDisplayManager.BacklightMode.MODE_2D)
    }

    override fun onResume() {
        super.onResume()
        val displayManager: LeiaDisplayManager? = LeiaSDK.getDisplayManager(applicationContext)
        displayManager?.requestBacklightMode(LeiaDisplayManager.BacklightMode.MODE_3D)

        /*  Make app full screen */
        setFullScreenImmersive()
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