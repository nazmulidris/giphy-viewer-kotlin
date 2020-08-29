/*
 * Copyright 2020 Nazmul Idris. All rights reserved.
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
import com.r3bl.giphyviewer.AppMode
import com.r3bl.giphyviewer.MyApplication
import com.r3bl.giphyviewer.MyViewModel
import com.r3bl.giphyviewer.NetworkServiceResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class ViewModelSearchRequest {

  @Test
  fun makeRequest() {
    val appContext = ApplicationProvider.getApplicationContext<MyApplication>()
    val viewModel = MyViewModel(appContext)

    val latch = CountDownLatch(2)
    run {
      runOnUiThread {
        viewModel.responseObservable.observeForever { latch.countDown() }
      }
      viewModel.appMode = AppMode.Search("hello")
      viewModel.requestRefreshData { latch.countDown() }
    }
    latch.await()

    val dataEvent = viewModel.responseObservable.value
    println("dataEvent: $dataEvent")
    assertThat(dataEvent).isNotNull
    assertThat(dataEvent).isInstanceOf(NetworkServiceResponse.Refresh::class.java)
  }

}

@RunWith(AndroidJUnit4::class)
class ViewModelSearchMoreRequest {

  @Test
  fun makeRequest() {
    val appContext = ApplicationProvider.getApplicationContext<MyApplication>()
    val viewModel = MyViewModel(appContext)

    // First, make the Search Refresh Request.
    run {
      val latch = CountDownLatch(2)
      run {
        runOnUiThread {
          viewModel.responseObservable.observeForever { latch.countDown() }
        }
        viewModel.appMode = AppMode.Search("hello")
        viewModel.requestRefreshData { latch.countDown() }
      }
      latch.await()
    }

    // Make the Search More Request.
    run {
      val latch = CountDownLatch(2)
      run {
        runOnUiThread {
          viewModel.responseObservable.observeForever { latch.countDown() }
        }
        viewModel.requestMoreData { latch.countDown() }
      }
      latch.await()

      val dataEvent = viewModel.responseObservable.value
      println("dataEvent: $dataEvent")
      assertThat(dataEvent).isNotNull
      assertThat(dataEvent).isInstanceOf(NetworkServiceResponse.More::class.java)
    }

  }

}