package com.reelvideos.app.recorder.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.reelvideos.app.recorder.fragment.PictureFragment
import com.reelvideos.app.recorder.fragment.VideoFragment

class MediaPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

  private val mItems = mutableListOf<Fragment>()

  init {
    mItems.add(VideoFragment())
//    mItems.add(PictureFragment())
  }

  override fun getItem(position: Int): Fragment {
    return mItems[position]
  }

  override fun getCount(): Int {
    return 1
  }

  override fun getPageTitle(position: Int): CharSequence? {
    return if (position == 0) {
      "video"
    } else {
      "image"
    }
  }
}