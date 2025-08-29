package com.buhzzi.danxiretainer.page.retension

import android.os.Build
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.userProfile
import com.buhzzi.danxiretainer.repository.settings.userProfileNotNull
import com.buhzzi.danxiretainer.util.userDirPathOf
import com.buhzzi.danxiretainer.util.usersDirPath
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.getOwner
import kotlin.io.path.getPosixFilePermissions
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.io.path.readAttributes
import kotlin.io.path.readSymbolicLink
import kotlin.io.path.relativeTo
import kotlin.io.path.relativeToOrNull
import kotlin.io.path.relativeToOrSelf

@Composable
fun RetentionPageContent(modifier: Modifier = Modifier) {
	val context = LocalContext.current
	val userProfile = DxrSettings.Models.userProfile ?: run {
		Box(
			modifier = modifier,
			contentAlignment = Alignment.Center,
		) {
			Text(stringResource(R.string.user_not_logged_in))
		}
		return
	}
	val userId = userProfile.userId ?: run {
		Box(
			modifier = modifier,
			contentAlignment = Alignment.Center,
		) {
			Text(stringResource(R.string.user_profile_invalid))
		}
		return
	}
	val userDirPath = remember(userId) { context.userDirPathOf(userId) }
	val viewModel = viewModel<RetentionViewModel>()
	val path = viewModel.path

	BackHandler(path.pathString.isNotEmpty()) {
		viewModel.path = path.parent ?: Path("")
	}

	Column(
		modifier = modifier,
	) {
		val lazyListState = rememberLazyListState()
		LaunchedEffect(path) {
			lazyListState.scrollToItem(path.nameCount)
		}
		LazyRow(
			modifier = Modifier
				.padding(8.dp),
			state = lazyListState,
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			itemsIndexed((userDirPath.fileName / path).toList()) { index, dir ->
				AssistChip(
					{ viewModel.path = index.takeIf { it > 0 }?.let { path.subpath(0, it) } ?: Path("") },
					{
						Text(dir.name)
					},
				)
			}
		}
		val attributes = remember(userDirPath, path) {
			(userDirPath / path).readAttributes<BasicFileAttributes>(LinkOption.NOFOLLOW_LINKS)
		}
		when {
			attributes.isSymbolicLink ->
				SymbolicLinkScreen(userDirPath / path) { viewModel.path = it }

			attributes.isDirectory ->
				DirectoryScreen(userDirPath / path) { viewModel.path = it }

			attributes.isRegularFile ->
				RegularFileScreen(userDirPath / path)

			else ->
				Unit
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetentionPageTopBar() {
	TopAppBar({
		Text(stringResource(R.string.retention_label))
	})
}

@Composable
private fun SymbolicLinkScreen(path: Path, onSetPath: (Path) -> Unit) {
	val context = LocalContext.current
	val targetPath = remember(path) { runCatching { path.parent / path.readSymbolicLink() }.getOrNull() }
	Column(
		modifier = Modifier
			.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		chooseEntryIcon(targetPath)()
		targetPath?.let { targetPath ->
			val relativeTargetPath = remember {
				runCatching {
					val userId = DxrSettings.Models.userProfileNotNull.userIdNotNull
					targetPath.relativeToOrNull(context.userDirPathOf(userId))
				}.getOrNull()
			}
			Text((relativeTargetPath ?: targetPath).pathString)
			relativeTargetPath?.let { relativeTargetPath ->
				Button(
					{ onSetPath(relativeTargetPath) },
				) {
					Text(stringResource(R.string.go_to_target_position))
				}
			} ?: Text(stringResource(R.string.cannot_go_to_target_position))
		} ?: Text(stringResource(R.string.cannot_read_target_position))
	}
}

@Composable
private fun DirectoryScreen(path: Path, onSetPath: (Path) -> Unit) {
	val context = LocalContext.current
	val entries = remember(path) {
		path.listDirectoryEntries().sortedWith(
			compareBy<Path> { !it.isDirectory(LinkOption.NOFOLLOW_LINKS) }
				.thenBy { it.name.lowercase() },
		)
	}
	LazyColumn {
		items(entries) { entry ->
			ListItem(
				{
					Text(entry.name)
				},
				modifier = Modifier
					.clickable {
						runCatching {
							val userId = DxrSettings.Models.userProfileNotNull.userIdNotNull
							val entryPath = (path / entry.name).relativeTo(context.userDirPathOf(userId))
							onSetPath(entryPath)
						}
					},
				leadingContent = chooseEntryIcon(entry),
			)
		}
	}
}

@Composable
private fun RegularFileScreen(path: Path) {
	val text = remember(path) {
		path.inputStream(LinkOption.NOFOLLOW_LINKS).use { `in` ->
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				`in`.readNBytes(DEFAULT_BUFFER_SIZE)
			} else {
				ByteArray(DEFAULT_BUFFER_SIZE).also { `in`.read(it) }
			}
		}.decodeToString(throwOnInvalidSequence = false)
	}
	OutlinedTextField(
		text,
		{ },
		readOnly = true,
		textStyle = TextStyle(
			fontFamily = FontFamily.Monospace,
		),
	)
}

@Composable
private fun PropertiesScreen(path: Path, onSetPath: (Path) -> Unit) {
	val context = LocalContext.current
	val targetPath = remember(path) { path.readSymbolicLink() }
	val attributes = path.readAttributes<BasicFileAttributes>(LinkOption.NOFOLLOW_LINKS)
	Column {
		ListItem(
			{
				Text("${path.name} -> ${targetPath.relativeToOrSelf(context.usersDirPath).pathString}")
			},
			supportingContent = {
				Text("File")
			},
		)
		ListItem(
			{
				Text(Formatter.formatFileSize(context, attributes.size()))
			},
			supportingContent = {
				Text("Size")
			},
		)
		ListItem(
			{
				val permissions = remember(path) { path.getPosixFilePermissions(LinkOption.NOFOLLOW_LINKS) }
				val owner = remember(path) { path.getOwner(LinkOption.NOFOLLOW_LINKS) }
				Text(buildString {
					append(permissions)
					owner?.let { owner ->
						append(' ')
						append(owner.name)
					}
				})
			},
			supportingContent = {
				Text("Permissions")
			},
		)
		ListItem(
			{
				Text(attributes.lastAccessTime().toString())
			},
			supportingContent = {
				Text("Access")
			},
		)
		ListItem(
			{
				Text(attributes.lastModifiedTime().toString())
			},
			supportingContent = {
				Text("Modify")
			},
		)
		ListItem(
			{
				Text(attributes.creationTime().toString())
			},
			supportingContent = {
				Text("Create")
			},
		)
	}
}

private fun chooseEntryIcon(path: Path?): @Composable () -> Unit {
	val attributes = path?.readAttributes<BasicFileAttributes>(LinkOption.NOFOLLOW_LINKS)
	return when {
		attributes?.isSymbolicLink == true -> {
			{
				Icon(Icons.Default.Link, null)
			}
		}

		attributes?.isDirectory == true -> {
			{
				Icon(Icons.Default.Folder, null)
			}
		}

		attributes?.isRegularFile == true -> {
			{
				Icon(Icons.Default.Description, null)
			}
		}

		else -> {
			{
				Icon(Icons.Default.QuestionMark, null)
			}
		}
	}
}

class RetentionViewModel : ViewModel() {
	var path by mutableStateOf(Path(""))
}
