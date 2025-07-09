package com.buhzzi.danxiretainer.page.forum

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.buhzzi.danxiretainer.model.settings.DxrPagerScrollOrientation
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.pagerScrollOrientationFlow
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
	init {
		println("ChannelPagerViewModel($itemsProducer, $pageSize)")
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	private val itemsChannel = viewModelScope.produce(capacity = 0) {
		try {
			itemsProducer()
		} finally {
			ended = true
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
		ended && return

		loading && return
		try {
			loading = true

			val items = buildList {
				repeat(pageSize) {
					println("$it/$pageSize")
					val hole = itemsChannel.receiveCatching().getOrNull() ?: return@buildList
					add(hole)
				}
			}
			if (items.isNotEmpty()) {
				cachedPages.add(items)
			}
		} finally {
			loading = false
		}
	}
}

@Composable
fun <T> ChannelPager(
	itemsProducer: suspend ProducerScope<T>.() -> Unit,
	key: String?,
	pageSize: Int,
	refresh: suspend CoroutineScope.() -> Unit,
	modifier: Modifier = Modifier,
	itemContent: @Composable (T) -> Unit,
) {
	val pagerScrollOrientation by DxrSettings.Models.pagerScrollOrientationFlow.collectAsState(null)
	when (pagerScrollOrientation) {
		DxrPagerScrollOrientation.HORIZONTAL -> HorizontalScrollChannelPager(
			itemsProducer,
			key,
			pageSize,
			refresh,
			modifier,
			itemContent,
		)

		DxrPagerScrollOrientation.VERTICAL -> VerticalScrollChannelPager(
			itemsProducer,
			key,
			pageSize,
			refresh,
			modifier,
			itemContent,
		)

		else -> Unit
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
	PullToRefreshBox(
		refreshing,
		{
			refreshing = true
			pagerViewModel.viewModelScope.launch(Dispatchers.IO) {
				try {
					// TODO sometimes first load are interrupted and unable to restart automatically
					print("refresh begin")
					refresh()
					println(" end")
				} finally {
					refreshing = false
				}
			}
		},
		modifier = modifier,
	) {
		val pagerState = rememberPagerState { pagerViewModel.pages.size + 1 }
		HorizontalPager(
			pagerState,
			modifier = modifier,
		) { pageIndex ->
			pagerViewModel.pages.getOrNull(pageIndex)?.let {
				LazyColumn(
					modifier = Modifier
						.fillMaxSize(),
				) {
					items(pagerViewModel.pages[pageIndex]) { item ->
						itemContent(item)
					}
				}
			} ?: run {
				LaunchedEffect(
					pagerViewModel,
					refreshing,
					pagerState,
				) {
					pagerViewModel.loadPage()
				}
				Box(
					modifier = Modifier
						.fillMaxSize()
						.verticalScroll(rememberScrollState()),
					contentAlignment = Alignment.Center,
				) {
					if (pagerViewModel.loading) {
						LinearProgressIndicator()
					}
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
	PullToRefreshBox(
		refreshing,
		{
			refreshing = true
			pagerViewModel.viewModelScope.launch(Dispatchers.IO) {
				try {
					refresh()
				} finally {
					refreshing = false
				}
			}
		},
		modifier = modifier,
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
					refreshing,
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
