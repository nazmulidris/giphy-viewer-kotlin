package com.developerlife.giphyviewer

sealed class AppMode {
  class Trending : AppMode()
  data class Search(val query: String) : AppMode()
}