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
import org.jetbrains.anko.find
import org.jetbrains.anko.info

private const val TRIGGER_LOADING_THRESHOLD = 2
private const val GRID_SPAN_COUNT = 2

/**
 * Creates and manages the RecyclerView that is used by the [MainActivity].
 *
 *  1. It is wired via LiveData to the [MyViewModel]. It lets the RecyclerView
 *  Adapter know when the [GiphyClient] has more data.
 *  2. The RecyclerView uses a StaggeredGridLayoutManager.
 *  3. Fresco is used for image loading.
 *
 *  More info: [https://stackoverflow.com/a/34624907/2085356].
 */
class RecyclerViewManager(private val activity: MainActivity,
                          private val recyclerView: RecyclerView,
                          private val appViewModel: MyViewModel =
                              ViewModelProviders
                                  .of(activity)
                                  .get(MyViewModel::class.java)
) : AnkoLogger {
  // Attach live data observer.
  private val liveDataObserver = object : AnkoLogger {
    init {
      info { "setupLiveDataObserver: " }
      appViewModel
          .responseObservable
          .observe(
              activity,
              Observer { response ->
                when (response) {
                  is NetworkServiceResponse.More    -> moreDataResponse(
                      response.newSize)
                  is NetworkServiceResponse.Refresh -> refreshDataResponse()
                  is NetworkServiceResponse.Error   -> errorResponse()
                }
              }
          )
    }

    /** Handle response for [MyViewModel.requestMoreData] */
    fun moreDataResponse(newDataSize: Int) {
      info { "onGetMoreEvent: " }
      paginateIsLoading = false
      val underlyingDataSize = appViewModel.data.size
      val positionStart = underlyingDataSize - newDataSize
      info {
        "notifyItemRangeInserted: start: $positionStart, count: $newDataSize"
      }
      dataAdapter.notifyItemRangeInserted(positionStart, newDataSize)
    }

    /** Handle response for [MyViewModel.requestRefreshData] */
    fun refreshDataResponse() {
      info { "onRefreshEvent: " }
      setupInfiniteScrolling()
      dataAdapter.notifyDataSetChanged()
    }

    /** Handle response for [MyViewModel.requestRefreshData] or
     * [MyViewModel.requestMoreData] */
    fun errorResponse() {
      info { "onErrorEvent: " }
      paginateIsLoading = false
      toast(context = activity) { setText("Network error occurred") }
    }
  }

  // Create RecyclerView layout Manager.
  private val layoutManager: StaggeredGridLayoutManager =
      StaggeredGridLayoutManager(GRID_SPAN_COUNT, VERTICAL)
          .apply {
            gapStrategy = GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            recyclerView.layoutManager = this
            info { "RecyclerView layout set to StaggeredGridLayoutManager" }
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
                info { "saveListPosition: $firstVisibleItemPosition" }
              }

              @OnLifecycleEvent(Lifecycle.Event.ON_START)
              fun restoreListPosition() {
                layoutManager.scrollToPosition(appViewModel.position)
                info { "restoreListPosition: ${appViewModel.position}" }
              }
            })
  }

  // Create and attach RecyclerView data adapter.
  private var dataAdapter: DataAdapter

  init {
    dataAdapter = DataAdapter {
      activity.startActivity(FullScreenActivity.getIntent(activity, it))
    }
    recyclerView.adapter = dataAdapter
    info { "RecyclerView adapter set to DataAdapter" }
  }

  // Handle infinite scrolling.
  private var paginateIsLoading: Boolean = false
  private lateinit var paginate: Paginate

  /**
   * This only needs to be done once for the life of this class. Infinite
   * scrolling only comes into play after the first set of data has been loaded.
   */
  fun setupInfiniteScrolling() {
    if (::paginate.isInitialized) {
      info { "setupInfiniteScrolling: already set up" }
      return
    }

    info { "setupInfiniteScrolling: set it up ONCE" }
    val callbacks = object : Paginate.Callbacks {
      override fun onLoadMore() {
        info { "onLoadMore: " }
        paginateIsLoading = true
        appViewModel.requestMoreData()
      }

      override fun isLoading(): Boolean {
        info { "isLoading: $paginateIsLoading" }
        return paginateIsLoading
      }

      /** Return false to always allow infinite scrolling  */
      override fun hasLoadedAllItems(): Boolean {
        info { "hasLoadedAllItems: return false" }
        return false
      }
    }
    paginate = Paginate.with(recyclerView, callbacks)
        .setLoadingTriggerThreshold(TRIGGER_LOADING_THRESHOLD)
        .setLoadingListItemSpanSizeLookup { GRID_SPAN_COUNT }
        .build()
  }

  // RecyclerView data adapter.
  private inner class DataAdapter(val mediaClickHandler:
                                  BlockWithSingleArgLambda<Media>
  ) :
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

  // ViewHolder implementation.
  private inner class RowViewHolder(
      cellView: View,
      val imageView: SimpleDraweeView = cellView.find(R.id.image_grid_cell)
  ) : RecyclerView.ViewHolder(cellView) {
    fun bindDataToView(data: Media, block: BlockWithSingleArgLambda<Media>) {
      imageView.setOnClickListener { block.invoke(data) }
      val image = data.images.fixedWidthDownsampled
      val imageUri = Uri.parse(image.gifUrl)
      imageView.aspectRatio = (image.width / image.height).toFloat()
      imageView.controller = Fresco
          .newDraweeControllerBuilder()
          .setUri(imageUri)
          .setAutoPlayAnimations(true)
          .build()
    }
  }
}
