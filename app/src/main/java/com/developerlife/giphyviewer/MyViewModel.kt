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
import org.jetbrains.anko.debug
import java.util.*
import kotlin.collections.ArrayList

class MyViewModel(application: Application) :
  AndroidViewModel(application), AnkoLogger {

  // RecyclerView position.

  /** Current scrolled position of RecyclerView. */
  var position = 0

  // Broadcast underlying data storage changes.

  /** DataEvent observable */
  val dataEventObservable = MutableLiveData<DataEvent>()

  // Lifecycle hooks.

  override fun onCleared() {
    super.onCleared()
    debug { "MyViewModel.onCleared: model is destroyed" }
  }

  // Manage app modes.

  var appMode: AppMode = AppMode.Trending()

  // Underlying data storage.

  private val underlyingData_ = ArrayList<Media>()
  val data: List<Media>
    get() = Collections.unmodifiableList(underlyingData_)

  // Methods called from UI that generate network service requests.

  fun requestRefreshData(runOnComplete: (() -> Unit)? = null) {
    getApplication<MyApplication>().giphyClient
        .makeRequest(appMode = appMode,
                     responseHandler = object : GiphyClientResponseHandler {
                       override fun onComplete() {
                         runOnComplete?.invoke()
                       }

                       override fun onResponse(mediaList: List<Media>) {
                         resetData(mediaList)
                       }

                       override fun onError() {
                         errorData()
                       }
                     })
  }

  fun requestMoreData(runOnComplete: (() -> Unit)? = null) {
    getApplication<MyApplication>().giphyClient
        .makeRequest(appMode = appMode,
                     offset = underlyingData_.size,
                     responseHandler = object : GiphyClientResponseHandler {
                       override fun onComplete() {
                         runOnComplete?.invoke()
                       }

                       override fun onResponse(mediaList: List<Media>) {
                         updateData(mediaList)
                       }

                       override fun onError() {
                         errorData()
                       }
                     }
        )
  }

  // Methods that modify underlyingData_ and update RecyclerView.

  private fun resetData(newData: List<Media>) {
    underlyingData_.clear()
    underlyingData_.addAll(newData)
    debug { "resetData: data size: ${underlyingData_.size}" }
    dataEventObservable.postValue(DataEvent.Refresh())
  }

  private fun updateData(newData: List<Media>) {
    debug { "updateData: data size: ${underlyingData_.size}" }
    dataEventObservable.postValue(DataEvent.More(newData.size))
  }

  private fun errorData() {
    debug { "errorData" }
    dataEventObservable.postValue(DataEvent.Error())
  }
}