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

import com.giphy.sdk.core.models.Media
import com.giphy.sdk.core.network.api.GPHApiClient
import org.jetbrains.anko.AnkoLogger

class GiphyClient : AnkoLogger {
  val API_KEY = "mnVttajnx9Twmgp3vFbMQa3Gvn9Rv4Hg"
  val MAX_ITEMS_PER_REQUEST = 25
  val client = GPHApiClient(API_KEY)

}

interface GiphyClientResponseHandler {
  fun onResponse(mediaList: List<Media>)
  fun onError()
}