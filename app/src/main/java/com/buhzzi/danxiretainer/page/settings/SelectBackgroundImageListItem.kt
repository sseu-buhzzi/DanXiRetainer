package com.buhzzi.danxiretainer.page.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.backgroundImagePath
import com.buhzzi.danxiretainer.repository.settings.backgroundImagePathStringFlow
import com.buhzzi.danxiretainer.repository.settings.userProfile
import com.buhzzi.danxiretainer.util.backgroundImagePathOf
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.outputStream

@Composable
fun SelectBackgroundImageListItem() {
	// TODO 用戶登入後將設定目錄中的圖像移動至用戶設定目錄
	val context = LocalContext.current

	val backgroundImagePathString by DxrSettings.Prefs.backgroundImagePathStringFlow.collectAsState(null)

	var dialogEvent by remember { mutableStateOf<DialogEvent?>(null) }

	val errorReadingLabel = stringResource(R.string.background_image_error_reading_label)
	val errorDecodingLabel = stringResource(R.string.background_image_error_decoding_label)
	val onImageSelected by rememberUpdatedState { uri: Uri ->
		runCatching {
			val `in` = context.contentResolver.openInputStream(uri)
				?: error(errorReadingLabel)
			val bitmap = BitmapFactory.decodeStream(`in`)
				?: error(errorDecodingLabel)
			context.addBackgroundImage(bitmap)
		}.getOrElse { exception ->
			dialogEvent = DialogEvent.InformException(exception)
		}
	}
	val selectBackgroundImageLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.PickVisualMedia(),
	) { uri ->
		uri?.let { onImageSelected(it) }
	}
	dialogEvent?.Dialog { dialogEvent = it }
	ListItem(
		{
			Text(stringResource(R.string.background_image_label))
		},
		modifier = Modifier
			.clickable {
				val targetPath = context.backgroundImagePathOf(DxrSettings.Models.userProfile?.userId)
				if (targetPath.exists()) {
					dialogEvent = DialogEvent.ConfirmRemoval(targetPath)
				} else {
					selectBackgroundImageLauncher.launch(
						PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
					)
				}
			},
		supportingContent = {
			Text(
				settingsValueStringResource(
					// serves as `String` here
					backgroundImagePathString,
					nullLabelId = R.string.background_image_pick_one_label,
				),
			)
		},
		leadingContent = {
			Icon(Icons.Default.PhotoLibrary, null)
		},
	)
}

private sealed class DialogEvent {
	data class InformException(
		val cause: Throwable,
	) : DialogEvent() {
		@Composable
		override fun Dialog(dialogEventSetter: (DialogEvent?) -> Unit) {
			AlertDialog(
				{ dialogEventSetter(null) },
				{
					TextButton({ dialogEventSetter(null) }) {
						Text(stringResource(R.string.confirm_label))
					}
				},
				dismissButton = {
					TextButton({ dialogEventSetter(null) }) {
						Text(stringResource(R.string.dismiss_label))
					}
				},
				icon = {
					Icon(Icons.Default.Error, null)
				},
				title = {
					Text(stringResource(R.string.background_image_exception_happened_label))
				},
				text = {
					Text(cause.toString())
				},
			)
		}
	}

	data class ConfirmRemoval(
		val targetPath: Path,
	) : DialogEvent() {
		@Composable
		override fun Dialog(dialogEventSetter: (DialogEvent?) -> Unit) {
			val context = LocalContext.current

			AlertDialog(
				{ dialogEventSetter(null) },
				{
					TextButton({
						runCatching {
							context.removeBackgroundImage()
						}.getOrElse { exception ->
							dialogEventSetter(InformException(exception))
						}
						dialogEventSetter(null)
					}) {
						Text(stringResource(R.string.confirm_label))
					}
				},
				dismissButton = {
					TextButton({ dialogEventSetter(null) }) {
						Text(stringResource(R.string.dismiss_label))
					}
				},
				icon = {
					Icon(Icons.Default.Warning, null)
				},
				text = {
					Text(stringResource(R.string.background_image_confirm_delete_label))
				},
			)
		}
	}

	@Composable
	abstract fun Dialog(dialogEventSetter: (DialogEvent?) -> Unit)
}

private fun Context.addBackgroundImage(bitmap: Bitmap) {
	val targetPath = backgroundImagePathOf(DxrSettings.Models.userProfile?.userId)
	targetPath.createParentDirectories().outputStream().use { out ->
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
	}
	DxrSettings.Models.backgroundImagePath = targetPath
}

private fun Context.removeBackgroundImage() {
	val backgroundImagePath = DxrSettings.Models.backgroundImagePath ?: return
	backgroundImagePath.deleteIfExists()
	DxrSettings.Models.backgroundImagePath = null
}
