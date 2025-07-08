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

package dart.dan_xi.provider


enum class SortOrder { LAST_REPLIED, LAST_CREATED;
	val internalString get() = when (this) {
		LAST_REPLIED -> "time_updated"
		LAST_CREATED -> "time_created"
	}
}

//Forum Folded Post Behavior
enum class FoldBehavior { SHOW, FOLD, HIDE;
	val internalString get() = when (this) {
		FOLD -> "fold"
		HIDE -> "hide"
		SHOW -> "show"
	}
}

fun foldBehaviorFromInternalString(str: String?) = when (str) {
	"fold" -> FoldBehavior.FOLD
	"hide" -> FoldBehavior.HIDE
	"show" -> FoldBehavior.SHOW
	else -> FoldBehavior.FOLD
}

enum class OtNotificationTypes { MENTION, SUBSCRIPTION, REPORT;
	val internalString get() = when (this) {
		MENTION -> "mention"
		SUBSCRIPTION -> "favorite" // keep 'favorite' here for backward support
		REPORT -> "report"
	}
}

fun notificationTypeFromInternalString(str: String) = when (str) {
	"mention" -> OtNotificationTypes.MENTION
	"favorite" -> OtNotificationTypes.SUBSCRIPTION
	"report" -> OtNotificationTypes.REPORT
	else -> null
}

enum class ThemeType { LIGHT, DARK, SYSTEM;
	val internalString get() = when (this) {
		LIGHT -> "light"
		DARK -> "dark"
		SYSTEM -> "system"
	}


}

fun themeTypeFromInternalString(str: String?) = when (str) {
	"light" -> ThemeType.LIGHT
	"dark" -> ThemeType.DARK
	"system" -> ThemeType.SYSTEM
	else -> null
}