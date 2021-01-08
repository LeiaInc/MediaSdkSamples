package com.leiainc.androidsdk.sample.sample_leiacam_image_capture

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.leiainc.androidsdk.display.LeiaDisplayManager
import com.leiainc.androidsdk.display.LeiaSDK
import com.leiainc.androidsdk.sample.sample_leiacam_image_capture.databinding.ActivityImageCaptureBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImageCaptureActivity : AppCompatActivity() {

    private val viewModel by viewModels<ImageCaptureViewModel>()

    private var _binding: ActivityImageCaptureBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 3391
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityImageCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.takePhotoBtn.setOnClickListener {
            dispatchTakePictureIntent()
        }

        val quadBitmapObserver = Observer<Bitmap> { quadBitmap ->
            // Observe LiveData to update UI when Quad Bitmap is available.
            binding.quadView.setQuadBitmap(quadBitmap)
            toggleImageUI(quadBitmap != null)
            toggleBacklight(quadBitmap != null)
        }

        viewModel.quadBitmapLiveData.observe(this, quadBitmapObserver)

        binding.cancelButton.setOnClickListener {
            /*  Delete image and Clear live data. */
            viewModel.clearImage()

            toggleBacklight(false)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }

                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "sample.leiacam.capture.provider",
                            it
                    )

                    viewModel.currentPhotoUri = photoURI
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            viewModel.loadImage()
        }
    }

    private fun toggleImageUI(showImage: Boolean) {
        if (showImage) {
            binding.cancelButton.visibility = View.VISIBLE
            binding.quadView.visibility = View.VISIBLE
            binding.takePhotoBtn.visibility = View.GONE
        } else {
            binding.cancelButton.visibility = View.GONE
            binding.quadView.visibility = View.GONE
            binding.takePhotoBtn.visibility = View.VISIBLE
        }
    }

    private fun toggleBacklight(backlightEnabled : Boolean) {
        val displayManager: LeiaDisplayManager? = LeiaSDK.getDisplayManager(applicationContext)
        val backlightMode = if (backlightEnabled) LeiaDisplayManager.BacklightMode.MODE_3D else LeiaDisplayManager.BacklightMode.MODE_2D
        displayManager?.requestBacklightMode(backlightMode)
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.quadBitmapLiveData.value != null) {
            /*  Switch off backlight only if image is loaded. */
            toggleBacklight(false)
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.quadBitmapLiveData.value != null) {
            /*  Switch on backlight only if image is loaded. */
            toggleBacklight(true)
        }
        /*  Make app fullscreen */
        setFullScreenImmersive()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val displayManager: LeiaDisplayManager? = LeiaSDK.getDisplayManager(applicationContext)
        displayManager?.onWindowFocusedChanged(hasFocus)
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
