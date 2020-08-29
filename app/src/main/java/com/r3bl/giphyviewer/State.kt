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

package com.r3bl.giphyviewer

import java.util.*

/** User interaction causes a mode to be created and set on the UI. */
sealed class AppMode {
  data class Trending(val timestamp: Date = Date()) : AppMode()
  data class Search(val query: String, val timestamp: Date = Date()) : AppMode()
}

/**
 * GiphyClient responses result in this event being broadcast to various
 * parts of the UI that need to respond to these underlying data model changes.
 */
sealed class NetworkServiceResponse {
  data class Refresh(val timestamp: Date = Date()) : NetworkServiceResponse()
  data class Error(val timestamp: Date = Date()) : NetworkServiceResponse()
  data class More(val newSize: Int, val timestamp: Date = Date()) :
    NetworkServiceResponse()
}