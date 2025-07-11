package com.buhzzi.danxiretainer.page.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.model.settings.DxrHttpProxy
import com.buhzzi.danxiretainer.page.DxrScaffoldWrapper
import com.buhzzi.danxiretainer.repository.api.forum.DxrForumApi
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.authBaseUrl
import com.buhzzi.danxiretainer.repository.settings.authBaseUrlFlow
import com.buhzzi.danxiretainer.repository.settings.forumBaseUrl
import com.buhzzi.danxiretainer.repository.settings.forumBaseUrlFlow
import com.buhzzi.danxiretainer.repository.settings.httpProxy
import com.buhzzi.danxiretainer.repository.settings.httpProxyFlow
import com.buhzzi.danxiretainer.repository.settings.imageBaseUrl
import com.buhzzi.danxiretainer.repository.settings.imageBaseUrlFlow
import dart.package0.dan_xi.common.Constant
import dart.package0.dan_xi.util.WebvpnProxy

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsNetworkPage() {
	DxrScaffoldWrapper(
		topBar = {
			SettingsSubpageTopBar("${stringResource(R.string.settings_label)} - ${stringResource(R.string.network_label)}")
		}
	) { contentPadding ->
		Column(
			modifier = Modifier
				.padding(contentPadding)
				.fillMaxSize()
				.verticalScroll(rememberScrollState()),
		) {
			val authBaseUrl by DxrSettings.Items.authBaseUrlFlow.collectAsState(null)
			InputListItem(
				authBaseUrl ?: "",
				stringResource(R.string.auth_base_url_label),
				settingsValueStringResource(authBaseUrl),
				null,
				Icons.Default.Dns,
			) { text ->
				DxrSettings.Items.authBaseUrl = text
				true
			}

			val forumBaseUrl by DxrSettings.Items.forumBaseUrlFlow.collectAsState(null)
			InputListItem(
				forumBaseUrl ?: "",
				stringResource(R.string.forum_base_url_label),
				settingsValueStringResource(forumBaseUrl),
				null,
				Icons.Default.Dns,
			) { text ->
				DxrSettings.Items.forumBaseUrl = text
				true
			}

			val imageBaseUrl by DxrSettings.Items.imageBaseUrlFlow.collectAsState(null)
			InputListItem(
				imageBaseUrl ?: "",
				stringResource(R.string.image_base_url_label),
				settingsValueStringResource(imageBaseUrl),
				null,
				Icons.Default.Dns,
			) { text ->
				DxrSettings.Items.imageBaseUrl = text
				true
			}

			val serversAlternatives = listOf(
				"fduhole.com",
				"webvpn.fudan.edu.cn",
			)
			SingleSelectListItem(
				serversAlternatives,
				0,
				stringResource(R.string.servers_alternatives_label),
				"",
				Icons.Default.MoreHoriz,
			) { selection ->
				when (selection?.let { serversAlternatives[it] }) {
					"fduhole.com" -> {
						DxrSettings.Items.authBaseUrl = Constant.AUTH_BASE_URL
						DxrSettings.Items.forumBaseUrl = Constant.FORUM_BASE_URL
						DxrSettings.Items.imageBaseUrl = Constant.IMAGE_BASE_URL
						true
					}

					"webvpn.fudan.edu.cn" -> {
						DxrSettings.Items.authBaseUrl = WebvpnProxy.vpnPrefix["auth.fduhole.com"]
						DxrSettings.Items.forumBaseUrl = WebvpnProxy.vpnPrefix["forum.fduhole.com"]
						DxrSettings.Items.imageBaseUrl = WebvpnProxy.vpnPrefix["image.fduhole.com"]
						true
					}

					else -> false
				}
			}

			val httpProxy by DxrSettings.Models.httpProxyFlow.collectAsState(null)
			ToggleListItem(
				httpProxy?.enabled == true,
				stringResource(R.string.proxy_label),
				stringResource(R.string.use_proxy_to_access_internet_label),
				Icons.Default.NetworkPing,
			) { checked ->
				(httpProxy ?: DxrHttpProxy()).copy(
					enabled = checked,
				).let { updateHttpProxy(it) }
			}
			AnimatedVisibility(httpProxy?.enabled == true) {
				Column {
					InputListItem(
						httpProxy?.host ?: "",
						stringResource(R.string.host_label),
						settingsValueStringResource(httpProxy?.host),
						stringResource(R.string.proxy_server_host_label),
						Icons.Default.Dns,
					) { text ->
						httpProxy?.copy(
							host = text,
						)?.let { updateHttpProxy(it) }
						true
					}
					InputListItem(
						httpProxy?.port?.toString() ?: "",
						stringResource(R.string.port_label),
						settingsValueStringResource(httpProxy?.port?.toString()),
						stringResource(R.string.proxy_server_port_label),
						Icons.Default.SettingsInputComponent,
					) { text ->
						text.toUShortOrNull()?.toInt()?.let { port ->
							httpProxy?.copy(
								port = port,
							)?.let { updateHttpProxy(it) }
							true
						} == true
					}
				}
			}
		}
	}
}

private fun updateHttpProxy(httpProxy: DxrHttpProxy) {
	DxrSettings.Models.httpProxy = httpProxy
	DxrForumApi.updateClient()
}
