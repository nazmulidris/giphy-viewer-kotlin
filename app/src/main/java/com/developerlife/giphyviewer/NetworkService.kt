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
import com.giphy.sdk.core.models.enums.MediaType
import com.giphy.sdk.core.models.enums.RatingType
import com.giphy.sdk.core.network.api.CompletionHandler
import com.giphy.sdk.core.network.api.GPHApiClient
import com.giphy.sdk.core.network.response.ListMediaResponse
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

private const val API_KEY = "mnVttajnx9Twmgp3vFbMQa3Gvn9Rv4Hg"
private const val MAX_ITEMS_PER_REQUEST = 25

class GiphyClient : AnkoLogger {
  private val client = GPHApiClient(API_KEY)

  fun makeRequest(appMode: AppMode,
                  responseHandler: GiphyClientResponseHandler,
                  offset: Int? = null
  ) {
    when (appMode) {
      is AppMode.Trending -> {
        info {
          "makeTrendingRequest: offset=$offset, limit: $MAX_ITEMS_PER_REQUEST"
        }
        client.trending(MediaType.gif,
                        MAX_ITEMS_PER_REQUEST,
                        offset,
                        RatingType.g,
                        generateWrapperFor(responseHandler))
      }
      is AppMode.Search   -> {
        info {
          "makeSearchRequest: query: ${appMode.query}, offset=$offset, " +
          "limit:$MAX_ITEMS_PER_REQUEST"
        }
        client.search(appMode.query,
                      MediaType.gif,
                      MAX_ITEMS_PER_REQUEST,
                      offset,
                      RatingType.g,
                      null,
                      generateWrapperFor(responseHandler))
      }
    }
  }

  private fun generateWrapperFor(responseHandler: GiphyClientResponseHandler):
      CompletionHandler<ListMediaResponse> {
    return CompletionHandler { results, _ ->
      // This code runs in the main thread.
      runOnUiThread {
        info { "results: $results" }
        when {
          results == null      -> responseHandler.onErrorResponse()
          results.data != null -> responseHandler.onOkResponse(results.data)
        }
        responseHandler.onComplete()
      }
    }
  }
}

interface GiphyClientResponseHandler {
  fun onOkResponse(mediaList: List<Media>)
  fun onErrorResponse()
  fun onComplete()
}