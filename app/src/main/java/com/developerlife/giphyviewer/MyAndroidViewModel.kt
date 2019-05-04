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

class MyAndroidViewModel(application: Application) :
  AndroidViewModel(application), AnkoLogger {

  /** Current scrolled position of RecyclerView. */
  var position = 0

  /** AppMode observable. */
  val appMode = MutableLiveData<AppMode>()

  /** DataEvent observable */
  val dataEvent = MutableLiveData<DataEvent>()

  init {
    appMode.value = AppMode.Trending()
    debug {
      "MyAndroidViewModel.init: set appMode to trending"
    }
  }

  // Change app modes.

  fun setTrendingMode() {
    appMode.value = AppMode.Trending()
  }

  fun setSearchMode(query: String) {
    appMode.value = AppMode.Search(query)
  }

  // Underlying data storage and getter.

  val underlyingData_ = ArrayList<Media>()
  val data: List<Media>
    get() = Collections.unmodifiableList(underlyingData_)

  // Methods called from UI that generate network service requests.

  fun requestRefreshData(runOnRefreshComplete: Runnable?) {
    // TODO migrate requestRefreshData & resetData
  }

  fun requestMoreData() {
    // TODO migrate requestMoreData & updateData
  }

}