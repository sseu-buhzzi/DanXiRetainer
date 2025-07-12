package com.buhzzi.danxiretainer.page.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.page.DxrDestination
import com.buhzzi.danxiretainer.page.DxrScaffoldWrapper
import com.buhzzi.danxiretainer.page.LocalSnackbarController
import com.buhzzi.danxiretainer.page.runCatchingOnSnackbar
import com.buhzzi.danxiretainer.repository.api.forum.DxrForumApi
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.accessJwt
import com.buhzzi.danxiretainer.repository.settings.accessJwtFlow
import com.buhzzi.danxiretainer.repository.settings.email
import com.buhzzi.danxiretainer.repository.settings.emailFlow
import com.buhzzi.danxiretainer.repository.settings.passwordCt
import com.buhzzi.danxiretainer.repository.settings.passwordCtFlow
import com.buhzzi.danxiretainer.repository.settings.refreshJwt
import com.buhzzi.danxiretainer.repository.settings.refreshJwtFlow
import com.buhzzi.danxiretainer.repository.settings.shouldLoadUserAfterJwt
import com.buhzzi.danxiretainer.repository.settings.shouldLoadUserAfterJwtOrDefault
import com.buhzzi.danxiretainer.repository.settings.shouldLoadUserAfterJwtOrDefaultFlow
import com.buhzzi.danxiretainer.repository.settings.userProfile
import com.buhzzi.danxiretainer.util.androidKeyStoreDecrypt
import com.buhzzi.danxiretainer.util.androidKeyStoreEncrypt
import com.buhzzi.danxiretainer.util.getJwtExpiration
import com.buhzzi.danxiretainer.util.toBytesBase64
import com.buhzzi.danxiretainer.util.toBytesUtf8
import com.buhzzi.danxiretainer.util.toStringBase64
import com.buhzzi.danxiretainer.util.toStringUtf8
import dart.package0.dan_xi.model.forum.JwToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsAccountPage() {
	val context = LocalContext.current

	val scope = rememberCoroutineScope()

	DxrScaffoldWrapper(
		topBar = {
			SettingsSubpageTopBar("${stringResource(R.string.settings_label)} - ${stringResource(R.string.account_label)}")
		}
	) { contentPadding ->
		val snackbarController = LocalSnackbarController.current

		Column(
			modifier = Modifier
				.padding(contentPadding)
				.fillMaxSize()
				.verticalScroll(rememberScrollState()),
		) {
			val email by DxrSettings.Items.emailFlow.collectAsState(null)
			val emailFormatMessage = stringResource(R.string.email_format_description)
			InputListItem(
				email ?: "",
				stringResource(R.string.email_label),
				email ?: stringResource(R.string.unset_label),
				stringResource(R.string.email_input_description),
				Icons.Default.Email,
			) { text ->
				if (text.endsWith("@fudan.edu.cn") || text.endsWith("@m.fudan.edu.cn")) {
					DxrSettings.Items.email = text
					true
				} else {
					Toast.makeText(context, emailFormatMessage, Toast.LENGTH_SHORT).show()
					false
				}
			}

			val passwordCt by DxrSettings.Items.passwordCtFlow.collectAsState(null)
			InputListItem(
				passwordCt?.let { androidKeyStoreDecrypt(it.toBytesBase64()).toStringUtf8() } ?: "",
				stringResource(R.string.password_label),
				passwordCt?.let { stringResource(R.string.view_edit_password_label) } ?: stringResource(R.string.unset_label),
				stringResource(R.string.message_password),
				Icons.Default.Password,
			) { text ->
				DxrSettings.Items.passwordCt = androidKeyStoreEncrypt(text.toBytesUtf8()).toStringBase64()
				true
			}

			NavigateListItem(
				DxrDestination.SettingsAccountUserProfile,
				stringResource(R.string.user_profile_label),
				Icons.Default.Person,
			)

			val jwtExpirationFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") }
			val accessJwt by DxrSettings.Items.accessJwtFlow.collectAsState(null)
			val accessJwtExpirationText by remember {
				derivedStateOf {
					accessJwt?.let { getJwtExpiration(it) }?.format(jwtExpirationFormatter)
				}
			}
			InputListItem(
				accessJwt ?: "",
				stringResource(R.string.access_jwt_label),
				accessJwt?.let {
					accessJwtExpirationText?.let { stringResource(R.string.token_expires_label, it) } ?: stringResource(R.string.token_cannot_parse_label)
				} ?: stringResource(R.string.unavailable_label),
				buildString {
					append(stringResource(R.string.access_jwt_description))
					accessJwtExpirationText?.let { append("\n\n${stringResource(R.string.token_expires_description, it)}") }
				},
				Icons.Default.VpnKey,
			) { text ->
				DxrSettings.Items.accessJwt = text.trim()
				true
			}

			val refreshJwt by DxrSettings.Items.refreshJwtFlow.collectAsState(null)
			val refreshJwtExpirationText by remember {
				derivedStateOf {
					refreshJwt?.let { getJwtExpiration(it) }?.format(jwtExpirationFormatter)
				}
			}
			InputListItem(
				refreshJwt ?: "",
				stringResource(R.string.refresh_jwt_label),
				refreshJwt?.let {
					settingsValueStringResource(
						refreshJwtExpirationText,
						valueLabelId = R.string.token_expires_label,
						nullLabelId = R.string.token_cannot_parse_label,
					)
				} ?: stringResource(R.string.unavailable_label),
				buildString {
					append(stringResource(R.string.refresh_jwt_description))
					refreshJwtExpirationText?.let { append("\n\n${stringResource(R.string.token_expires_description, it)}") }
				},
				Icons.Default.Autorenew,
			) { text ->
				DxrSettings.Items.refreshJwt = text.trim()
				true
			}

			val shouldLoadUserAfterJwt by DxrSettings.Models.shouldLoadUserAfterJwtOrDefaultFlow.collectAsState(
				DxrSettings.Models.shouldLoadUserAfterJwtOrDefault,
			)
			Row(
				modifier = Modifier
					.padding(horizontal = 16.dp),
				horizontalArrangement = Arrangement.spacedBy(16.dp),
			) {
				Button(
					{
						scope.launch(Dispatchers.IO) {
							runCatchingOnSnackbar(snackbarController) {
								val jwToken = DxrForumApi.authLogIn(
									checkNotNull(email) { "No email" },
									androidKeyStoreDecrypt(checkNotNull(passwordCt) { "No password" }.toBytesBase64()).toStringUtf8(),
								)
								handleJwtAndOptionallyFetchUserProfile(jwToken, shouldLoadUserAfterJwt == true)
							}
						}
					},
					modifier = Modifier
						.weight(1F),
				) {
					Text(stringResource(R.string.log_in_label))
				}
				Button(
					{
						scope.launch(Dispatchers.IO) {
							runCatchingOnSnackbar(snackbarController) {
								val jwToken = DxrForumApi.authRefresh(checkNotNull(refreshJwt) { "No refresh JWT" })
								handleJwtAndOptionallyFetchUserProfile(jwToken, shouldLoadUserAfterJwt == true)
							}
						}
					},
					modifier = Modifier
						.weight(1F),
				) {
					Text(stringResource(R.string.refresh_label))
				}
				AnimatedVisibility(
					shouldLoadUserAfterJwt != true,
					enter = slideInHorizontally(
						initialOffsetX = { it },
					) + expandHorizontally(),
					exit = slideOutHorizontally(
						targetOffsetX = { it },
					) + shrinkHorizontally(),
				) {
					Button(
						{
							scope.launch(Dispatchers.IO) {
								runCatchingOnSnackbar(snackbarController) {
									val user = DxrForumApi.getUserProfile()
									DxrSettings.Models.userProfile = user
								}
							}
						},
						modifier = Modifier
							.weight(1F),
					) {
						Text(stringResource(R.string.load_user_label))
					}
				}
			}

			var expandedButtonsDescription by remember { mutableStateOf(false) }
			ListItem(
				{
					Text(stringResource(R.string.account_buttons_have_question_description))
				},
				modifier = Modifier
					.clickable {
						expandedButtonsDescription = !expandedButtonsDescription
					},
				leadingContent = {
					Icon(Icons.Default.QuestionMark, null)
				},
				trailingContent = {
					if (expandedButtonsDescription) {
						Icon(Icons.Default.ExpandLess, null)
					} else {
						Icon(Icons.Default.ExpandMore, null)
					}
				},
				colors = if (expandedButtonsDescription) {
					ListItemDefaults.colors(
						headlineColor = MaterialTheme.colorScheme.primary,
						leadingIconColor = MaterialTheme.colorScheme.primary,
						trailingIconColor = MaterialTheme.colorScheme.primary,
					)
				} else {
					ListItemDefaults.colors()
				}
			)
			AnimatedVisibility(expandedButtonsDescription) {
				ListItem(
					{
						Text(
							stringResource(R.string.account_buttons_description),
							fontSize = 12.sp,
							lineHeight = 16.sp,
						)
					},
					modifier = Modifier
						.clickable {
							expandedButtonsDescription = false
						},
					leadingContent = {
						Icon(Icons.Default.Info, null)
					},
				)
			}

			ToggleListItem(
				shouldLoadUserAfterJwt == true,
				stringResource(R.string.load_user_after_jwt_label),
				stringResource(R.string.load_user_after_jwt_desc_label),
				Icons.Default.PersonSearch,
			) { checked ->
				DxrSettings.Items.shouldLoadUserAfterJwt = checked
			}
		}
	}
}

suspend fun handleJwtAndOptionallyFetchUserProfile(
	jwToken: JwToken,
	shouldLoadUser: Boolean,
) {
	DxrSettings.Items.accessJwt = jwToken.access
	DxrSettings.Items.refreshJwt = jwToken.refresh
	if (shouldLoadUser == true) {
		val user = DxrForumApi.getUserProfile()
		DxrSettings.Models.userProfile = user
	}
}
