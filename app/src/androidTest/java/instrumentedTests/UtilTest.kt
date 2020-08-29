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
import com.r3bl.giphyviewer.shorten
import com.r3bl.giphyviewer.shortenUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.anko.doAsync
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class UtilTest {

  val longUrl =
      "https://en.wikipedia.org/wiki/Cache_replacement_policies#Last_in_first_out_(LIFO)"
  val shortUrl = "http://tinyurl.com/yakt9emb"

  @Test
  fun canCallShortenUrlInAsyncTask() {
    val latch = CountDownLatch(1)
    lateinit var processedUrl: String
    doAsync {
      processedUrl = shortenUrl(longUrl)
      latch.countDown()
    }
    latch.await()
    assertThat(processedUrl).isNotEqualTo(longUrl)
    assertThat(processedUrl).isEqualTo(shortUrl)
  }

  @Test
  fun canCallShortenUrlCoroutines() {
    val latch = CountDownLatch(1)
    lateinit var processedUrl: String
    GlobalScope.launch(Dispatchers.IO) {
      processedUrl = shorten(longUrl)
      latch.countDown()
    }
    latch.await()
    assertThat(processedUrl).isNotEqualTo(longUrl)
    assertThat(processedUrl).isEqualTo(shortUrl)
  }

}
