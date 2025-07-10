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

package dart.package0.dan_xi.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import dart.package0.dan_xi.common.Constant
import kotlin.math.sqrt

val Color.hslFloatArray
	get() = FloatArray(3)
		.also { ColorUtils.colorToHSL(toArgb(), it) }

val Color.hue get() = hslFloatArray[0]

val Color.saturation get() = hslFloatArray[1]

val Color.lightness get() = hslFloatArray[2]

fun Color.withHue(hue: Float) = hslFloatArray
	.let { Color.hsl(hue, it[1], it[2]) }

fun Color.withHue(hue: (FloatArray) -> Float) = hslFloatArray
	.let { Color.hsl(hue(it), it[1], it[2]) }

fun Color.withSaturation(saturation: Float) = hslFloatArray
	.let { Color.hsl(it[0], saturation, it[2]) }

fun Color.withSaturation(saturation: (FloatArray) -> Float) = hslFloatArray
	.let { Color.hsl(it[0], saturation(it), it[2]) }

fun Color.withLightness(lightness: Float) = hslFloatArray
	.let { Color.hsl(it[0], it[2], lightness) }

fun Color.withLightness(lightness: (FloatArray) -> Float) = hslFloatArray
	.let { Color.hsl(it[0], it[1], lightness(it)) }

fun Color.autoAdapt(systemInDarkTheme: Boolean) = hslFloatArray
	.let {
		when {
			systemInDarkTheme && it[2] < 0.5 -> withLightness(sqrt(it[2]) * 3 / 2)
			!systemInDarkTheme && it[2] > 0.5 -> withLightness(it[2] * it[2] * 2 / 3)
			else -> this
		}
	}

fun String.hashColor(systemInDarkTheme: Boolean) = takeUnless {
	isEmpty() || startsWith("*")
}?.run {
	val colorName = Constant.tagColorList[chars().sum() % Constant.tagColorList.size]
	Constant.getColorFromString(colorName)[if (systemInDarkTheme) 300 else 800] ?: Color.Red
}
