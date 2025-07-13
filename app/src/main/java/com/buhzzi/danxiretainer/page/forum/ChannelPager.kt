package com.buhzzi.danxiretainer.page.forum

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buhzzi.danxiretainer.model.settings.DxrPagerScrollOrientation
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.pagerScrollOrientationOrDefault
import com.buhzzi.danxiretainer.repository.settings.pagerScrollOrientationOrDefaultFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.launch
import java.util.Collections

private class ChannelPagerViewModel<T>(
	itemsFlow: Flow<T>,
	private val pageSize: Int,
) : ViewModel() {
	private val cachedPages = mutableStateListOf<List<T>>()

	var loading by mutableStateOf(false)
		private set
	var ended by mutableStateOf(false)
		private set

	// `produce` has default capacity of `Channel.RENDEZVOUS`, which is `0`
	// `produceIn` will not always act like that, it may preserve a buffer
	private val itemsChannel = itemsFlow.produceIn(viewModelScope)

	val readonlyPages: List<List<T>> = Collections.unmodifiableList(cachedPages)
	val readonlyItems = object : AbstractList<T>() {
		override val size
			// Equivalent to `pages.sumOf { it.size }`
			get() = (cachedPages.size - 1) * pageSize + (cachedPages.lastOrNull()?.size ?: pageSize)

		override fun get(index: Int) =
			cachedPages[index / pageSize][index % pageSize]
	}

	suspend fun loadPage() {
		Log.d("ChannelPagerViewModel", "loadPage: ${cachedPages.size} pages, ${readonlyItems.size} items")
		// ended && return

		loading && return
		try {
			loading = true

			var buildingException: Throwable? = null
			val items = buildList {
				repeat(pageSize) {
					val hole = itemsChannel.receiveCatching().getOrElse { exception ->
						ended = true
						buildingException = exception
						return@buildList
					}
					add(hole)
				}
			}
			Log.d("ChannelPagerViewModel", "received ${items.size}: ${cachedPages.size} pages, ${readonlyItems.size} items")
			if (items.isNotEmpty()) {
				cachedPages.add(items)
			}
			buildingException?.let { throw it }
		} finally {
			loading = false
		}
	}
}

@Composable
fun <T> ChannelPager(
	itemsFlow: Flow<T>,
	key: String?,
	pageSize: Int,
	initialItemIndex: Int,
	initialItemScrollOffset: Int,
	saveItemPosition: (Int, Int) -> Unit,
	refresh: suspend CoroutineScope.() -> Unit,
	modifier: Modifier = Modifier,
	itemContent: @Composable (T) -> Unit,
) {
	val pagerScrollOrientation by DxrSettings.Models.pagerScrollOrientationOrDefaultFlow.collectAsState(
		DxrSettings.Models.pagerScrollOrientationOrDefault,
	)
	when (pagerScrollOrientation) {
		DxrPagerScrollOrientation.HORIZONTAL -> HorizontalScrollChannelPager(
			itemsFlow = itemsFlow,
			key = key,
			pageSize = pageSize,
			initialItemIndex = initialItemIndex,
			initialItemScrollOffset = initialItemScrollOffset,
			saveItemPosition = saveItemPosition,
			refresh = refresh,
			modifier = modifier,
			itemContent = itemContent,
		)

		DxrPagerScrollOrientation.VERTICAL -> VerticalScrollChannelPager(
			itemsFlow = itemsFlow,
			key = key,
			pageSize = pageSize,
			initialItemIndex = initialItemIndex,
			initialItemScrollOffset = initialItemScrollOffset,
			saveItemPosition = saveItemPosition,
			refresh = refresh,
			modifier = modifier,
			itemContent = itemContent,
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> HorizontalScrollChannelPager(
	itemsFlow: Flow<T>,
	key: String?,
	pageSize: Int,
	initialItemIndex: Int,
	initialItemScrollOffset: Int,
	saveItemPosition: (Int, Int) -> Unit,
	refresh: suspend CoroutineScope.() -> Unit,
	modifier: Modifier = Modifier,
	itemContent: @Composable (T) -> Unit,
) {
	val pagerViewModel = viewModel<ChannelPagerViewModel<T>>(
		key = key,
	) {
		ChannelPagerViewModel(itemsFlow, pageSize)
	}
	val initialPageIndex = initialItemIndex / pageSize
	val initialItemIndexInPage = initialItemIndex % pageSize
	val pagerState = rememberPagerState(initialPageIndex) {
		pagerViewModel.readonlyPages.size + if (pagerViewModel.ended) 0 else 1
	}
	val lazyListStates = remember(pagerViewModel, pagerState.pageCount) {
		List(pagerState.pageCount) { pageIndex ->
			if (pageIndex == initialItemIndex) LazyListState(
				initialItemIndexInPage,
				initialItemScrollOffset,
			) else LazyListState()
		}
	}

	var targetPageIndex by rememberSaveable { mutableIntStateOf(0) }
	LaunchedEffect(pagerViewModel) {
		pagerViewModel.viewModelScope.launch {
			while (!pagerViewModel.ended && initialItemIndex >= pagerViewModel.readonlyItems.size) {
				pagerViewModel.loadPage()
			}
			targetPageIndex = initialPageIndex
			lazyListStates[initialPageIndex].scrollToItem(initialItemIndexInPage, initialItemScrollOffset)
		}
	}
	LaunchedEffect(targetPageIndex) {
		pagerState.animateScrollToPage(targetPageIndex)
	}
	var refreshing by remember { mutableStateOf(false) }
	DisposableEffect(refreshing) {
		onDispose {
			val pageIndex = pagerState.currentPage
			val lazyListState = lazyListStates[pageIndex]
			val itemIndex = pageIndex * pageSize + lazyListState.firstVisibleItemIndex
			val itemScrollOffset = lazyListState.firstVisibleItemScrollOffset
			saveItemPosition(itemIndex, itemScrollOffset)
		}
	}

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
		HorizontalPager(
			pagerState,
			modifier = modifier,
		) { pageIndex ->
			pagerViewModel.readonlyPages.getOrNull(pageIndex)?.let {
				LazyColumn(
					modifier = Modifier
						.fillMaxSize(),
					state = lazyListStates[pageIndex],
				) {
					items(pagerViewModel.readonlyPages[pageIndex]) { item ->
						itemContent(item)
					}
				}
			} ?: run {
				LaunchedEffect(
					pagerViewModel,
					refreshing,
				) {
					refreshing && return@LaunchedEffect
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
	itemsFlow: Flow<T>,
	key: String?,
	pageSize: Int,
	initialItemIndex: Int,
	initialItemScrollOffset: Int,
	saveItemPosition: (Int, Int) -> Unit,
	refresh: suspend CoroutineScope.() -> Unit,
	modifier: Modifier = Modifier,
	itemContent: @Composable (T) -> Unit,
) {
	val pagerViewModel = viewModel<ChannelPagerViewModel<T>>(
		key = key,
	) {
		ChannelPagerViewModel(itemsFlow, pageSize)
	}
	val lazyListState = rememberLazyListState(
		initialItemIndex,
		initialItemScrollOffset,
	)
	LaunchedEffect(pagerViewModel) {
		pagerViewModel.viewModelScope.launch {
			var lastPageFirstItemIndex = 0
			while (!pagerViewModel.ended && initialItemIndex >= pagerViewModel.readonlyItems.size.also { lastPageFirstItemIndex = it }) {
				pagerViewModel.loadPage()
				// use part jump to simulate animate scroll, or say to make it look like on the way jumping to the initial index
				lazyListState.scrollToItem(lastPageFirstItemIndex)
			}
			lazyListState.scrollToItem(initialItemIndex, initialItemScrollOffset)
		}
	}
	// optional TODO `animateScrollToItem` or speed costumed `scrollBy`. They are both laggy
	/*
	LaunchedEffect(targetItemIndex) {
		delay(2048)
		val costumedSpeed = false
		if (costumedSpeed) {
			var indexDiff: Int
			while ((targetItemIndex - lazyListState.firstVisibleItemIndex).also { indexDiff = it } > 0) {
				lazyListState.animateScrollBy(indexDiff * 64F)
			}
		} else {
			lazyListState.animateScrollToItem(targetItemIndex)
		}
	}
	 */
	var refreshing by remember { mutableStateOf(false) }
	DisposableEffect(refreshing) {
		onDispose {
			val itemIndex = lazyListState.firstVisibleItemIndex
			val itemScrollOffset = lazyListState.firstVisibleItemScrollOffset
			saveItemPosition(itemIndex, itemScrollOffset)
		}
	}

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
		LazyColumn(
			modifier = Modifier
				.fillMaxSize(),
			state = lazyListState,
		) {
			items(pagerViewModel.readonlyItems) { item ->
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
