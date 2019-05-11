/*
 * Copyright 2019 R3BL LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.r3bl.giphyviewer

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.giphy.sdk.core.models.Media
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

/**
 * Displays a full screen animated GIF, given the URI that is passed in the
 * Intent that creates it. Fresco is used to actually load and render the
 * animated GIF.
 */
class FullScreenActivity : Activity(), AnkoLogger {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_full_screen)

    val imageUri = intent.data
    val width = intent.getIntExtra(WIDTH, 0)
    val height = intent.getIntExtra(HEIGHT, 0)

    val imageView = findViewById<SimpleDraweeView>(R.id.fullscreen_gif)

    imageView.aspectRatio = width.toFloat() / height.toFloat()
    imageView.controller = Fresco.newDraweeControllerBuilder()
        .setUri(imageUri)
        .setAutoPlayAnimations(true)
        .build()

    shortenUriAndCopyToClipboard(imageUri!!)
  }

  private fun shortenUriAndCopyToClipboard(imageUri: Uri) {
    val longUrl = imageUri.toString()
    GlobalScope.launch(Dispatchers.IO) {
      lateinit var shortUrl: String
      try {
        shortUrl = shorten(longUrl)
        info { "coroutine-shortUrl: $shortUrl" }
      }
      catch (e: Exception) {
        shortUrl = longUrl
        info { "coroutine-copyUrlToClipboard exception! $e" }
      }
      copyTextToClipboard(shortUrl)
      toast(this@FullScreenActivity) {
        setText("URL copied to clipboard")
      }
    }
  }

  private fun copyTextToClipboard(plainText: String) {
    val clipboard =
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(URL, plainText)
    clipboard.primaryClip = clip
  }

  companion object {

    val WIDTH = "width"
    val HEIGHT = "height"
    val URL = "url"

    fun getIntent(context: Context, item: Media): Intent {
      val intent = Intent(context, FullScreenActivity::class.java)
      val url = item.images.original.gifUrl
      intent.data = Uri.parse(url)
      val width = item.images.original.width
      intent.putExtra(WIDTH, width)
      val height = item.images.original.height
      intent.putExtra(HEIGHT, height)
      return intent
    }
  }
}