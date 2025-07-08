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

package dart.dan_xi.util.forum

import android.content.Context
import com.buhzzi.danxiretainer.R
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/// Create human-readable duration, e.g.: 1 hour ago, 2 days ago
object HumanDuration {
	private val defaultFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

	fun tryFormat(context: Context, dateTime: OffsetDateTime): String = Duration.between(
		dateTime,
		OffsetDateTime.now(),
	).run {
		when {
			seconds < 1 -> context.getString(R.string.human_duration_moments_label)
			toMinutes() < 1 -> context.getString(R.string.human_duration_seconds_label, seconds)
			toHours() < 1 -> context.getString(R.string.human_duration_minutes_label, toMinutes())
			toDays() < 1 -> context.getString(R.string.human_duration_hours_label, toHours())
			toDays() <= 30 -> context.getString(R.string.human_duration_days_label, toDays())
			else -> dateTime.format(defaultFormatter)
		}
	}
}
