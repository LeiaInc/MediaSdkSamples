package com.leiainc.androidsdk.sample.sample_render_sbs

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.*
import com.leiainc.androidsdk.photoformat.IOUtils
import com.leiainc.androidsdk.photoformat.MultiviewImageDecoder
import com.leiainc.androidsdk.sample.sample_render_sbs.utils.DiskUtils
import com.leiainc.androidsdk.sbs.MultiviewSynthesizer2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SbsRenderViewModel(private val app: Application): AndroidViewModel(app) {

    val quadBitmapLiveData : MutableLiveData<Bitmap> by lazy {
        MutableLiveData<Bitmap>()
    }

    init {
        loadLifImageOnDisk()
    }

    private fun loadLifImageOnDisk() = viewModelScope.launch(Dispatchers.IO) {
        val context = app.applicationContext

        /* This function searches for the Uri of the file name stored on the internal storage as 'farm-lif.jpg'. */
        val fileUri = DiskUtils.saveResourceToFile(context, R.raw.cards_2x1)
        if (fileUri != null) {
            val multiviewImage = MultiviewImageDecoder.getDefault().decode(context, fileUri, 1280 * 720)
            /*  Decoder returns null if */
            if (multiviewImage != null) {
                val synthesizer2 = MultiviewSynthesizer2.createMultiviewSynthesizer(context)
                synthesizer2.populateDisparityMaps(multiviewImage)

                val quadBitmap = synthesizer2.toQuadBitmap(multiviewImage)
                quadBitmapLiveData.postValue(quadBitmap)
                return@launch
            }
        }

        quadBitmapLiveData.postValue(null)
    }
}
