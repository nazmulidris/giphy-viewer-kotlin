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

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.giphy.sdk.core.models.Media
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.*
import kotlin.collections.ArrayList

class MyViewModel(application: Application) :
  AndroidViewModel(application), AnkoLogger {

  // RecyclerView position.
  /** Current scrolled position of RecyclerView. */
  var position = 0

  // Broadcast underlying data storage changes.
  /** DataEvent observable */
  val responseObservable = MutableLiveData<NetworkServiceResponse>()

  // Lifecycle hooks.
  override fun onCleared() {
    super.onCleared()
    info { "MyViewModel.onCleared: model is destroyed" }
  }

  // Manage app modes.
  var appMode: AppMode = AppMode.Trending()

  // Underlying data storage.
  private val underlyingData = ArrayList<Media>()
  val data: List<Media>
    get() = Collections.unmodifiableList(underlyingData)

  // Methods called from UI that generate network service requests.
  fun requestRefreshData(runOnComplete: BlockLambda? = null) {
    getApplication<MyApplication>().giphyClient
        .makeRequest(appMode = appMode,
                     responseHandler = object : GiphyClientResponseHandler {
                       override fun onComplete() {
                         runOnComplete?.invoke()
                       }

                       override fun onOkResponse(mediaList: List<Media>) {
                         responseRefreshData(mediaList)
                       }

                       override fun onErrorResponse() {
                         responseError()
                       }
                     })
  }

  fun requestMoreData(runOnComplete: BlockLambda? = null) {
    getApplication<MyApplication>().giphyClient
        .makeRequest(appMode = appMode,
                     offset = underlyingData.size,
                     responseHandler = object : GiphyClientResponseHandler {
                       override fun onComplete() {
                         runOnComplete?.invoke()
                       }

                       override fun onOkResponse(mediaList: List<Media>) {
                         responseUpdateData(mediaList)
                       }

                       override fun onErrorResponse() {
                         responseError()
                       }
                     }
        )
  }

  // Methods that modify and update RecyclerView.
  private fun responseRefreshData(newData: List<Media>) {
    underlyingData.clear()
    underlyingData.addAll(newData)
    info { "resetData: data size: ${underlyingData.size}" }
    info { prettyPrint(underlyingData) }
    responseObservable.value = NetworkServiceResponse.Refresh()
  }

  private fun responseUpdateData(newData: List<Media>) {
    underlyingData.addAll(newData)
    info { "updateData: data size: ${underlyingData.size}" }
    info { prettyPrint(underlyingData) }
    responseObservable.value =
        NetworkServiceResponse.More(newData.size)
  }

  private fun responseError() {
    info { "errorData" }
    responseObservable.value = NetworkServiceResponse.Error()
  }

  private fun prettyPrint(data: List<Media>): String {
    val buffer = StringBuffer()
    for (media in data) {
      buffer.append("id: ${media.id}, url ${media.bitlyGifUrl}\n")
    }
    return buffer.toString()
  }
}