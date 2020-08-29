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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.giphy.sdk.core.models.Media
import com.r3bl.giphyviewer.AppMode
import com.r3bl.giphyviewer.GiphyClient
import com.r3bl.giphyviewer.GiphyClientResponseHandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class NetworkServiceTest {

  @Test
  fun canMakeSearchRequest() {
    val client = GiphyClient()
    val latch = CountDownLatch(1)
    var results: List<Media>? = null

    client.makeRequest(
        AppMode.Search("hello"),
        object : GiphyClientResponseHandler {
          override fun onComplete() {
            latch.countDown()
          }

          override fun onOkResponse(mediaList: List<Media>) {
            results = mediaList
          }

          override fun onErrorResponse() {}
        },
        0)

    latch.await()

    results?.run {
      android.util.Log.d("search results", results!!.size.toString())
    }

    assertThat(results).isNotNull
    assertThat(results).isNotEmpty
  }

  @Test
  fun canMakeTrendingRequest() {
    val client = GiphyClient()
    val latch = CountDownLatch(1)
    var results: List<Media>? = null

    client.makeRequest(
        AppMode.Trending(),
        object : GiphyClientResponseHandler {
          override fun onComplete() {
            latch.countDown()
          }

          override fun onOkResponse(mediaList: List<Media>) {
            results = mediaList
          }

          override fun onErrorResponse() {}
        },
        0)

    latch.await()

    results?.run {
      android.util.Log.d("trending results", results!!.size.toString())
    }

    assertThat(results).isNotNull
    assertThat(results).isNotEmpty
  }

}