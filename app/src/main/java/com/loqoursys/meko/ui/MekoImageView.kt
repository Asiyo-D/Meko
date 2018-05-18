package com.loqoursys.meko.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.loqoursys.meko.listener.ThumbnailListener
import utils.getThumbnail

/**
 * Created by root on 4/25/18 for LoqourSys
 */
class MekoImageView : ImageView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private val imgPlaceholder = ColorDrawable(Color.WHITE)

    private val requestOptions: RequestOptions = RequestOptions()
            .placeholder(imgPlaceholder)
            .centerCrop()

//    private var mBlurFactor = DEFAULT_BLUR_FACTOR

    fun loadURL(imageURL: String, callback: LoadingRequestListener.Callback?) {
        getThumbnail(imageURL, object : ThumbnailListener {
            override fun thumbnail(thumbnailURL: String?) {
                loadImageIntoView(thumbnailURL, imageURL, callback)
            }
        })
    }

    fun loadImageIntoView(thumbnailUrl: String?, imageURL: String,
                          callback: LoadingRequestListener.Callback?) {

        if (thumbnailUrl != null) {
            Glide.with(this)
                    .load(imageURL)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .thumbnail(Glide.with(this)
                            .load(thumbnailUrl)
                            .apply(requestOptions))
                    .apply(requestOptions)
                    .listener(LoadingRequestListener(callback))
                    .into(this)
        } else {
            Glide.with(this)
                    .load(imageURL)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(requestOptions)
                    .listener(LoadingRequestListener(callback))
                    .into(this)
        }

    }
//
//    private fun getBlurBitmap(loadedBitmap: Bitmap): Bitmap = FastBlurUtil.doBlur(loadedBitmap
//            .copy(loadedBitmap.config, true), mBlurFactor, true)

    class LoadingRequestListener(private val callback: Callback?)
        : RequestListener<Drawable> {

        init {

        }

        interface Callback {
            fun onFailure(message: String)
            fun onComplete()
        }

        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?,
                                  isFirstResource: Boolean): Boolean {
            callback?.onFailure("Could not load images check your connection")
            target?.onLoadFailed(defaultDrawable)
            return false
        }

        override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>,
                                     dataSource: DataSource, isFirstResource: Boolean): Boolean {
//            target.onResourceReady(resource,
            callback?.onComplete()
//                    DrawableCrossFadeTransition(resource,1000,isFirstResource))
            return false
        }
    }


    companion object {
//        const val DEFAULT_BLUR_FACTOR = 8
        private val DEFAULT_COLOR = Color.parseColor("#66CCCCCC")
        val defaultDrawable = ColorDrawable(DEFAULT_COLOR)
    }
}
