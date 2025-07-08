package com.buhzzi.danxiretainer.page.forum

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buhzzi.danxiretainer.util.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private class BidirectionalChannelPagerViewModel<T>(
	backwardProducer: suspend ProducerScope<T>.() -> Unit,
	forwardProducer: suspend ProducerScope<T>.() -> Unit,
	private val pageSize: Int,
) : ViewModel() {
	@Deprecated("Only for debugging")
	private var loadCount = 0

	@OptIn(ExperimentalCoroutinesApi::class)
	private val itemChannels = listOf(
		viewModelScope.produce(capacity = 0, block = backwardProducer),
		viewModelScope.produce(capacity = 0, block = forwardProducer),
	)

	@SuppressLint("MutableCollectionMutableState")
	private val cachedPagesRefState = mutableStateOf(Singleton(ArrayDeque<List<T>>()))
	val pages: ArrayDeque<List<T>>
		get() = cachedPagesRefState.value.`object`

	private val loadingList = mutableStateListOf(false, false)
	val loadingBackward get() = loadingList[0]
	val loadingForward get() = loadingList[1]

	private val endedList = mutableStateListOf(false, false)
	val endedBackward get() = endedList[0]
	val endedForward get() = endedList[1]

	fun reachedBackwardEnd(pageIndex: Int) = endedList[0] && pageIndex <= 0
	fun reachedForwardEnd(pageIndex: Int) = endedList[1] && pageIndex >= pages.lastIndex

	suspend fun loadPage(backward: Boolean) {
		val loadIndex = loadCount++

		val directionInt = if (backward) 0 else 1
		endedList[directionInt] && return

		println("$loadIndex: loadPage($backward) $loadingBackward $loadingForward")
		loadingList[directionInt] && return
		runCatching {
			loadingList[directionInt] = true

			// TODO emulate network delay, delete it
			when (directionInt) {
				0 -> delay(0)
				1 -> delay(1024)
			}
			println("$loadIndex: -> $loadingBackward $loadingForward")
			val items = buildList {
				repeat(pageSize) {
					val hole = itemChannels[directionInt].receiveCatching().getOrNull() ?: run {
						endedList[directionInt] = true
						return@buildList
					}
					add(hole)
				}
			}
			println("$loadIndex:, ${items.size}")
			if (items.isNotEmpty()) {
				if (backward) {
					pages.addFirst(items.asReversed())
				} else {
					pages.addLast(items)
				}
				cachedPagesRefState.value = Singleton(pages)
			}
			println("$loadIndex: ${items.size}, ${pages.size}")
		}.getOrElse { cause ->
			println("$loadIndex: ${cause.message}")
		}
		loadingList[directionInt] = false
		println("$loadIndex: $loadingBackward $loadingForward ->")
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> HorizontalBidirectionalChannelPager(
	backwardProducer: suspend ProducerScope<T>.() -> Unit,
	forwardProducer: suspend ProducerScope<T>.() -> Unit,
	key: String?,
	pageSize: Int,
	refresh: suspend CoroutineScope.() -> Unit,
	modifier: Modifier = Modifier,
	itemContent: @Composable (T) -> Unit,
) {
	val pagerViewModel = viewModel<BidirectionalChannelPagerViewModel<T>>(
		key = key,
	) {
		BidirectionalChannelPagerViewModel(backwardProducer, forwardProducer, pageSize)
	}

	var refreshing by remember { mutableStateOf(false) }

	val pagerState = rememberPagerState(1) { pagerViewModel.pages.size + 2 }
	HorizontalPager(
		pagerState,
		modifier = modifier,
	) { index ->
		val pageIndex = index - 1
		pagerViewModel.pages.getOrNull(pageIndex)?.let {
			Column(
				verticalArrangement = Arrangement.SpaceBetween,
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				PullToRefreshBox(
					refreshing,
					{
						refreshing = true
						pagerViewModel.viewModelScope.launch(Dispatchers.IO) {
							refresh()
							refreshing = false
						}
					},
					modifier = Modifier
						.weight(1F),
				) {
					LazyColumn(
						modifier = Modifier
							.fillMaxSize(),
					) {
						items(pagerViewModel.pages[pageIndex]) { item ->
							itemContent(item)
						}
					}
				}
			}
		} ?: run {
			val backward = pageIndex < 0
			LaunchedEffect(pagerViewModel, pagerState) {
				pagerViewModel.loadPage(backward)
			}
			Box(
				modifier = Modifier
					.fillMaxSize(),
				contentAlignment = Alignment.Center,
			) {
				LinearProgressIndicator(
					modifier = Modifier
						.run {
							if (backward) {
								scale(-1F, 1F)
							} else {
								this
							}
						},
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> VerticalBidirectionalChannelPager(
	backwardProducer: suspend ProducerScope<T>.() -> Unit,
	forwardProducer: suspend ProducerScope<T>.() -> Unit,
	key: String?,
	pageSize: Int,
	refresh: suspend CoroutineScope.() -> Unit,
	modifier: Modifier = Modifier,
	itemContent: @Composable (T) -> Unit,
) {
	val scope = rememberCoroutineScope()

	val pagerViewModel = viewModel<BidirectionalChannelPagerViewModel<T>>(
		key = key,
	) {
		BidirectionalChannelPagerViewModel(backwardProducer, forwardProducer, pageSize)
	}

	var refreshing by remember { mutableStateOf(false) }

	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.SpaceBetween,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		PullToRefreshBox(
			refreshing,
			{
				refreshing = true
				pagerViewModel.viewModelScope.launch(Dispatchers.IO) {
					refresh()
					refreshing = false
				}
			},
			modifier = Modifier
				.weight(1F),
		) {
			// It is here to get LazyColumn's height
			BoxWithConstraints(
				modifier = Modifier
					.fillMaxSize(),
			) {
				val lazyColumnHeight = this.maxHeight

				val lazyListState = rememberLazyListState(1)
				LazyColumn(
					modifier = Modifier
						.fillMaxSize(),
					state = lazyListState,
				) {
					item {
						LaunchedEffect(
							pagerViewModel,
							lazyListState,
							pagerViewModel.loadingForward,
						) {
							pagerViewModel.loadingForward && return@LaunchedEffect
							val currentIndex = lazyListState.firstVisibleItemIndex
							val currentScrollOffset = lazyListState.firstVisibleItemScrollOffset
							println("lazyListState before ${lazyListState.firstVisibleItemIndex} ${lazyListState.firstVisibleItemScrollOffset}")
							pagerViewModel.loadPage(true)
							println("lazyListState after ${lazyListState.firstVisibleItemIndex} ${lazyListState.firstVisibleItemScrollOffset}")
							// lazyListState.scrollToItem(
							// 	3,
							// 	lazyListState.firstVisibleItemScrollOffset,
							// )
							lazyListState.scrollToItem(
								currentIndex + 1,
								currentScrollOffset,
							)
						}
						if (pagerViewModel.loadingBackward) {
							LinearProgressIndicator(
								modifier = Modifier
									.fillMaxWidth()
									.scale(-1F, 1F),
							)
						} else {
							Box(
								modifier = Modifier
									.height(64.dp),
								contentAlignment = Alignment.BottomCenter,
							) {
								LinearProgressIndicator(
									{
										1 - lazyListState.firstVisibleItemScrollOffset / 64F
									},
									modifier = Modifier
										.fillMaxWidth(),
								)
							}
						}
					}
					items(pagerViewModel.pages) { page ->
						Column(
							modifier = Modifier
								.border(1.dp, Color.Red),
						) {
							page.forEach { item ->
								itemContent(item)
							}
						}
					}
					item {
						LaunchedEffect(
							pagerViewModel,
							lazyListState,
							pagerViewModel.loadingBackward,
						) {
							pagerViewModel.loadingBackward && return@LaunchedEffect
							pagerViewModel.loadPage(false)
							lazyListState.scrollToItem(
								pagerViewModel.pages.size,
								lazyListState.firstVisibleItemScrollOffset,
							)
						}
						if (pagerViewModel.loadingForward) {
							LinearProgressIndicator(
								modifier = Modifier
									.fillMaxWidth(),
							)
						}
					}
				}
			}
		}
	}
}
