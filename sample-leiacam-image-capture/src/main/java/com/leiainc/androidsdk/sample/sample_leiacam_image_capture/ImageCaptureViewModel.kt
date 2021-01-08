package com.leiainc.androidsdk.sample.sample_leiacam_image_capture

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.leiainc.androidsdk.photoformat.MultiviewImageDecoder
import com.leiainc.androidsdk.sbs.MultiviewSynthesizer2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageCaptureViewModel(application: Application): AndroidViewModel(application) {

    val quadBitmapLiveData: MutableLiveData<Bitmap> by lazy { MutableLiveData<Bitmap>() }

    lateinit var currentPhotoUri: Uri  // Saved location for clicked image.

    fun loadImage() = viewModelScope.launch(Dispatchers.IO) {
        val context = getApplication<Application>().applicationContext

        /* This function searches for the Uri of the file name stored on the internal storage as 'farm-lif.jpg'. */
        val multiviewImage = MultiviewImageDecoder.getDefault().decode(context, currentPhotoUri, 1280 * 720)

        /*  Decoder returns null if */
        if (multiviewImage != null) {
            val synthesizer2 = MultiviewSynthesizer2.createMultiviewSynthesizer(context)
            synthesizer2.populateDisparityMaps(multiviewImage)

            val quadBitmap = synthesizer2.toQuadBitmap(multiviewImage)
            quadBitmapLiveData.postValue(quadBitmap)
            return@launch
        }

        quadBitmapLiveData.postValue(null)
    }

    /**
     * Deletes image from files directory. Runs this on IO thread.
     * Clears live data to trigger UI update
     */
    fun clearImage() = viewModelScope.launch(Dispatchers.IO) {
        val context = getApplication<Application>().applicationContext
        context.contentResolver.delete(currentPhotoUri, null, null)

        quadBitmapLiveData.postValue(null)
    }
}