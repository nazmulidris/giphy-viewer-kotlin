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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.giphy.sdk.core.models.Media
import com.paginate.Paginate
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

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
                          private val recyclerView: RecyclerView
) : AnkoLogger {
  private val appViewModel: MyAndroidViewModel
  private var isLoading: Boolean = false
  private var paginate: Paginate? = null
  private var layoutManager: StaggeredGridLayoutManager? = null

  private val dataAdapter: DataAdapter by lazy {
    val returnValue = DataAdapter(object : ItemClickListener<Media> {
      override fun onClick(item: Media) {
        activity.startActivity(FullScreenActivity.getIntent(activity, item))
      }
    })
    recyclerView.adapter = dataAdapter
    returnValue
  }

  init {
    this.appViewModel =
        ViewModelProviders.of(activity).get(MyAndroidViewModel::class.java)
    setupLiveDataObserver()
    setupLifecycleObservers()
    setupLayoutManager()
  }

  private fun setupLiveDataObserver() {
    debug { "setupLiveDataObserver: " }
    appViewModel
        .dataEventObservable
        .observe(
            activity,
            Observer { dataEvent ->
              when (dataEvent) {
                is DataEvent.Error   -> onErrorEvent()
                is DataEvent.More    -> onGetMoreEvent(dataEvent.newSize)
                is DataEvent.Refresh -> onRefreshEvent()
              }
            }
        )
  }

  //
  //
  // TODO clean up code below
  //
  //

  fun onGetMoreEvent(newDataSize: Int) {
    debug { "onGetMoreEvent: " }
    isLoading = false
    val underlyingDataSize = appViewModel.data.size
    dataAdapter
        .notifyItemRangeInserted(
            underlyingDataSize - newDataSize, newDataSize)
  }

  fun onRefreshEvent() {
    Log.d(TAG, "onRefreshEvent: ")
    setupInfiniteScrolling()
    dataAdapter.notifyDataSetChanged()
  }

  fun onErrorEvent() {
    Log.d(TAG, "onErrorEvent: ")
    isLoading = false
    Toast
        .makeText(activity, "Network error occurred", Toast.LENGTH_LONG)
        .show()
  }

  /**
   * This only needs to be done once for the life of this class. Infinite
   * scrolling only comes into play after the first set of data has been loaded.
   */
  fun setupInfiniteScrolling() {
    if (paginate == null) {
      Log.d(TAG, "setupInfiniteScrolling: setting it up ONCE")
      val callbacks = object : Paginate.Callbacks {
        override fun onLoadMore() {
          Log.d(TAG, "onLoadMore: ")
          isLoading = true
          appViewModel.requestMoreData()
        }

        override fun isLoading(): Boolean {
          Log.d(TAG, "isLoading: $isLoading")
          return isLoading
        }

        /** Return false to always allow infinite scrolling  */
        override fun hasLoadedAllItems(): Boolean {
          return false
        }
      }
      paginate = Paginate.with(recyclerView, callbacks)
          .setLoadingTriggerThreshold(TRIGGER_LOADING_THRESHOLD)
          .setLoadingListItemSpanSizeLookup { GRID_SPAN_COUNT }
          .build()
    }
    else {
      Log.d(TAG, "setupInfiniteScrolling: already setup up")
    }
  }

  private fun setupLayoutManager() {
    layoutManager = StaggeredGridLayoutManager(
        GRID_SPAN_COUNT, StaggeredGridLayoutManager.VERTICAL)
    layoutManager!!.gapStrategy =
        StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
    recyclerView.layoutManager = layoutManager
  }

  // Saving/restoring list position.

  private fun setupLifecycleObservers() {
    activity.lifecycle
        .addObserver(
            object : LifecycleObserver {
              @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
              fun saveListPosition() {
                val firstVisibleItemPosition =
                    layoutManager!!.findFirstVisibleItemPositions(null)[0]
                appViewModel.position = firstVisibleItemPosition
                Log.d(TAG, "saveListPosition: $firstVisibleItemPosition")
              }

              @OnLifecycleEvent(Lifecycle.Event.ON_START)
              fun restoreListPosition() {
                layoutManager!!.scrollToPosition(appViewModel.position)
                Log.d(TAG, "restoreListPosition: " + appViewModel.position)
              }
            })
  }

  private inner class DataAdapter internal constructor(
      private val onItemClickHandler: ItemClickListener<Media>
  ) :
    RecyclerView.Adapter<RowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int
    ): RowViewHolder {
      val cellView = LayoutInflater.from(parent.context)
          .inflate(R.layout.grid_cell, parent, false)
      return RowViewHolder(cellView)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
      holder.bindDataToView(
          appViewModel.underlyingData[position], onItemClickHandler)
    }

    override fun getItemCount(): Int {
      return appViewModel.underlyingData.size
    }
  }

  private inner class RowViewHolder(imageView: View) :
    RecyclerView.ViewHolder(imageView) {

    private val imageView: SimpleDraweeView

    init {
      this.imageView = imageView.findViewById(R.id.image_grid_cell)
    }

    fun bindDataToView(data: Media, onItemClick: ItemClickListener<Media>) {
      imageView.setOnClickListener { v -> onItemClick.onClick(data) }
      val imageUri = Uri.parse(data.images.fixedWidthDownsampled.gifUrl)
      imageView.aspectRatio =
          data.images.fixedWidthDownsampled.width.toFloat() /
          data.images.fixedWidthDownsampled.height.toFloat()
      imageView.controller = Fresco.newDraweeControllerBuilder()
          .setUri(imageUri)
          .setAutoPlayAnimations(true)
          .build()
    }
  }

  internal interface ItemClickListener<T> {
    fun onClick(item: T)
  }

  companion object {

    // Infinite scrolling support.

    val TRIGGER_LOADING_THRESHOLD = 2

    // Layout Manager.

    private val GRID_SPAN_COUNT = 2
  }
}
