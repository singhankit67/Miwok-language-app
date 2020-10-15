package com.reelvideos.app.recorder.listener

import com.reelvideos.app.recorder.entity.Effect

interface OnEffectTouchListener {

  fun onEffectTouchEvent(event: Int, effect: Effect)
}