/*
 * Copyright (C) 2020 Trinity. All rights reserved.
 * Copyright (C) 2020 Wang LianJie <wlanjie888@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.reelvideos.app.recorder.editor

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reelvideos.app.R
import com.reelvideos.app.recorder.adapter.CategoryAdapter
import com.reelvideos.app.recorder.adapter.SubtitleAdapter
import com.reelvideos.app.recorder.entity.ResourceForm
import com.reelvideos.app.recorder.entity.SubtitleInfo
import com.reelvideos.app.recorder.view.PasteUISimpleImpl
import com.reelvideos.app.recorder.view.PasteUITextImpl
import com.reelvideos.app.recorder.view.SpacesItemDecoration

class SubtitleChooser : Chooser {

  private var mSubtitles: MutableList<ResourceForm> ?= null
  private var mOnSubtitleItemClickListener: OnSubtitleItemClickListener ?= null

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  override fun init() {
    mSubtitles = mutableListOf()
    val view: View = LayoutInflater.from(context).inflate(R.layout.subtitle_view, this)
//    initListener()
    initResourceLocal()
    val recyclerView: RecyclerView = view.findViewById(R.id.effect_list)
    recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    recyclerView.addItemDecoration(SpacesItemDecoration(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt(), 1))
    val subtitleAdapter = SubtitleAdapter(context) {
      mOnSubtitleItemClickListener?.onSubtitleItemClick(it)
    }
//    subtitleAdapter.setOnItemClickListener(mOnItemClickListener)
    recyclerView.adapter = subtitleAdapter
    subtitleAdapter.showFontData()
    val categoryRecyclerView = view.findViewById<RecyclerView>(R.id.category_list)
    categoryRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    val adapter = CategoryAdapter(context) {

    }
    adapter.addShowFontCategory()
    categoryRecyclerView.adapter = adapter
    mSubtitles?.let {
      adapter.setData(it)
    }
  }

  private fun initResourceLocal() {
    val form = ResourceForm()
    form.isMore = true
    mSubtitles?.add(form)
  }

  override fun isPlayerNeedZoom(): Boolean {
    return true

  }

  override fun getThumbContainer(): FrameLayout? {
    return findViewById(R.id.fl_thumblinebar)
  }

  override fun isHostPaste(paste: PasteUISimpleImpl): Boolean {
    return paste is PasteUITextImpl
  }

  fun setOnSubtitleItemClickListener(l: OnSubtitleItemClickListener) {
    mOnSubtitleItemClickListener = l
  }

  interface OnSubtitleItemClickListener {
    fun onSubtitleItemClick(info: SubtitleInfo)
  }
}