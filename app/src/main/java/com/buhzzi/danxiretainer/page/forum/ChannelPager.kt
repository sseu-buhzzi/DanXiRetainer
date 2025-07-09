package com.buhzzi.danxiretainer.page.forum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch

private class ChannelPagerViewModel<T>(
	itemsProducer: suspend ProducerScope<T>.() -> Unit,
	private val pageSize: Int,
) : ViewModel() {
	@Deprecated("Only for debugging")
	private var loadCount = 0

	@OptIn(ExperimentalCoroutinesApi::class)
	private val itemsChannel = viewModelScope.produce(capacity = 0) {
		runCatching {
			itemsProducer()
		}.getOrElse { exception ->
			ended = true
			throw exception
		}
	}

	private val cachedPages = mutableStateListOf<List<T>>()
	val pages get() = cachedPages.toList()

	var loading by mutableStateOf(false)
		private set
	var ended by mutableStateOf(false)
		private set

	fun reachedEnd(pageIndex: Int) = ended && pageIndex >= pages.lastIndex

	suspend fun loadPage() {
		val loadIndex = loadCount++

		ended && return

		println("$loadIndex: $loading")
		loading && return
		val exception = runCatching {
			loading = true

			// TODO emulate network delay, delete it
			// delay(1024)
			println("$loadIndex: -> $loading")
			val items = buildList {
				repeat(pageSize) {
					val hole = itemsChannel.receiveCatching().getOrNull() ?: return@buildList
					add(hole)
				}
			}
			println("$loadIndex:, ${items.size}")
			if (items.isNotEmpty()) {
				cachedPages.add(items)
			}
			println("$loadIndex: ${items.size}, ${pages.size}")
		}.exceptionOrNull()
		loading = false
		println("$loadIndex: $loading ->")
		exception?.let { throw it }
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> HorizontalScrollChannelPager(
	itemsProducer: suspend ProducerScope<T>.() -> Unit,
	key: String?,
	pageSize: Int,
	refresh: suspend CoroutineScope.() -> Unit,
	modifier: Modifier = Modifier,
	itemContent: @Composable (T) -> Unit,
) {
	val pagerViewModel = viewModel<ChannelPagerViewModel<T>>(
		key = key,
	) {
		ChannelPagerViewModel(itemsProducer, pageSize)
	}

	var refreshing by remember { mutableStateOf(false) }

	val pagerState = rememberPagerState(1) { pagerViewModel.pages.size + 1 }
	HorizontalPager(
		pagerState,
		modifier = modifier,
	) { pageIndex ->
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
			LaunchedEffect(
				pagerViewModel,
				pagerState,
			) {
				pagerViewModel.loadPage()
			}
			Box(
				modifier = Modifier
					.fillMaxSize(),
				contentAlignment = Alignment.Center,
			) {
				if (pagerViewModel.loading) {
					LinearProgressIndicator()
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> VerticalScrollChannelPager(
	itemsProducer: suspend ProducerScope<T>.() -> Unit,
	key: String?,
	pageSize: Int,
	refresh: suspend CoroutineScope.() -> Unit,
	modifier: Modifier = Modifier,
	itemContent: @Composable (T) -> Unit,
) {
	val pagerViewModel = viewModel<ChannelPagerViewModel<T>>(
		key = key,
	) {
		ChannelPagerViewModel(itemsProducer, pageSize)
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
			val lazyListState = rememberLazyListState()
			LazyColumn(
				modifier = Modifier
					.fillMaxSize(),
				state = lazyListState,
			) {
				items(pagerViewModel.pages.flatten()) { item ->
					itemContent(item)
				}
				item {
					LaunchedEffect(
						pagerViewModel,
						lazyListState,
					) {
						pagerViewModel.loadPage()
					}
					if (pagerViewModel.loading) {
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
