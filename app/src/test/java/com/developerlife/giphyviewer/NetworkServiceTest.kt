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
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

class NetworkServiceTest : AndroidTest() {
  @Test
  fun `can create GiphyClient (using its API key)`() {
    val client = GiphyClient()
    Assert.assertNotNull(client)
  }
}

/**
 * Base class for Robolectric data layer tests. Inherit from this class to
 * create a test.
 * More info:
 * - https://fernandocejas.com/2017/02/03/android-testing-with-kotlin/
 * - http://robolectric.org/androidx_test/
 * - https://stackoverflow.com/a/52923630/2085356
 * - http://robolectric.org/migrating/
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    application = AndroidTest.ApplicationStub::class,
    sdk = intArrayOf(28)
)
abstract class AndroidTest {
  fun context(): Context {
    return ApplicationProvider.getApplicationContext()
  }

  fun cacheDir(): File {
    return context().cacheDir
  }

  internal class ApplicationStub : Application()
}
