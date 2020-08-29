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

package unitTests

import com.r3bl.giphyviewer.AppMode
import com.r3bl.giphyviewer.NetworkServiceResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppModeTest {
  @Test
  fun `can create Trending mode`() {
    val mode: AppMode =
        AppMode.Trending()
    assertTrue(mode is AppMode.Trending)
  }

  @Test
  fun `can create Search mode`() {
    val mode: AppMode =
        AppMode.Search("query param")
    assertTrue(mode is AppMode.Search)
    assertEquals("query param", (mode as AppMode.Search).query)
  }
}

class DataTest {
  @Test
  fun `can create Refresh event`() {
    val event: NetworkServiceResponse =
        NetworkServiceResponse.Refresh()
    assertTrue(event is NetworkServiceResponse.Refresh)
  }

  @Test
  fun `can create Error event`() {
    val event: NetworkServiceResponse =
        NetworkServiceResponse.Error()
    assertTrue(event is NetworkServiceResponse.Error)
  }

  @Test
  fun `can create GetMore event`() {
    val newSize = 123
    val event: NetworkServiceResponse =
        NetworkServiceResponse.More(newSize)
    assertTrue(event is NetworkServiceResponse.More)
    assertEquals(newSize, (event as NetworkServiceResponse.More).newSize)
  }
}