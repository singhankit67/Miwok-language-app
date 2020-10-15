package com.trinity.editor

import com.trinity.listener.OnExportListener

interface VideoExport {

  /**
   * Start export
   * @param path The address where the recorded video is saved
   * @param width The width of the recorded video, the SDK will do a 16 times integer operation, The width of the final output video may be inconsistent with the setting
   * @param height High of recorded video, SDK Will do 16 times integer operations, The width of the final output video may be inconsistent with the setting
     * @param videoBitRate Video output bit rate, If the setting is 2000, Then 2M, The final output and the setting may be different
   * @param frameRate Video output frame rate
   * @param sampleRate Audio sampling rate
   * @param channelCount Number of audio channels
   * @param audioBitRate Audio bit rate
   * @param l Export callback including success, failure and progress callback
   * @return Int ErrorCode. SUCCESS For success, Other failures
   */
  fun export( info: VideoExportInfo, l: OnExportListener): Int

  /**
   * Uncomposite
   */
  fun cancel()

  /**
   * Release resources
   */
  fun release()
}