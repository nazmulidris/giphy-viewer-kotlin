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

package instrumentedTests

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.developerlife.giphyviewer.DataEvent
import com.developerlife.giphyviewer.MyAndroidViewModel
import com.developerlife.giphyviewer.MyApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class ViewModelTrendingRequest {

  @Test
  fun makeRequest() {
    val appContext = ApplicationProvider.getApplicationContext<MyApplication>()
    val viewModel = MyAndroidViewModel(appContext)

    val latch = CountDownLatch(2)
    run {
      runOnUiThread {
        viewModel.dataEventObservable.observeForever { latch.countDown() }
      }
      viewModel.setTrendingMode()
      viewModel.requestRefreshData { latch.countDown() }
    }
    latch.await()

    val dataEvent = viewModel.dataEventObservable.value
    assertThat(dataEvent).isNotNull
    assertThat(dataEvent).isInstanceOf(DataEvent.Refresh::class.java)
    println("dataEvent: $dataEvent")
  }

}

@RunWith(AndroidJUnit4::class)
class ViewModelTrendingMoreRequest {

  @Test
  fun makeRequest() {
    val appContext = ApplicationProvider.getApplicationContext<MyApplication>()
    val viewModel = MyAndroidViewModel(appContext)

    // First, make the Trending Refresh Request.
    run {
      val latch = CountDownLatch(2)
      run {
        runOnUiThread {
          viewModel.dataEventObservable.observeForever { latch.countDown() }
        }
        viewModel.setTrendingMode()
        viewModel.requestRefreshData { latch.countDown() }
      }
      latch.await()
    }

    // Make the Trending More Request.
    run {
      val latch = CountDownLatch(2)
      run {
        runOnUiThread {
          viewModel.dataEventObservable.observeForever { latch.countDown() }
        }
        viewModel.requestMoreData { latch.countDown() }
      }
      latch.await()

      val dataEvent = viewModel.dataEventObservable.value
      assertThat(dataEvent).isNotNull
      assertThat(dataEvent).isInstanceOf(DataEvent.More::class.java)
      println("dataEvent: $dataEvent")
    }

  }

}