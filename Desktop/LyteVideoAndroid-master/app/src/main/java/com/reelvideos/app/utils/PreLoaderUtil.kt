package com.reelvideos.app.utils

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheKeyFactory
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import com.reelvideos.app.R
import com.reelvideos.app.base.CoreApp.simpleCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PreLoaderUtil {

    private var cachingJob: Job? = null

     fun preCacheVideo(videoURL:String?, context: Context) {
            val videoUri = Uri.parse(videoURL)
            val dataSpec = DataSpec(videoUri, 0, 200 * 1024, null)
            val defaultCacheKeyFactory = CacheUtil.DEFAULT_CACHE_KEY_FACTORY
            val progressListener =
                    CacheUtil.ProgressListener { requestLength, bytesCached, _ ->
                        val downloadPercentage: Double = (bytesCached * 100.0
                                / requestLength)
                        Log.e("TAGGED", "$downloadPercentage%")
                    }
            val dataSource: DataSource =
                    DefaultDataSourceFactory(
                            context,
                            Util.getUserAgent(context, context.getString(R.string.app_name))).createDataSource()

            cachingJob = GlobalScope.launch(Dispatchers.IO) {
                cacheVideo(dataSpec, defaultCacheKeyFactory, dataSource, progressListener)
            }

    }

    private fun cacheVideo(
            dataSpec: DataSpec,
            defaultCacheKeyFactory: CacheKeyFactory?,
            dataSource: DataSource,
            progressListener: CacheUtil.ProgressListener
    ) {
        try {
            CacheUtil.cache(
                    dataSpec,
                    simpleCache,
                    defaultCacheKeyFactory,
                    dataSource,
                    progressListener,
                    null
            )
        } catch (exception : Exception){

        }

    }
}