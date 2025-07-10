// Copyright 2014 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package dart.package0.flutter.src.painting

import androidx.compose.ui.graphics.Color

/// A color that has a small table of related colors called a "swatch".
///
/// The table is accessed by key values of type `T`.
///
/// See also:
///
///  * [MaterialColor] and [MaterialAccentColor], which define Material Design
///    primary and accent color swatches.
///  * [Colors], which defines all of the standard Material Design
///    colors.
open class ColorSwatch<T>(
	primary: Long,
	protected val swatch: Map<T, Color>,
) {
	val color = Color(primary)

	/// Creates a color that has a small table of related colors called a "swatch".
	///
	/// The `primary` argument should be the 32 bit ARGB value of one of the
	/// values in the swatch, as would be passed to the [Color.new] constructor
	/// for that same color, and as is exposed by [value]. (This is distinct from
	/// the key of any color in the swatch.)

	/// Returns an element of the swatch table.
	operator fun get(key: T) = swatch[key]

	/// Returns the valid keys for accessing operator[].
	val keys get() = swatch.keys
}
