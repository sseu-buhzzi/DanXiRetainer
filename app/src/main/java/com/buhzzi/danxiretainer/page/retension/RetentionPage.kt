package com.buhzzi.danxiretainer.page.retension

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.userProfile
import com.buhzzi.danxiretainer.repository.settings.userProfileNotNull
import com.buhzzi.danxiretainer.util.LocalSnackbarProvider
import com.buhzzi.danxiretainer.util.userDirPathOf
import com.buhzzi.danxiretainer.util.usersDirPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import java.nio.file.FileVisitResult
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories
import kotlin.io.path.createSymbolicLinkPointingTo
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.io.path.getOwner
import kotlin.io.path.getPosixFilePermissions
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.outputStream
import kotlin.io.path.pathString
import kotlin.io.path.readAttributes
import kotlin.io.path.readSymbolicLink
import kotlin.io.path.relativeTo
import kotlin.io.path.relativeToOrNull
import kotlin.io.path.relativeToOrSelf
import kotlin.io.path.useDirectoryEntries
import kotlin.io.path.visitFileTree

@Composable
fun RetentionPageContent(modifier: Modifier = Modifier) {
	val context = LocalContext.current
	val snackbarProvider = LocalSnackbarProvider.current
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
			snackbarProvider.runShowing {
				(userDirPath / path).readAttributes<BasicFileAttributes>(LinkOption.NOFOLLOW_LINKS)
			}.getOrElse {
				viewModel.path = path.parent
				null
			}
		}
		when {
			attributes?.isSymbolicLink == true -> SymbolicLinkScreen(userDirPath / path) {
				viewModel.path = it
			}

			attributes?.isDirectory == true -> DirectoryScreen(userDirPath / path) {
				viewModel.path = it
			}

			attributes?.isRegularFile == true -> RegularFileScreen(userDirPath / path)

			// TODO implement it, like a question or something
			else -> Unit
		}
	}
}

@Composable
fun RetentionPageTopBar() {
	@OptIn(ExperimentalMaterial3Api::class)
	TopAppBar(
		{
			Text(stringResource(R.string.retention_label))
		},
		actions = {
			ClearRetentionButton()
			ImportRetentionButton()
			ExportRetentionButton()
		},
	)
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

@Composable
private fun ClearRetentionButton() {
	val context = LocalContext.current
	val snackbarProvider = LocalSnackbarProvider.current
	val scope = rememberCoroutineScope()
	IconButton(
		{
			scope.launch(Dispatchers.IO) {
				snackbarProvider.runShowingSuspend {
					val userId = DxrSettings.Models.userProfileNotNull.userIdNotNull
					val userDirPath = context.userDirPathOf(userId)
					userDirPath.useDirectoryEntries { subdirPaths ->
						subdirPaths.forEach { path ->
							@OptIn(ExperimentalPathApi::class)
							path.deleteRecursively()
						}
					}
				}
			}
		},
	) {
		Icon(Icons.Default.DeleteForever, null)
	}
}

@Composable
private fun ImportRetentionButton() {
	val context = LocalContext.current
	val snackbarProvider = LocalSnackbarProvider.current

	var uriNullable by remember { mutableStateOf<Uri?>(null) }
	val importLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.StartActivityForResult(),
	) { result ->
		uriNullable = result.takeIf { it.resultCode == Activity.RESULT_OK }?.data?.data
	}
	uriNullable?.let { uri ->
		val progressState = remember(uri) { ReadingArchiveProgressState.empty() }
		LaunchedEffect(uri) {
			launch(Dispatchers.IO) {
				val userId = DxrSettings.Models.userProfileNotNull.userIdNotNull
				val userDirPath = context.userDirPathOf(userId)
				context.contentResolver.query(
					uri,
					null,
					null,
					null,
					null,
				)?.use { cursor ->
					cursor.runCatching {
						val sizeColumnIndex = getColumnIndexOrThrow(OpenableColumns.SIZE)
						check(moveToFirst())
						progressState.size = getLong(sizeColumnIndex)
					}
				}
				TarArchiveInputStream(checkNotNull(context.contentResolver.openInputStream(uri))).use { tarIn ->
					generateSequence { tarIn.nextEntry }.forEach { entry ->
						snackbarProvider.runShowing {
							progressState.name = entry.name
							val path = userDirPath / entry.name
							when {
								entry.isSymbolicLink -> path.createParentDirectories()
									.createSymbolicLinkPointingTo(Path(entry.linkName))

								entry.isDirectory -> path.createDirectories()

								entry.isFile -> path.createParentDirectories()
									.outputStream()
									.use { out -> tarIn.copyTo(out) }
							}
							progressState.progress = tarIn.bytesRead
						}
					}
				}
				uriNullable = null
			}
		}
		TransferringAlertDialog(
			stringResource(R.string.retention_importing),
			progressState.name,
			{
				// For TAR archives it's probably costing time to measure the size of every directory. Then we only show
				// progress in the file size.
				LinearProgressIndicator(
					{ progressState.progress / progressState.size.toFloat() },
					modifier = Modifier
						.fillMaxWidth(),
				)
			},
			{
				Icon(Icons.Default.MoveToInbox, null)
			},
		) { uriNullable = null }
	}

	IconButton(
		{
			snackbarProvider.runShowing {
				val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
					addCategory(Intent.CATEGORY_OPENABLE)
					type = "*/*"
					putExtra(
						Intent.EXTRA_MIME_TYPES,
						arrayOf("application/x-tar", "application/x-gtar"),
					)
				}
				importLauncher.launch(intent)
			}
		},
	) {
		Icon(Icons.Default.MoveToInbox, null)
	}
}

@Composable
private fun ExportRetentionButton() {
	val context = LocalContext.current
	val snackbarProvider = LocalSnackbarProvider.current

	var uriNullable by remember { mutableStateOf<Uri?>(null) }
	val exportLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.StartActivityForResult(),
	) { result ->
		uriNullable = result.takeIf { it.resultCode == Activity.RESULT_OK }?.data?.data
	}
	uriNullable?.let { uri ->
		val progressStack = remember(uri) { WalkingTreeProgressStack() }
		LaunchedEffect(uri) {
			launch(Dispatchers.IO) {
				val userId = DxrSettings.Models.userProfileNotNull.userIdNotNull
				val userDirPath = context.userDirPathOf(userId)
				TarArchiveOutputStream(checkNotNull(context.contentResolver.openOutputStream(uri))).use { tarOut ->
					tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)
					userDirPath.visitFileTree(
						followLinks = false,
					) {
						onPreVisitDirectory { path, attributes ->
							val name = userDirPath.relativize(path).pathString
							snackbarProvider.runShowing {
								tarOut.putArchiveEntry(TarArchiveEntry(path, name))
								tarOut.closeArchiveEntry()
							}
							progressStack.push(path, name)
							FileVisitResult.CONTINUE
						}
						onPostVisitDirectory { path, attributes ->
							progressStack.pop()
							progressStack.walk()
							FileVisitResult.CONTINUE
						}
						onVisitFile { path, attributes ->
							val name = userDirPath.relativize(path).pathString
							snackbarProvider.runShowing {
								tarOut.putArchiveEntry(TarArchiveEntry(path, name))
								if (!attributes.isSymbolicLink) {
									path.inputStream(LinkOption.NOFOLLOW_LINKS).use { `in` ->
										`in`.copyTo(tarOut)
									}
								}
								tarOut.closeArchiveEntry()
							}
							progressStack.walk()
							FileVisitResult.CONTINUE
						}
					}
				}
				uriNullable = null
			}
		}
		TransferringAlertDialog(
			stringResource(R.string.retention_exporting),
			progressStack.last().name,
			{
				Column(
					modifier = Modifier
						.fillMaxWidth(),
					horizontalAlignment = Alignment.End,
				) {
					// We're showing hierarchal progress bars for every directory in the path.
					// option TODO configurable max depth
					progressStack.take(64)
						.zipWithNext()
						.forEach { (parentState, state) ->
							val full = state.generation
							val begin = state.position
							val end = (parentState.position + 1) * state.degree
							val rest = (full - begin).toFloat()
							LinearProgressIndicator(
								{ (end - begin) / rest },
								modifier = Modifier
									.fillMaxWidth(rest / full),
							)
						}
				}
			},
			{
				Icon(Icons.Default.Outbox, null)
			},
		) { uriNullable = null }
	}

	IconButton(
		{
			snackbarProvider.runShowing {
				val userId = DxrSettings.Models.userProfileNotNull.userIdNotNull
				val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
					addCategory(Intent.CATEGORY_OPENABLE)
					type = "application/x-tar"
					putExtra(
						Intent.EXTRA_TITLE,
						"${context.getString(R.string.app_name)}-$userId-${System.currentTimeMillis().toHexString()}.tar",
					)
				}
				exportLauncher.launch(intent)
			}
		},
	) {
		Icon(Icons.Default.Outbox, null)
	}
}

@Composable
private fun TransferringAlertDialog(
	description: String,
	name: String,
	progress: @Composable () -> Unit,
	icon: @Composable () -> Unit,
	dismiss: () -> Unit,
) {
	AlertDialog(
		dismiss,
		{
			TextButton(dismiss) {
				Text(stringResource(R.string.dismiss_label))
			}
		},
		icon = icon,
		text = {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Text(
					description,
					modifier = Modifier
						.fillMaxWidth(),
					textAlign = TextAlign.Center,
				)
				progress()
				Text(
					name,
					modifier = Modifier
						.fillMaxWidth(),
					textAlign = TextAlign.Center,
				)
			}
		},
	)
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

private class ReadingArchiveProgressState(
	name: String,
	size: Long,
	progress: Long,
) {
	var name by mutableStateOf(name)
	var size by mutableLongStateOf(size)
	var progress by mutableLongStateOf(progress)

	companion object {
		fun empty() = ReadingArchiveProgressState("", 1, 0)
	}
}

private class WalkingTreeProgressStack : AbstractList<WalkingTreeProgressStack.StackState>() {
	override val size get() = stack.size

	override fun get(index: Int) = stack[index]

	private val stack = mutableStateListOf(StackState.empty())

	fun push(dirPath: Path, name: String) {
		val degree = dirPath.listDirectoryEntries().size
		val parentState = stack.last()
		val generation = parentState.generation * degree
		val position = parentState.position * degree
		stack.add(StackState(name, degree, generation, position))
	}

	fun pop() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
			stack.removeLast()
		} else {
			checkNotNull(stack.removeLastOrNull())
		}
	}

	fun walk() {
		++stack.last().position
	}

	class StackState(
		val name: String,
		val degree: Int,
		val generation: Int,
		position: Int,
	) {
		var position by mutableIntStateOf(position)

		companion object {
			fun empty() = StackState("", 1, 1, 0)
		}
	}
}

class RetentionViewModel : ViewModel() {
	var path by mutableStateOf(Path(""))
}
