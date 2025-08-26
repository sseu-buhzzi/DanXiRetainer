/*
 *     Portions translated from original Dart source of DanXi-Dev
 *
 *     Copyright (C) 2021  DanXi-Dev
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dart.package0.dan_xi.common

import dart.package0.flutter.src.material.Colors
import java.time.OffsetDateTime
import java.time.ZoneOffset

/// Store some important constants, such as app id, default color styles, etc.
object Constant {
	/// The number of posts on each pages returned from the server of Forum.
	const val POST_COUNT_PER_PAGE: Long = 10

	/// The number of search results on each pages returned from the server of Danke.
	const val SEARCH_COUNT_PER_PAGE: Long = 10

	const val SUPPORT_QQ_GROUP = "941342818"

	/// The division name of the curriculum page. We use this to determine whether
	/// we should show the curriculum page (instead of a normal forum division).
	///
	/// See also:
	///
	/// * [ListDelegate], which determines the page content per division.
	/// * [PostsType], which can represent a special division.
	/// * [OTDivision], whose name is what we compare with.
	const val SPECIAL_DIVISION_FOR_CURRICULUM = "评教"

	/// The default user agent used by the app.
	///
	/// Note that this is not the same as the user agent used by the WebView, or the
	/// forum's [Dio]. Those two are set by WebView and [ForumRepository].
	const val  DEFAULT_USER_AGENT =
		"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36"

	const val APPSTORE_APPID = "1568629997"

	/// A link to the "forget password" page of Forum.
	const val FORUM_FORGOT_PASSWORD_URL =
	"https://auth.fduhole.com/register?type=forget_password"

	const val FORUM_REGISTER_URL = "https://auth.fduhole.com/register"

	/// The default start date of a semester.
	val defaultSemesterStartDate: OffsetDateTime = OffsetDateTime.of(2023, 2, 20, 0, 0, 0, 0, ZoneOffset.ofHours(8))

	const val UIS_URL = "https://uis.fudan.edu.cn/authserver/login"
	const val UIS_HOST = "uis.fudan.edu.cn"

	/// The default URLs of [ForumRepository] and [CurriculumBoardHoleRepository].
	///
	const val FORUM_BASE_URL_LEGACY = "https://www.fduhole.com/api"
	const val FORUM_BASE_URL = "https://forum.fduhole.com/api"
	const val AUTH_BASE_URL = "https://auth.fduhole.com/api"
	const val IMAGE_BASE_URL = "https://image.fduhole.com"
	const val DANKE_BASE_URL = "https://danke.fduhole.com/api"

	/// An link to the FAQ page of Danxi.
	const val FAQ_URL =
		"https://danxi-dev.feishu.cn/wiki/wikcnrPPGDCiTODBYRkdwLlHH65"

	/// The keys of special cards that are not features, but can be added to the dashboard.
	///
	/// See also:
	/// - [DashboardCard]
	/// - [registerFeature]

	/// A divider.
	const val FEATURE_DIVIDER = "divider"

	/// Not a displayable feature, but indicates the start of a new card.
	/// i.e. the content below this feature will be shown in a new card.
	const val FEATURE_NEW_CARD = "new_card"

	/// A custom card, allowing user to tap to jump to a web location.
	const val FEATURE_CUSTOM_CARD = "custom_card"

	/// Add a Chinese symbol(￥) at the end of [num].
	///
	/// If [num] is empty, return an empty string.
	fun yuanSymbol(num: String?) = num
		?.takeIf { it.isNotBlank() }
		?.let { "\u00a5$it" }
		?: ""

	/// An Unicode ZERO WIDTH SPACE wrapper.
	///
	/// We mainly use it to relieve vertical alignment issues.
	/// See https://github.com/flutter/flutter/issues/128019 for details.
	///
	/// Remove this method and its usage if the issue has been resolved.
	fun withZwb(originalStr: String?) = originalStr
		?.let { "$it\u200b" }
		?: ""

	/// A list of tag colors used by Forum.
	val tagColorList = listOf(
		"ed",
		"pink",
		"purple",
		"deep-purple",
		"indigo",
		"blue",
		"light-blue",
		"yan",
		"teal",
		"green",
		"light-green",
		"lime",
		"yellow",
		"amber",
		"orange",
		"deep-orange",
		"brown",
		"blue-grey",
		"grey",
	)

	/// Get the corresponding [Color] from a color string.
	fun getColorFromString(color: String?) = when (color) {
		"red" -> Colors.red
		"pink" -> Colors.pink
		"purple" -> Colors.purple
		"deep-purple" -> Colors.deepPurple
		"indigo" -> Colors.indigo
		"blue" -> Colors.blue
		"light-blue" -> Colors.lightBlue
		"cyan" -> Colors.cyan
		"teal" -> Colors.teal
		"green" -> Colors.green
		"light-green" -> Colors.lightGreen
		"lime" -> Colors.lime
		"yellow" -> Colors.yellow
		"amber" -> Colors.amber
		"orange" -> Colors.orange
		"deep-orange" -> Colors.deepOrange
		"brown" -> Colors.brown
		"blue-grey" -> Colors.blueGrey
		"grey" -> Colors.grey
		else -> Colors.red
	}

	/// A list of Fudan campus.
	///
	/// It is a copy of [Campus.values] except [Campus.NONE].
	val campusValues = listOf(
		Campus.HANDAN_CAMPUS,
		Campus.FENGLIN_CAMPUS,
		Campus.JIANGWAN_CAMPUS,
		Campus.ZHANGJIANG_CAMPUS,
	)

	///A list of provided languages
	///
	/// It is a copy of [Language.values] except [Language.NONE].
	val languageValues = listOf(
		Language.SIMPLE_CHINESE,
		Language.ENGLISH,
		Language.JAPANESE,
	)

	/// A default configuration JSON string for setting special days to celebrate
	/// in lunar calendar.
	///
	/// It is only used as a fallback when [AnnouncementRepository] cannot obtain the config from
	/// server.
	const val SPECIAL_DAYS = """[
		{
			"type": 1,
			"date": "除夕",
			"celebrationWords": [
			"万物迎春送残腊，一年结局在今宵。🎇",
			"鼓角梅花添一部，五更欢笑拜新年。🎇",
			"冬尽今宵促，年开明日长。🎇",
			"春风来不远，只在屋东头。"
			]
		},
		{
			"type": 1,
			"date": "春节",
			"celebrationWords": [
			"爆竹声中一岁除，春风送暖入屠苏。🎆",
			"不须迎向东郊去，春在千门万户中。🎆",
			"松竹含新秋，轩窗有余清。"
			]
		}
	]"""

	val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
}

enum class Language {
	SIMPLE_CHINESE, ENGLISH, JAPANESE, NONE;

	companion object {
		private val language = listOf("简体中文", "English", "日本語")

		fun fromChineseName(name: String) = language.indexOfFirst { it.contains(name) }
			.takeIf { it != -1 }
			?.let { Constant.languageValues[it] }
			?: NONE
	}
}

/// A list of Fudan campus.
enum class Campus {
	HANDAN_CAMPUS,
	FENGLIN_CAMPUS,
	JIANGWAN_CAMPUS,
	ZHANGJIANG_CAMPUS,
	NONE,
	;

	fun getTeachingBuildings() = when (this) {
		HANDAN_CAMPUS -> listOf("HGX", "H2", "H3", "H4", "H5", "H6")
		FENGLIN_CAMPUS -> listOf("F1", "F2")
		JIANGWAN_CAMPUS -> listOf("JA", "JB")
		ZHANGJIANG_CAMPUS -> listOf("Z2")
		NONE -> listOf("?")
	}

	companion object {
		private val campusName = listOf("邯郸", "枫林", "江湾", "张江")

		fun fromChineseName(name: String?) = name?.let { _ ->
			campusName.indexOfFirst { it.contains(name) }
				.takeIf { it != -1 }
				?.let { Constant.campusValues[it] }
		} ?: NONE
	}
}

/// Define a set of possible connection status.
enum class ConnectionStatus { NONE, CONNECTING, DONE, FAILED, FATAL_ERROR }
