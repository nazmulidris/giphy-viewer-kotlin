/*
 * Copyright 2019 R3BL LLC. All rights reserved.
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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info

/**
 * The main UI of the application that contains the Toolbar, SearchView, and
 * RecyclerView.
 *
 * It creates a [MyViewModel] that is stable across orientation changes, and is
 * only created or destroyed when:
 * 1. the user starts the app (launching it from the launcher, not just
 * switching to an already running instance),
 * 2. or exits it (by pressing back and not not just pressing home or switching
 * between apps).
 */
class MainActivity : AppCompatActivity() {
  private lateinit var viewHolder: ViewHolder
  private lateinit var appViewModel: MyViewModel

  // Cold start the Activity.
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    appViewModel = ViewModelProviders
        .of(this)
        .get(MyViewModel::class.java)
    viewHolder = ViewHolder(this)
    loadData()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    viewHolder.setupSearchView(menu, appViewModel)
    return super.onCreateOptionsMenu(menu)
  }

  // Load fresh data into the activity.
  private fun loadData() {
    if (appViewModel.data.isEmpty()) {
      // Activity has no data, so perform a refresh now.
      viewHolder.swipeRefreshLayout.isRefreshing = true
      viewHolder.onRefreshGestureHandler.onRefresh()
    }
    else {
      /*
       * Activity underwent an orientation change. The MyViewModel has data, but
       * pagination isn't attached to the RecyclerView, since this only happens
       * after the first refresh operation occurs (in the block above) when the
       * Activity is created for the very very first time (not an orientation
       * change driven destroy -> instantiate).
       */
      viewHolder.recyclerViewManager.setupInfiniteScrolling()
    }
  }

  /**
   * Convenience class that holds all the views of this activity in one place.
   */
  private inner class ViewHolder(var activity: MainActivity) {
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var onRefreshGestureHandler: SwipeRefreshLayout.OnRefreshListener
    lateinit var toolbar: Toolbar
    lateinit var recyclerView: RecyclerView
    lateinit var recyclerViewManager: RecyclerViewManager

    init {
      setupSwipeRefreshLayout()
      setupToolbar()
      setupRecyclerView()
    }

    fun setupSwipeRefreshLayout() {
      swipeRefreshLayout = find(R.id.swipe_refresh_container)
      onRefreshGestureHandler = SwipeRefreshLayout.OnRefreshListener {
        appViewModel
            .requestRefreshData { swipeRefreshLayout.isRefreshing = false }
      }
      swipeRefreshLayout.setOnRefreshListener(onRefreshGestureHandler)
    }

    fun setupToolbar() {
      toolbar = find(R.id.app_toolbar)
      setSupportActionBar(toolbar)
    }

    fun setupRecyclerView() {
      recyclerView = find(R.id.recycler_view)
      recyclerViewManager = RecyclerViewManager(activity, recyclerView)
    }

    fun setupSearchView(menu: Menu,
                        appViewModel: MyViewModel
    ) {
      menuInflater.inflate(R.menu.main_activity_actions, menu)
      val searchMenuItem = menu.findItem(R.id.action_search)
      val searchView = searchMenuItem.actionView as SearchView
      SearchViewManager(searchView, searchMenuItem, appViewModel)
    }
  }

  /**
   * Creates and manages the SearchView (which is in the Toolbar) that is used
   * by the [MainActivity].
   */
  private inner class SearchViewManager(searchView: SearchView,
                                        searchMenuItem: MenuItem,
                                        appViewModel: MyViewModel
  ) : AnkoLogger {
    init {
      searchView.setOnCloseListener {
        info { "onClose: clear search mode, and request refresh" }
        appViewModel.appMode = AppMode.Trending()
        appViewModel.requestRefreshData()
        false
      }

      searchView.setOnQueryTextListener(
          object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
              info { "onQueryTextSubmit: $query" }
              if (!query.isEmpty()) {
                searchMenuItem.collapseActionView()
                appViewModel.appMode = AppMode.Search(query)
                appViewModel.requestRefreshData()
              }
              return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
              return true
            }
          })

      /*
       * When activity has been thru an orientation change, make sure to restore
       * the SearchView state (if it was in search mode before the orientation
       * change).
       */
      val appMode = appViewModel.appMode
      when (appMode) {
        is AppMode.Search -> {
          searchMenuItem.expandActionView()
          searchView.isIconified = false
          searchView.setQuery(appMode.query, false)
        }
      }
    }
  }
}