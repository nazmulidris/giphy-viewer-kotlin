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

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.giphy.sdk.core.models.Media
import com.paginate.Paginate
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.find

typealias MediaHandlerLambda = (Media) -> Unit

private const val TRIGGER_LOADING_THRESHOLD = 2
private const val GRID_SPAN_COUNT = 2

/**
 * Creates and manages the RecyclerView that is used by the [MainActivity].
 *
 *  1. It is wired via LiveData to the [AppViewModel]. It lets the RecyclerView
 *  Adapter know when the [GiphyClient] has more data.
 *  2. The RecyclerView uses a StaggeredGridLayoutManager.
 *  3. Fresco is used for image loading.
 *
 *  More info: https://stackoverflow.com/a/34624907/2085356
 */
class RecyclerViewManager(private val activity: MainActivity,
                          private val recyclerView: RecyclerView,
                          private val appViewModel: MyViewModel =
                              ViewModelProviders
                                  .of(activity)
                                  .get(MyViewModel::class.java)
) : AnkoLogger {
  // Create RecyclerView data adapter.
  private val dataAdapter = DataAdapter {
    activity.startActivity(FullScreenActivity.getIntent(activity, it))
  }.apply {
    recyclerView.adapter = this
  }

  // Attach live data observer.
  private val liveDataObserver = object : AnkoLogger {
    init {
      debug { "setupLiveDataObserver: " }
      appViewModel
          .dataEventObservable
          .observe(
              activity,
              Observer { dataEvent ->
                when (dataEvent) {
                  is DataEvent.More    -> onGetMore(dataEvent.newSize)
                  is DataEvent.Refresh -> onRefresh()
                  is DataEvent.Error   -> onError()
                }
              }
          )
    }

    fun onGetMore(newDataSize: Int) {
      debug { "onGetMoreEvent: " }
      currentlyLoadingFlag = false
      val underlyingDataSize = appViewModel.data.size
      dataAdapter
          .notifyItemRangeInserted(
              underlyingDataSize - newDataSize, newDataSize)
    }

    fun onRefresh() {
      debug { "onRefreshEvent: " }
      setupInfiniteScrolling()
      dataAdapter.notifyDataSetChanged()
    }

    fun onError() {
      debug { "onErrorEvent: " }
      currentlyLoadingFlag = false
      toast(context = activity) { setText("Network error occurred") }
    }
  }

  // Create RecyclerView layout Manager.
  private val layoutManager: StaggeredGridLayoutManager =
      StaggeredGridLayoutManager(GRID_SPAN_COUNT, VERTICAL)
          .apply {
            gapStrategy = GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            recyclerView.layoutManager = this
          }

  // Handle save/restore RecyclerView position.
  init {
    activity.lifecycle
        .addObserver(
            object : LifecycleObserver {
              @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
              fun saveListPosition() {
                val firstVisibleItemPosition =
                    layoutManager.findFirstVisibleItemPositions(null)[0]
                appViewModel.position = firstVisibleItemPosition
                debug { "saveListPosition: $firstVisibleItemPosition" }
              }

              @OnLifecycleEvent(Lifecycle.Event.ON_START)
              fun restoreListPosition() {
                layoutManager.scrollToPosition(appViewModel.position)
                debug { "restoreListPosition: ${appViewModel.position}" }
              }
            })
  }

  // Handle infinite scrolling.
  private var currentlyLoadingFlag: Boolean = false
  private lateinit var paginate: Paginate

  /**
   * This only needs to be done once for the life of this class. Infinite
   * scrolling only comes into play after the first set of data has been loaded.
   */
  fun setupInfiniteScrolling() {
    if (::paginate.isInitialized) {
      debug { "setupInfiniteScrolling: already set up" }
      return
    }

    debug { "setupInfiniteScrolling: set it up ONCE" }
    val callbacks = object : Paginate.Callbacks {
      override fun onLoadMore() {
        debug { "onLoadMore: " }
        currentlyLoadingFlag = true
        appViewModel.requestMoreData()
      }

      override fun isLoading(): Boolean {
        debug { "isLoading: $currentlyLoadingFlag" }
        return currentlyLoadingFlag
      }

      /** Return false to always allow infinite scrolling  */
      override fun hasLoadedAllItems(): Boolean {
        debug { "hasLoadedAllItems: return false" }
        return false
      }
    }
    paginate = Paginate.with(recyclerView, callbacks)
        .setLoadingTriggerThreshold(TRIGGER_LOADING_THRESHOLD)
        .setLoadingListItemSpanSizeLookup { GRID_SPAN_COUNT }
        .build()
  }

  private inner class DataAdapter(val mediaClickHandler: MediaHandlerLambda) :
    RecyclerView.Adapter<RowViewHolder>() {
    override fun onCreateViewHolder(parentView: ViewGroup,
                                    viewType: Int
    ): RowViewHolder {
      val cellView = LayoutInflater
          .from(parentView.context)
          .inflate(R.layout.grid_cell, parentView, false)
      return RowViewHolder(cellView)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
      holder.bindDataToView(appViewModel.data[position], mediaClickHandler)
    }

    override fun getItemCount(): Int {
      return appViewModel.data.size
    }
  }

  private inner class RowViewHolder(
      cellView: View,
      val imageView: SimpleDraweeView = cellView.find(R.id.image_grid_cell)
  ) : RecyclerView.ViewHolder(cellView) {
    fun bindDataToView(data: Media, block: MediaHandlerLambda) {
      imageView.setOnClickListener { block.invoke(data) }
      with(data.images.fixedWidthDownsampled) {
        imageView.apply {
          aspectRatio = width.toFloat() / height.toFloat()
          controller = Fresco.newDraweeControllerBuilder()
              .setUri(Uri.parse(gifUrl))
              .setAutoPlayAnimations(true)
              .build()
        }
      }
    }
  }
}
