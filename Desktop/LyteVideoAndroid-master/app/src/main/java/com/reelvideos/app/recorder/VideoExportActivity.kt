package com.reelvideos.app.recorder

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceManager
import com.airbnb.lottie.LottieAnimationView
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.reelvideos.app.PostActivity
import com.reelvideos.app.R
import com.reelvideos.app.config.Constants
import com.reelvideos.app.config.Constants.OUTPUT_FILE_PROCESSED
import com.reelvideos.app.recorder.entity.MediaItem
//import com.reelvideos.app.utils.DialogCreator
import com.timqi.sectorprogressview.ColorfulRingProgressView
import com.trinity.core.TrinityCore
import com.trinity.editor.VideoExport
import com.trinity.editor.VideoExportInfo
import com.trinity.listener.OnExportListener
import kotlinx.android.synthetic.main.activity_video_export.*
import kotlinx.android.synthetic.main.fragment_musicdiscover.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by wlanjie on 2019-07-30
 * Improved by pushpedra-starlord
 */
class VideoExportActivity : AppCompatActivity(), OnExportListener {

    //private lateinit var mProgressBar: LottieAnimationView
    private lateinit var mVideoView: VideoView
    private lateinit var mVideoExport: VideoExport
    private var musicPath = ""
    private var musicName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_export)
        //mProgressBar = findViewById(R.id.progress_view)
        mVideoView = findViewById(R.id.video_view)
        mVideoView.setOnPreparedListener {
            it.isLooping = true
        }

        if (intent.getStringExtra("musicPath").toString() != null)
            musicPath = intent.getStringExtra("musicPath").toString()


        if (intent.getStringExtra("musicName") != null)
            musicName = intent.getStringExtra("musicName").toString()

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val softCodecEncode = preferences.getBoolean("export_soft_encode", false)
        val softCodecDecode = preferences.getBoolean("export_soft_decode", false)
        val info = VideoExportInfo(OUTPUT_FILE_PROCESSED)
        info.mediaCodecDecode = !softCodecDecode
        info.mediaCodecEncode = !softCodecEncode
        //DialogCreator.showProgressDialog(this)
        loaderanimationforvideoexport.visibility = View.VISIBLE
        loaderanimationforvideoexport.playAnimation()
        val width = resources.displayMetrics.widthPixels
        val params = mVideoView.layoutParams as ConstraintLayout.LayoutParams
        params.width = width
        params.height = ((width * (info.height * 1.0f / info.width)).toInt())
        mVideoView.layoutParams = params
        mVideoExport = TrinityCore.createExport(this)
        mVideoExport.export(info, this)
    }

    override fun onExportProgress(progress: Float) {
        loaderanimationforvideoexport.visibility = View.VISIBLE
        loaderanimationforvideoexport.playAnimation()
    }

    override fun onExportFailed(error: Int) {
    }

    override fun onExportCanceled() {

        val file = File(OUTPUT_FILE_PROCESSED)
        file.delete()
        //DialogCreator.cancelProgressDialog()
        loaderanimationforvideoexport.visibility = View.GONE

    }

    override fun onExportComplete() {
        if (musicPath.length > 5) {
            //mProgressBar.visibility = View.GONE
            mergeFile(OUTPUT_FILE_PROCESSED, musicPath)
        } else {
            // mProgressBar.visibility = View.GONE
            GotopostScreen()
        }
    }

    fun GotopostScreen() {
        val intent = Intent(this, PostActivity::class.java)
        //mProgressBar.visibility = View.GONE
        loaderanimationforvideoexport.visibility = View.GONE
        intent.putExtra("draft_file", "draft_file")
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun mergeFile(videoFileName: String, audioFileName: String) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val outputfilename = Constants.APP_SHOWING_DIR + "VIDEOFINAL_" + "temp2" + timeStamp + ".mp4"
        val f = File(outputfilename)
        if (f.exists()) f.delete()


        val command: String

        command = "-i $videoFileName -stream_loop -1 -i $audioFileName -map 0:v -map 1:a -c:v copy -shortest $outputfilename"

        FFmpeg.executeAsync(command) { executionId, returnCode ->
            Log.e("FFmpeg", "CODEx : $returnCode")
            Config.printLastCommandOutput(Log.INFO)
            if (returnCode == 0) {
                val uri: Uri = Uri.parse(outputfilename)
                var mp: MediaPlayer
                mp = MediaPlayer.create(this@VideoExportActivity, uri)
                var duration = mp.duration
                var height = mp.videoHeight
                var width = mp.videoWidth
                var type = "video"
                var path = outputfilename

                var item = MediaItem(path, type, width, height)

                val medias = mutableListOf<MediaItem>()
                medias.add(item)

                //mProgressBar.visibility = View.GONE
                loaderanimationforvideoexport.visibility = View.GONE

                val intent = Intent(this, EditorActivity::class.java)
                intent.putExtra("medias", medias.toTypedArray())
                intent.putExtra("musicName", musicName)
                startActivity(intent)

            } else {
                Toast.makeText(this@VideoExportActivity, "Error Music Merge", Toast.LENGTH_LONG).show()
                //mProgressBar.visibility = View.GONE
                loaderanimationforvideoexport.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mVideoExport.cancel()
    }
}