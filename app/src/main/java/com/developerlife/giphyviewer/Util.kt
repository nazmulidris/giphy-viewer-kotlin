/*
 * Copyright 2019 Nazmul Idris. All rights reserved.
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

package com.developerlife.giphyviewer

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.net.toUri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// Type aliases.

typealias BlockLambda = () -> Unit
typealias BlockWithSingleArgLambda<T> = (T) -> Unit

// Extension functions.

inline fun toast(
    context: Context,
    text: String = "",
    duration: Int = Toast.LENGTH_SHORT,
    crossinline block: Toast.() -> Unit
) {
  runOnUiThread {
    with(Toast.makeText(context, text, duration)) {
      block()
      show()
    }
  }
}

fun runOnUiThread(block: BlockLambda) {
  Handler(Looper.getMainLooper()).post { block.invoke() }
}

// Coroutines.

suspend fun shorten(longUrl: String): String {
  return suspendCoroutine { promise ->
    try {
      val shortUrl = shortenUrl(longUrl)
      promise.resume(shortUrl)
    }
    catch (e: Exception) {
      promise.resume(longUrl)
    }
  }
}

// Helper functions.

fun shortenUrl(longUrl: String): String {
  val encodedUri = longUrl.toUri().toString()

  val tinyUrl = "https://tinyurl.com/api-create.php?url=${URLEncoder.encode(
      encodedUri, "UTF-8")}"

  val connection = URL(tinyUrl).openConnection() as HttpURLConnection
  connection.requestMethod = "GET"
  val reader = BufferedReader(InputStreamReader(connection.inputStream))

  val response = StringBuilder()

  var line: String? = ""

  while (line != null) {
    line = reader.readLine()
    line?.apply { response.append(this) }
  }

  reader.close()

  return response.toString()
}