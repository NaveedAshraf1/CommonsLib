package com.lymors.lycommons.utils

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.lymors.lycommons.R


interface MyClickListener{
    fun onClick(mediaItem: MediaModel)
}

class SliderViewPagerAdapter(private val mediaList: List<MediaModel>, activity: FragmentActivity, private var  onClick: MyClickListener) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = mediaList.size
    override fun createFragment(position: Int ): Fragment =
        SliderFragment.newInstance(mediaList[position], onClick)
}


class SliderFragment(var onClick: MyClickListener) : Fragment() {

    private lateinit var mediaItem: MediaModel
    private var exoPlayer: SimpleExoPlayer? = null
    private lateinit var imageView: ImageView
    private lateinit var playerView: PlayerView
    private lateinit var playButtonImg: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var rootView: FrameLayout // Define rootView here

    companion object {
        fun newInstance(mediaItem: MediaModel, onClick: MyClickListener): SliderFragment {
            val fragment = SliderFragment(onClick)
            fragment.mediaItem = mediaItem
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        rootView = FrameLayout(context)
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        rootView.layoutParams = layoutParams

        imageView = ImageView(context)
        val imageViewLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        imageView.layoutParams = imageViewLayoutParams
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        rootView.addView(imageView)

        imageView.setOnClickListener {
            onClick.onClick(mediaItem)
        }



        playerView = PlayerView(context)
        val playerViewLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        playerView.layoutParams = playerViewLayoutParams
        rootView.addView(playerView)



        playButtonImg = ImageView(context)
        val imageLayoutParams = FrameLayout.LayoutParams(100, 100)
        imageLayoutParams.gravity = Gravity.CENTER
        playButtonImg.layoutParams = imageLayoutParams
        playButtonImg.scaleType = ImageView.ScaleType.CENTER_INSIDE
        playButtonImg.setImageResource(R.drawable.play_button)
        rootView.addView(playButtonImg)

        progressBar = ProgressBar(context)
        val progressBarLayoutParams = FrameLayout.LayoutParams(70, 70)
        progressBarLayoutParams.gravity = Gravity.CENTER
        progressBar.layoutParams = progressBarLayoutParams
        rootView.addView(progressBar)


        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (mediaItem.mediaType == MediaType.IMAGE) {
            progressBar.visibility = View.GONE
            playButtonImg.visibility = View.GONE
            playerView.visibility = View.GONE
            imageView.visibility = View.VISIBLE

            when (mediaItem.mediaResourceType) {
                ResourceType.DRAWABLE -> {
                    val resourceId = context?.resources?.getIdentifier(mediaItem.media.substringAfterLast("."), "drawable", context?.packageName)
                    resourceId?.let {
                        Glide.with(requireContext()).load(resourceId).into(imageView)
                    }
                }

                ResourceType.URL -> {
                    Glide.with(requireContext()).load(mediaItem.media).into(imageView)
                }

                ResourceType.URI -> {
                    Glide.with(requireContext()).load(Uri.parse(mediaItem.media)).into(imageView)
                }

                ResourceType.RAW -> {
                    val resourceId = context?.resources?.getIdentifier(mediaItem.media.substringAfterLast("."), "raw", context?.packageName)
                    resourceId?.let {
                        Glide.with(requireContext()).load(resourceId).into(imageView)
                    }
                }
            }
        } else {
            progressBar.visibility = View.GONE
            playButtonImg.visibility = View.VISIBLE
            imageView.visibility = View.VISIBLE
            playerView.visibility = View.GONE

            when (mediaItem.thumbnailResourceType) {
                ResourceType.DRAWABLE -> {
                    val resourceId = context?.resources?.getIdentifier(mediaItem.thumbnail.substringAfterLast("."), "drawable", context?.packageName)
                    resourceId?.let {
                        Glide.with(requireContext()).load(resourceId).into(imageView)
                    }
                }

                ResourceType.URL -> {
                    Glide.with(requireContext()).load(mediaItem.thumbnail).into(imageView)
                }

                ResourceType.URI -> {
                    Glide.with(requireContext()).load(Uri.parse(mediaItem.thumbnail))
                        .into(imageView)
                }

                ResourceType.RAW -> {
                    val resourceId = context?.resources?.getIdentifier(mediaItem.thumbnail.substringAfterLast("."), "raw", context?.packageName)
                    resourceId?.let {
                        Glide.with(requireContext()).load(resourceId).into(imageView)
                    }
                }
            }



            playButtonImg.setOnClickListener {
                playButtonImg.visibility = View.GONE
                imageView.visibility = View.GONE
                playerView.visibility = View.VISIBLE

                if (exoPlayer == null) initializePlayer()

                exoPlayer?.play()
            }


        }
    }

    private fun initializePlayer() {
        exoPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        playerView.player = exoPlayer

        when (mediaItem.mediaResourceType) {
            ResourceType.DRAWABLE -> {}
            ResourceType.URL -> {
                exoPlayer?.setMediaItem(MediaItem.fromUri(mediaItem.media))
            }
            ResourceType.URI -> {
                exoPlayer?.setMediaItem(MediaItem.fromUri(mediaItem.media))
            }
            ResourceType.RAW -> {
                val resourceId = context?.resources?.getIdentifier(mediaItem.media.substringAfterLast("."), "raw", context?.packageName)
                resourceId?.let { resId ->
                    val rawUri = Uri.parse("android.resource://" + context?.packageName + "/" + resId)
                    val mediaItem = MediaItem.fromUri(rawUri)
                    exoPlayer?.setMediaItem(mediaItem)
                }
            }

        }


        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                progressBar.visibility =
                    if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
            }
        })

        exoPlayer?.prepare()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer?.release()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }
}

data class MediaModel(
    var mediaType: MediaType = MediaType.IMAGE,
    var mediaResourceType: ResourceType = ResourceType.URL,
    var media: String = "",
    var thumbnailResourceType: ResourceType = ResourceType.URL,
    var thumbnail: String = ""
)

enum class MediaType { VIDEO, IMAGE }

enum class ResourceType { URL, URI, DRAWABLE, RAW }

fun setUpMediaSlider(
    context: FragmentActivity,
    relativeLayout: RelativeLayout,
    mediaItems: List<MediaModel>,
    showPosition: Boolean = false,
    onClick : MyClickListener
) {

    val viewPager = ViewPager2(context).apply {
        id = View.generateViewId()
        layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            300.dpToPx(context)
        ).apply {
            addRule(RelativeLayout.ALIGN_PARENT_TOP)
        }
    }

    val cardView = CardView(context).apply {
        layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            addRule(RelativeLayout.CENTER_HORIZONTAL)
            bottomMargin = 8.dpToPx(context)
        }

        radius = 20f
        cardElevation = 0f
        setCardBackgroundColor(Color.parseColor("#80000000"))
    }



    val textViewPosition = TextView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setPadding(8.dpToPx(context), 4.dpToPx(context), 8.dpToPx(context), 4.dpToPx(context))
        }
        setTextColor(Color.WHITE)
        textSize = 18f
        text = "1/${mediaItems.size}"
    }
    if (showPosition) {
        textViewPosition.visibility = View.VISIBLE
    }else{
        textViewPosition.visibility = View.GONE
    }

    cardView.addView(textViewPosition)
    relativeLayout.addView(viewPager)
    relativeLayout.addView(cardView)
    viewPager.adapter = SliderViewPagerAdapter(mediaItems, context , onClick )
    viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            textViewPosition.text = "${position + 1}/${mediaItems.size}"
        }
    })
}

fun Int.dpToPx(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context.resources.displayMetrics
    ).toInt()
}


//
// languages 12
// 3 intro
// categories
// searchView
// selection of stickers
// sticker select and share
// app lovin ads
//
//
//
//












