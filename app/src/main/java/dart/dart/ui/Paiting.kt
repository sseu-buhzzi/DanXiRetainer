// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
package dart.dart.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import kotlin.math.pow

// See <https://www.w3.org/TR/WCAG20/#relativeluminancedef>
@Suppress("UnusedReceiverParameter")
private fun Color.linearizeColorComponent(component: Float): Float {
	if (component <= 0.03928F) {
		return component / 12.92F
	}
	return ((component + 0.055F) / 1.055F).pow(2.4F)
}

/// Returns a brightness value between 0 for darkest and 1 for lightest.
///
/// Represents the relative luminance of the color. This value is computationally
/// expensive to calculate.
///
/// See <https://en.wikipedia.org/wiki/Relative_luminance>.
fun Color.computeLuminance(): Float {
	assert(colorSpace != ColorSpaces.ExtendedSrgb)
	// See <https://www.w3.org/TR/WCAG20/#relativeluminancedef>
	val r = linearizeColorComponent(red)
	val g = linearizeColorComponent(green)
	val b = linearizeColorComponent(blue)
	return 0.2126F * r + 0.7152F * g + 0.0722F * b
}
