// Copyright 2014 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

/// @docImport 'app.dart';
/// @docImport 'app_bar.dart';
/// @docImport 'app_bar_theme.dart';
/// @docImport 'color_scheme.dart';
/// @docImport 'data_table.dart';
/// @docImport 'expand_icon.dart';
/// @docImport 'theme.dart';
/// @docImport 'theme_data.dart';
/// @docImport 'typography.dart';

package dart.package0.flutter.src.material

import androidx.compose.ui.graphics.Color
import dart.package0.flutter.src.painting.ColorSwatch

/// Defines a single color as well a color swatch with ten shades of the color.
///
/// The color's shades are referred to by index. The greater the index, the
/// darker the color. There are 10 valid indices: 50, 100, 200, ..., 900.
/// The value of this color should the same the value of index 500 and [shade500].
///
/// ## Updating to [ColorScheme]
///
/// The [ColorScheme] is preferred for
/// representing colors in applications that are configured
/// for Material 3 (see [ThemeData.useMaterial3]).
/// For more information on colors in Material 3 see
/// the spec at <https://m3.material.io/styles/color/the-color-system>.
///
///{@template flutter.material.colors.colorRoles}
/// In Material 3, colors are represented using color roles and
/// corresponding tokens. Each property in the [ColorScheme] class
/// represents one color role as defined in the spec above.
/// {@endtemplate}
///
/// ### Material 3 Colors in Flutter
///
///{@template flutter.material.colors.settingColors}
/// Flutter's Material widgets can be assigned colors at the widget level
/// using widget properties,
/// or at the app level using theme classes.
///
/// For example, you can set the background of the [AppBar] by
/// setting the [AppBar.backgroundColor] to a specific [Color] value.
///
/// To globally set the AppBar background color for your app, you
/// can set the [ThemeData.appBarTheme] property for your [MaterialApp]
/// using the [ThemeData] class. You can also override
/// the default appearance of all the [AppBar]s in a widget subtree by
/// placing the [AppBarTheme] at the root of the subtree.
///
/// Alternatively, you can set the [ThemeData.colorScheme] property
/// to a custom [ColorScheme]. This creates a unified [ColorScheme] to be
/// used across the app. The [AppBar.backgroundColor] uses the
/// [ColorScheme.surface] by default.
///{@endtemplate}
///
/// ### Migrating from [MaterialColor] to [ColorScheme]
///
/// In most cases, there are new properties in Flutter widgets that
/// accept a [ColorScheme] instead of a [MaterialColor].
///
/// For example, you may have previously constructed a [ThemeData]
/// using a primarySwatch:
///
/// ```dart
/// ThemeData(
///   primarySwatch: Colors.amber,
/// )
/// ```
///
/// In Material 3, you can use the [ColorScheme] class to
/// construct a [ThemeData] with the same color palette
/// by using the [ColorScheme.fromSeed] constructor:
///
///  ```dart
/// ThemeData(
///   colorScheme: ColorScheme.fromSeed(seedColor: Colors.amber),
/// )
/// ```
///
/// The [ColorScheme.fromSeed] constructor
/// will generate a set of tonal palettes,
/// which are used to create the color scheme.
///
/// Alternatively you can use the [ColorScheme.fromSwatch] constructor:
///
/// ```dart
/// ThemeData(
///  colorScheme: ColorScheme.fromSwatch(primarySwatch: Colors.amber),
/// )
/// ```
///
/// The [ColorScheme.fromSwatch] constructor will
/// create the color scheme directly from the specific
/// color values used in the [MaterialColor].
///
///
/// See also:
///
///  * [Colors], which defines all of the standard material colors.
class MaterialColor(
	primary: Long,
	swatch: Map<Int, Color>,
) : ColorSwatch<Int>(primary, swatch) {
	/// Creates a color swatch with a variety of shades.
	///
	/// The `primary` argument should be the 32 bit ARGB value of one of the
	/// values in the swatch, as would be passed to the [Color.new] constructor
	/// for that same color, and as is exposed by [value]. (This is distinct from
	/// the specific index of the color in the swatch.)

	/// The lightest shade.
	val shade50 get() = this[50]!!

	/// The second lightest shade.
	val shade100 get() = this[100]!!

	/// The third lightest shade.
	val shade200 get() = this[200]!!

	/// The fourth lightest shade.
	val shade300 get() = this[300]!!

	/// The fifth lightest shade.
	val shade400 get() = this[400]!!

	/// The default shade.
	val shade500 get() = this[500]!!

	/// The fourth darkest shade.
	val shade600 get() = this[600]!!

	/// The third darkest shade.
	val shade700 get() = this[700]!!

	/// The second darkest shade.
	val shade800 get() = this[800]!!

	/// The darkest shade.
	val shade900 get() = this[900]!!
}

/// Defines a single accent color as well a swatch of four shades of the
/// accent color.
///
/// The color's shades are referred to by index, the colors with smaller
/// indices are lighter, larger indices are darker. There are four valid
/// indices: 100, 200, 400, and 700. The value of this color should be the
/// same as the value of index 200 and [shade200].
///
/// See also:
///
///  * [Colors], which defines all of the standard material colors.
///  * <https://material.io/go/design-theming#color-color-schemes>
class MaterialAccentColor(
	primary: Long,
	swatch: Map<Int, Color>,
) : ColorSwatch<Int>(primary, swatch) {
	/// Creates a color swatch with a variety of shades appropriate for accent
	/// colors.

	/// The lightest shade.
	val shade100 get() = this[100]!!

	/// The default shade.
	val shade200 get() = this[200]!!

	/// The second darkest shade.
	val shade400 get() = this[400]!!

	/// The darkest shade.
	val shade700 get() = this[700]!!
}

/// [Color] and [ColorSwatch] constants which represent Material design's
/// [color palette](https://material.io/design/color/).
///
/// Instead of using an absolute color from these palettes, consider using
/// [Theme.of] to obtain the local [ThemeData.colorScheme], which defines
/// the colors that most of the Material components use by default.
///
///
/// Most swatches have colors from 100 to 900 in increments of one hundred, plus
/// the color 50. The smaller the number, the more pale the color. The greater
/// the number, the darker the color. The accent swatches (e.g. [redAccent]) only
/// have the values 100, 200, 400, and 700.
///
/// In addition, a series of blacks and whites with common opacities are
/// available. For example, [black54] is a pure black with 54% opacity.
///
/// {@tool snippet}
///
/// To select a specific color from one of the swatches, index into the swatch
/// using an integer for the specific color desired, as follows:
///
/// ```dart
/// Color selection = Colors.green[400]!; // Selects a mid-range green.
/// ```
/// {@end-tool}
/// {@tool snippet}
///
/// Each [ColorSwatch] constant is a color and can used directly. For example:
///
/// ```dart
/// Container(
///   color: Colors.blue, // same as Colors.blue[500] or Colors.blue.shade500
/// )
/// ```
/// {@end-tool}
///
/// ## Color palettes
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.pink.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.pinkAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.red.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.redAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepOrange.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepOrangeAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.orange.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.orangeAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.amber.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.amberAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.yellow.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.yellowAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lime.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.limeAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightGreen.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightGreenAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.green.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.greenAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.teal.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.tealAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.cyan.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.cyanAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightBlue.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightBlueAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blue.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blueAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.indigo.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.indigoAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.purple.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.purpleAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepPurple.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepPurpleAccent.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blueGrey.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.brown.png)
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.grey.png)
///
/// ## Blacks and whites
///
/// These colors are identified by their transparency. The low transparency
/// levels (e.g. [Colors.white12] and [Colors.white10]) are very hard to see and
/// should be avoided in general. They are intended for very subtle effects.
///
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blacks.png)
/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.whites.png)
///
/// The [Colors.transparent] color isn't shown here because it is entirely
/// invisible!
///
/// See also:
///
///  * Cookbook: [Use themes to share colors and font styles](https://docs.flutter.dev/cookbook/design/themes)
object Colors {
	/// Completely invisible.
	val transparent = Color(0x00000000)

	/// Completely opaque black.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blacks.png)
	///
	/// See also:
	///
	///  * [black87], [black54], [black45], [black38], [black26], [black12], which
	///    are variants on this color but with different opacities.
	///  * [white], a solid white color.
	///  * [transparent], a fully-transparent color.
	val black = Color(0xFF000000)

	/// Black with 87% opacity.
	///
	/// This is a good contrasting color for text in light themes.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blacks.png)
	///
	/// See also:
	///
	///  * [Typography.black], which uses this color for its text styles.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	///  * [black], [black54], [black45], [black38], [black26], [black12], which
	///    are variants on this color but with different opacities.
	val black87 = Color(0xDD000000)

	/// Black with 54% opacity.
	///
	/// This is a color commonly used for headings in light themes. It's also used
	/// as the mask color behind dialogs.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blacks.png)
	///
	/// See also:
	///
	///  * [Typography.black], which uses this color for its text styles.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	///  * [black], [black87], [black45], [black38], [black26], [black12], which
	///    are variants on this color but with different opacities.
	val black54 = Color(0x8A000000)

	/// Black with 45% opacity.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blacks.png)
	///
	/// See also:
	///
	///  * [black], [black87], [black54], [black38], [black26], [black12], which
	///    are variants on this color but with different opacities.
	val black45 = Color(0x73000000)

	/// Black with 38% opacity.
	///
	/// For light themes, i.e. when the Theme's [ThemeData.brightness] is
	/// [Brightness.light], this color is used for disabled icons and for
	/// placeholder text in [DataTable].
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blacks.png)
	///
	/// See also:
	///
	///  * [black], [black87], [black54], [black45], [black26], [black12], which
	///    are variants on this color but with different opacities.
	val black38 = Color(0x61000000)

	/// Black with 26% opacity.
	///
	/// Used for disabled radio buttons and the text of disabled flat buttons in light themes.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blacks.png)
	///
	/// See also:
	///
	///  * [ThemeData.disabledColor], which uses this color by default in light themes.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	///  * [black], [black87], [black54], [black45], [black38], [black12], which
	///    are variants on this color but with different opacities.
	val black26 = Color(0x42000000)

	/// Black with 12% opacity.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blacks.png)
	///
	/// Used for the background of disabled raised buttons in light themes.
	///
	/// See also:
	///
	///  * [black], [black87], [black54], [black45], [black38], [black26], which
	///    are variants on this color but with different opacities.
	val black12 = Color(0x1F000000)

	/// Completely opaque white.
	///
	/// This is a good contrasting color for the [ThemeData.primaryColor] in the
	/// dark theme. See [ThemeData.brightness].
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.whites.png)
	///
	/// See also:
	///
	///  * [Typography.white], which uses this color for its text styles.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	///  * [white70], [white60], [white54], [white38], [white30], [white12],
	///    [white10], which are variants on this color but with different
	///    opacities.
	///  * [black], a solid black color.
	///  * [transparent], a fully-transparent color.
	val white = Color(0xFFFFFFFF)

	/// White with 70% opacity.
	///
	/// This is a color commonly used for headings in dark themes.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.whites.png)
	///
	/// See also:
	///
	///  * [Typography.white], which uses this color for its text styles.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	///  * [white], [white60], [white54], [white38], [white30], [white12],
	///    [white10], which are variants on this color but with different
	///    opacities.
	val white70 = Color(0xB3FFFFFF)

	/// White with 60% opacity.
	///
	/// Used for medium-emphasis text and hint text when [ThemeData.brightness] is
	/// set to [Brightness.dark].
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.whites.png)
	///
	/// See also:
	///
	///  * [ExpandIcon], which uses this color for dark themes.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	///  * [white], [white54], [white30], [white38], [white12], [white10], which
	///    are variants on this color but with different opacities.
	val white60 = Color(0x99FFFFFF)

	/// White with 54% opacity.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.whites.png)
	///
	/// See also:
	///
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	///  * [white], [white60], [white38], [white30], [white12], [white10], which
	///    are variants on this color but with different opacities.
	val white54 = Color(0x8AFFFFFF)

	/// White with 38% opacity.
	///
	/// Used for disabled radio buttons and the text of disabled flat buttons in dark themes.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.whites.png)
	///
	/// See also:
	///
	///  * [ThemeData.disabledColor], which uses this color by default in dark themes.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	///  * [white], [white60], [white54], [white70], [white30], [white12],
	///    [white10], which are variants on this color but with different
	///    opacities.
	val white38 = Color(0x62FFFFFF)

	/// White with 30% opacity.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.whites.png)
	///
	/// See also:
	///
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	///  * [white], [white60], [white54], [white70], [white38], [white12],
	///    [white10], which are variants on this color but with different
	///    opacities.
	val white30 = Color(0x4DFFFFFF)

	/// White with 24% opacity.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.whites.png)
	///
	/// Used for the splash color for filled buttons.
	///
	/// See also:
	///
	///  * [white], [white60], [white54], [white70], [white38], [white30],
	///    [white10], which are variants on this color
	///    but with different opacities.
	val white24 = Color(0x3DFFFFFF)

	/// White with 12% opacity.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.whites.png)
	///
	/// Used for the background of disabled raised buttons in dark themes.
	///
	/// See also:
	///
	///  * [white], [white60], [white54], [white70], [white38], [white30],
	///    [white10], which are variants on this color but with different
	///    opacities.
	val white12 = Color(0x1FFFFFFF)

	/// White with 10% opacity.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.whites.png)
	///
	/// See also:
	///
	///  * [white], [white60], [white54], [white70], [white38], [white30],
	///    [white12], which are variants on this color
	///    but with different opacities.
	///  * [transparent], a fully-transparent color, not far from this one.
	val white10 = Color(0x1AFFFFFF)

	/// The red primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.red.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.redAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepOrange.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepOrangeAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.pink.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.pinkAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.red[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [redAccent], the corresponding accent colors.
	///  * [deepOrange] and [pink], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val red = MaterialColor(RED_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFFFEBEE),
		100 to Color(0xFFFFCDD2),
		200 to Color(0xFFEF9A9A),
		300 to Color(0xFFE57373),
		400 to Color(0xFFEF5350),
		500 to Color(RED_PRIMARY_VALUE),
		600 to Color(0xFFE53935),
		700 to Color(0xFFD32F2F),
		800 to Color(0xFFC62828),
		900 to Color(0xFFB71C1C),
	))
	const val RED_PRIMARY_VALUE = 0xFFF44336

	/// The red accent swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.red.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.redAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepOrange.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepOrangeAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.pink.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.pinkAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.redAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [red], the corresponding primary colors.
	///  * [deepOrangeAccent] and [pinkAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val redAccent = MaterialAccentColor(RED_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFFFF8A80),
		200 to Color(RED_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFFFF1744),
		700 to Color(0xFFD50000),
	))
	const val RED_ACCENT_PRIMARY_VALUE = 0xFFFF5252

	/// The pink primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.pink.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.pinkAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.red.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.redAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.purple.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.purpleAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.pink[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [pinkAccent], the corresponding accent colors.
	///  * [red] and [purple], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val pink = MaterialColor(PINK_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFFCE4EC),
		100 to Color(0xFFF8BBD0),
		200 to Color(0xFFF48FB1),
		300 to Color(0xFFF06292),
		400 to Color(0xFFEC407A),
		500 to Color(PINK_PRIMARY_VALUE),
		600 to Color(0xFFD81B60),
		700 to Color(0xFFC2185B),
		800 to Color(0xFFAD1457),
		900 to Color(0xFF880E4F),
	))
	const val PINK_PRIMARY_VALUE = 0xFFE91E63

	/// The pink accent color swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.pink.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.pinkAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.red.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.redAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.purple.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.purpleAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.pinkAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [pink], the corresponding primary colors.
	///  * [redAccent] and [purpleAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val pinkAccent =
	MaterialAccentColor(PINK_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFFFF80AB),
		200 to Color(PINK_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFFF50057),
		700 to Color(0xFFC51162),
	))
	const val PINK_ACCENT_PRIMARY_VALUE = 0xFFFF4081

	/// The purple primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.purple.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.purpleAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepPurple.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepPurpleAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.pink.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.pinkAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.purple[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [purpleAccent], the corresponding accent colors.
	///  * [deepPurple] and [pink], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val purple = MaterialColor(PURPLE_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFF3E5F5),
		100 to Color(0xFFE1BEE7),
		200 to Color(0xFFCE93D8),
		300 to Color(0xFFBA68C8),
		400 to Color(0xFFAB47BC),
		500 to Color(PURPLE_PRIMARY_VALUE),
		600 to Color(0xFF8E24AA),
		700 to Color(0xFF7B1FA2),
		800 to Color(0xFF6A1B9A),
		900 to Color(0xFF4A148C),
	))
	const val PURPLE_PRIMARY_VALUE = 0xFF9C27B0

	/// The purple accent color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.purple.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.purpleAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepPurple.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepPurpleAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.pink.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.pinkAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.purpleAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [purple], the corresponding primary colors.
	///  * [deepPurpleAccent] and [pinkAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val purpleAccent =
	MaterialAccentColor(PURPLE_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFFEA80FC),
		200 to Color(PURPLE_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFFD500F9),
		700 to Color(0xFFAA00FF),
	))
	const val PURPLE_ACCENT_PRIMARY_VALUE = 0xFFE040FB

	/// The deep purple primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepPurple.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepPurpleAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.purple.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.purpleAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.indigo.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.indigoAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.deepPurple[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [deepPurpleAccent], the corresponding accent colors.
	///  * [purple] and [indigo], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val deepPurple = MaterialColor(DEEP_PURPLE_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFEDE7F6),
		100 to Color(0xFFD1C4E9),
		200 to Color(0xFFB39DDB),
		300 to Color(0xFF9575CD),
		400 to Color(0xFF7E57C2),
		500 to Color(DEEP_PURPLE_PRIMARY_VALUE),
		600 to Color(0xFF5E35B1),
		700 to Color(0xFF512DA8),
		800 to Color(0xFF4527A0),
		900 to Color(0xFF311B92),
	))
	const val DEEP_PURPLE_PRIMARY_VALUE = 0xFF673AB7

	/// The deep purple accent color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepPurple.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepPurpleAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.purple.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.purpleAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.indigo.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.indigoAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.deepPurpleAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [deepPurple], the corresponding primary colors.
	///  * [purpleAccent] and [indigoAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val deepPurpleAccent =
	MaterialAccentColor(DEEP_PURPLE_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFFB388FF),
		200 to Color(DEEP_PURPLE_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFF651FFF),
		700 to Color(0xFF6200EA),
	))
	const val DEEP_PURPLE_ACCENT_PRIMARY_VALUE = 0xFF7C4DFF

	/// The indigo primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.indigo.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.indigoAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blue.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blueAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepPurple.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepPurpleAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.indigo[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [indigoAccent], the corresponding accent colors.
	///  * [blue] and [deepPurple], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val indigo = MaterialColor(INDIGO_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFE8EAF6),
		100 to Color(0xFFC5CAE9),
		200 to Color(0xFF9FA8DA),
		300 to Color(0xFF7986CB),
		400 to Color(0xFF5C6BC0),
		500 to Color(INDIGO_PRIMARY_VALUE),
		600 to Color(0xFF3949AB),
		700 to Color(0xFF303F9F),
		800 to Color(0xFF283593),
		900 to Color(0xFF1A237E),
	))
	const val INDIGO_PRIMARY_VALUE = 0xFF3F51B5

	/// The indigo accent color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.indigo.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.indigoAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blue.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blueAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepPurple.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepPurpleAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.indigoAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [indigo], the corresponding primary colors.
	///  * [blueAccent] and [deepPurpleAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val indigoAccent =
	MaterialAccentColor(INDIGO_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFF8C9EFF),
		200 to Color(INDIGO_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFF3D5AFE),
		700 to Color(0xFF304FFE),
	))
	const val INDIGO_ACCENT_PRIMARY_VALUE = 0xFF536DFE

	/// The blue primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blue.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blueAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.indigo.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.indigoAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightBlue.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightBlueAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blueGrey.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.blue[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [blueAccent], the corresponding accent colors.
	///  * [indigo], [lightBlue], and [blueGrey], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val blue = MaterialColor(BLUE_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFE3F2FD),
		100 to Color(0xFFBBDEFB),
		200 to Color(0xFF90CAF9),
		300 to Color(0xFF64B5F6),
		400 to Color(0xFF42A5F5),
		500 to Color(BLUE_PRIMARY_VALUE),
		600 to Color(0xFF1E88E5),
		700 to Color(0xFF1976D2),
		800 to Color(0xFF1565C0),
		900 to Color(0xFF0D47A1),
	))
	const val BLUE_PRIMARY_VALUE = 0xFF2196F3

	/// The blue accent color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blue.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blueAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.indigo.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.indigoAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightBlue.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightBlueAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.blueAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [blue], the corresponding primary colors.
	///  * [indigoAccent] and [lightBlueAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val blueAccent =
	MaterialAccentColor(BLUE_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFF82B1FF),
		200 to Color(BLUE_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFF2979FF),
		700 to Color(0xFF2962FF),
	))
	const val BLUE_ACCENT_PRIMARY_VALUE = 0xFF448AFF

	/// The light blue primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightBlue.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightBlueAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blue.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blueAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.cyan.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.cyanAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.lightBlue[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [lightBlueAccent], the corresponding accent colors.
	///  * [blue] and [cyan], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val lightBlue = MaterialColor(LIGHT_BLUE_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFE1F5FE),
		100 to Color(0xFFB3E5FC),
		200 to Color(0xFF81D4FA),
		300 to Color(0xFF4FC3F7),
		400 to Color(0xFF29B6F6),
		500 to Color(LIGHT_BLUE_PRIMARY_VALUE),
		600 to Color(0xFF039BE5),
		700 to Color(0xFF0288D1),
		800 to Color(0xFF0277BD),
		900 to Color(0xFF01579B),
	))
	const val LIGHT_BLUE_PRIMARY_VALUE = 0xFF03A9F4

	/// The light blue accent swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightBlue.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightBlueAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blue.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blueAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.cyan.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.cyanAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.lightBlueAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [lightBlue], the corresponding primary colors.
	///  * [blueAccent] and [cyanAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val lightBlueAccent =
	MaterialAccentColor(LIGHT_BLUE_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFF80D8FF),
		200 to Color(LIGHT_BLUE_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFF00B0FF),
		700 to Color(0xFF0091EA),
	))
	const val LIGHT_BLUE_ACCENT_PRIMARY_VALUE = 0xFF40C4FF

	/// The cyan primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.cyan.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.cyanAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightBlue.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightBlueAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.teal.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.tealAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blueGrey.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.cyan[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [cyanAccent], the corresponding accent colors.
	///  * [lightBlue], [teal], and [blueGrey], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val cyan = MaterialColor(CYAN_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFE0F7FA),
		100 to Color(0xFFB2EBF2),
		200 to Color(0xFF80DEEA),
		300 to Color(0xFF4DD0E1),
		400 to Color(0xFF26C6DA),
		500 to Color(CYAN_PRIMARY_VALUE),
		600 to Color(0xFF00ACC1),
		700 to Color(0xFF0097A7),
		800 to Color(0xFF00838F),
		900 to Color(0xFF006064),
	))
	const val CYAN_PRIMARY_VALUE = 0xFF00BCD4

	/// The cyan accent color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.cyan.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.cyanAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightBlue.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightBlueAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.teal.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.tealAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.cyanAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [cyan], the corresponding primary colors.
	///  * [lightBlueAccent] and [tealAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val cyanAccent =
	MaterialAccentColor(CYAN_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFF84FFFF),
		200 to Color(CYAN_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFF00E5FF),
		700 to Color(0xFF00B8D4),
	))
	const val CYAN_ACCENT_PRIMARY_VALUE = 0xFF18FFFF

	/// The teal primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.teal.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.tealAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.green.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.greenAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.cyan.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.cyanAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.teal[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [tealAccent], the corresponding accent colors.
	///  * [green] and [cyan], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val teal = MaterialColor(TEAL_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFE0F2F1),
		100 to Color(0xFFB2DFDB),
		200 to Color(0xFF80CBC4),
		300 to Color(0xFF4DB6AC),
		400 to Color(0xFF26A69A),
		500 to Color(TEAL_PRIMARY_VALUE),
		600 to Color(0xFF00897B),
		700 to Color(0xFF00796B),
		800 to Color(0xFF00695C),
		900 to Color(0xFF004D40),
	))
	const val TEAL_PRIMARY_VALUE = 0xFF009688

	/// The teal accent color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.teal.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.tealAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.green.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.greenAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.cyan.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.cyanAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.tealAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [teal], the corresponding primary colors.
	///  * [greenAccent] and [cyanAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val tealAccent =
	MaterialAccentColor(TEAL_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFFA7FFEB),
		200 to Color(TEAL_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFF1DE9B6),
		700 to Color(0xFF00BFA5),
	))
	const val TEAL_ACCENT_PRIMARY_VALUE = 0xFF64FFDA

	/// The green primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.green.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.greenAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.teal.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.tealAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightGreen.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightGreenAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lime.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.limeAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.green[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [greenAccent], the corresponding accent colors.
	///  * [teal], [lightGreen], and [lime], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val green = MaterialColor(GREEN_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFE8F5E9),
		100 to Color(0xFFC8E6C9),
		200 to Color(0xFFA5D6A7),
		300 to Color(0xFF81C784),
		400 to Color(0xFF66BB6A),
		500 to Color(GREEN_PRIMARY_VALUE),
		600 to Color(0xFF43A047),
		700 to Color(0xFF388E3C),
		800 to Color(0xFF2E7D32),
		900 to Color(0xFF1B5E20),
	))
	const val GREEN_PRIMARY_VALUE = 0xFF4CAF50

	/// The green accent color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.green.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.greenAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.teal.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.tealAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightGreen.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightGreenAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lime.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.limeAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.greenAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [green], the corresponding primary colors.
	///  * [tealAccent], [lightGreenAccent], and [limeAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val greenAccent =
	MaterialAccentColor(GREEN_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFFB9F6CA),
		200 to Color(GREEN_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFF00E676),
		700 to Color(0xFF00C853),
	))
	const val GREEN_ACCENT_PRIMARY_VALUE = 0xFF69F0AE

	/// The light green primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightGreen.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightGreenAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.green.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.greenAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lime.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.limeAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.lightGreen[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [lightGreenAccent], the corresponding accent colors.
	///  * [green] and [lime], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val lightGreen = MaterialColor(LIGHT_GREEN_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFF1F8E9),
		100 to Color(0xFFDCEDC8),
		200 to Color(0xFFC5E1A5),
		300 to Color(0xFFAED581),
		400 to Color(0xFF9CCC65),
		500 to Color(LIGHT_GREEN_PRIMARY_VALUE),
		600 to Color(0xFF7CB342),
		700 to Color(0xFF689F38),
		800 to Color(0xFF558B2F),
		900 to Color(0xFF33691E),
	))
	const val LIGHT_GREEN_PRIMARY_VALUE = 0xFF8BC34A

	/// The light green accent color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightGreen.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightGreenAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.green.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.greenAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lime.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.limeAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.lightGreenAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [lightGreen], the corresponding primary colors.
	///  * [greenAccent] and [limeAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val lightGreenAccent =
	MaterialAccentColor(LIGHT_GREEN_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFFCCFF90),
		200 to Color(LIGHT_GREEN_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFF76FF03),
		700 to Color(0xFF64DD17),
	))
	const val LIGHT_GREEN_ACCENT_PRIMARY_VALUE = 0xFFB2FF59

	/// The lime primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lime.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.limeAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightGreen.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightGreenAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.yellow.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.yellowAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.lime[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [limeAccent], the corresponding accent colors.
	///  * [lightGreen] and [yellow], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val lime = MaterialColor(LIME_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFF9FBE7),
		100 to Color(0xFFF0F4C3),
		200 to Color(0xFFE6EE9C),
		300 to Color(0xFFDCE775),
		400 to Color(0xFFD4E157),
		500 to Color(LIME_PRIMARY_VALUE),
		600 to Color(0xFFC0CA33),
		700 to Color(0xFFAFB42B),
		800 to Color(0xFF9E9D24),
		900 to Color(0xFF827717),
	))
	const val LIME_PRIMARY_VALUE = 0xFFCDDC39

	/// The lime accent primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lime.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.limeAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightGreen.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lightGreenAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.yellow.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.yellowAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.limeAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [lime], the corresponding primary colors.
	///  * [lightGreenAccent] and [yellowAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val limeAccent =
	MaterialAccentColor(LIME_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFFF4FF81),
		200 to Color(LIME_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFFC6FF00),
		700 to Color(0xFFAEEA00),
	))
	const val LIME_ACCENT_PRIMARY_VALUE = 0xFFEEFF41

	/// The yellow primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.yellow.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.yellowAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lime.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.limeAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.amber.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.amberAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.yellow[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [yellowAccent], the corresponding accent colors.
	///  * [lime] and [amber], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val yellow = MaterialColor(YELLOW_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFFFFDE7),
		100 to Color(0xFFFFF9C4),
		200 to Color(0xFFFFF59D),
		300 to Color(0xFFFFF176),
		400 to Color(0xFFFFEE58),
		500 to Color(YELLOW_PRIMARY_VALUE),
		600 to Color(0xFFFDD835),
		700 to Color(0xFFFBC02D),
		800 to Color(0xFFF9A825),
		900 to Color(0xFFF57F17),
	))
	const val YELLOW_PRIMARY_VALUE = 0xFFFFEB3B

	/// The yellow accent color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.yellow.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.yellowAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.lime.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.limeAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.amber.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.amberAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.yellowAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [yellow], the corresponding primary colors.
	///  * [limeAccent] and [amberAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val yellowAccent =
	MaterialAccentColor(YELLOW_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFFFFFF8D),
		200 to Color(YELLOW_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFFFFEA00),
		700 to Color(0xFFFFD600),
	))
	const val YELLOW_ACCENT_PRIMARY_VALUE = 0xFFFFFF00

	/// The amber primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.amber.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.amberAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.yellow.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.yellowAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.orange.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.orangeAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.amber[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [amberAccent], the corresponding accent colors.
	///  * [yellow] and [orange], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val amber = MaterialColor(AMBER_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFFFF8E1),
		100 to Color(0xFFFFECB3),
		200 to Color(0xFFFFE082),
		300 to Color(0xFFFFD54F),
		400 to Color(0xFFFFCA28),
		500 to Color(AMBER_PRIMARY_VALUE),
		600 to Color(0xFFFFB300),
		700 to Color(0xFFFFA000),
		800 to Color(0xFFFF8F00),
		900 to Color(0xFFFF6F00),
	))
	const val AMBER_PRIMARY_VALUE = 0xFFFFC107

	/// The amber accent color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.amber.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.amberAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.yellow.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.yellowAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.orange.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.orangeAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.amberAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [amber], the corresponding primary colors.
	///  * [yellowAccent] and [orangeAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val amberAccent =
	MaterialAccentColor(AMBER_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFFFFE57F),
		200 to Color(AMBER_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFFFFC400),
		700 to Color(0xFFFFAB00),
	))
	const val AMBER_ACCENT_PRIMARY_VALUE = 0xFFFFD740

	/// The orange primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.orange.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.orangeAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.amber.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.amberAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepOrange.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepOrangeAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.brown.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.orange[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [orangeAccent], the corresponding accent colors.
	///  * [amber], [deepOrange], and [brown], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val orange = MaterialColor(ORANGE_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFFFF3E0),
		100 to Color(0xFFFFE0B2),
		200 to Color(0xFFFFCC80),
		300 to Color(0xFFFFB74D),
		400 to Color(0xFFFFA726),
		500 to Color(ORANGE_PRIMARY_VALUE),
		600 to Color(0xFFFB8C00),
		700 to Color(0xFFF57C00),
		800 to Color(0xFFEF6C00),
		900 to Color(0xFFE65100),
	))
	const val ORANGE_PRIMARY_VALUE = 0xFFFF9800

	/// The orange accent color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.orange.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.orangeAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.amber.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.amberAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepOrange.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepOrangeAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.orangeAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [orange], the corresponding primary colors.
	///  * [amberAccent] and [deepOrangeAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val orangeAccent =
	MaterialAccentColor(ORANGE_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFFFFD180),
		200 to Color(ORANGE_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFFFF9100),
		700 to Color(0xFFFF6D00),
	))
	const val ORANGE_ACCENT_PRIMARY_VALUE = 0xFFFFAB40

	/// The deep orange primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepOrange.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepOrangeAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.orange.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.orangeAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.red.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.redAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.brown.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.deepOrange[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [deepOrangeAccent], the corresponding accent colors.
	///  * [orange], [red], and [brown], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val deepOrange = MaterialColor(DEEP_ORANGE_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFFBE9E7),
		100 to Color(0xFFFFCCBC),
		200 to Color(0xFFFFAB91),
		300 to Color(0xFFFF8A65),
		400 to Color(0xFFFF7043),
		500 to Color(DEEP_ORANGE_PRIMARY_VALUE),
		600 to Color(0xFFF4511E),
		700 to Color(0xFFE64A19),
		800 to Color(0xFFD84315),
		900 to Color(0xFFBF360C),
	))
	const val DEEP_ORANGE_PRIMARY_VALUE = 0xFFFF5722

	/// The deep orange accent color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepOrange.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.deepOrangeAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.orange.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.orangeAccent.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.red.png)
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.redAccent.png)
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.deepOrangeAccent[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [deepOrange], the corresponding primary colors.
	///  * [orangeAccent] [redAccent], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val deepOrangeAccent =
	MaterialAccentColor(DEEP_ORANGE_ACCENT_PRIMARY_VALUE, mapOf(
		100 to Color(0xFFFF9E80),
		200 to Color(DEEP_ORANGE_ACCENT_PRIMARY_VALUE),
		400 to Color(0xFFFF3D00),
		700 to Color(0xFFDD2C00),
	))
	const val DEEP_ORANGE_ACCENT_PRIMARY_VALUE = 0xFFFF6E40

	/// The brown primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.brown.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.orange.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blueGrey.png)
	///
	/// This swatch has no corresponding accent color and swatch.
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.brown[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [orange] and [blueGrey], vaguely similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val brown = MaterialColor(BROWN_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFEFEBE9),
		100 to Color(0xFFD7CCC8),
		200 to Color(0xFFBCAAA4),
		300 to Color(0xFFA1887F),
		400 to Color(0xFF8D6E63),
		500 to Color(BROWN_PRIMARY_VALUE),
		600 to Color(0xFF6D4C41),
		700 to Color(0xFF5D4037),
		800 to Color(0xFF4E342E),
		900 to Color(0xFF3E2723),
	))
	const val BROWN_PRIMARY_VALUE = 0xFF795548

	/// The grey primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.grey.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blueGrey.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.brown.png)
	///
	/// This swatch has no corresponding accent swatch.
	///
	/// This swatch, in addition to the values 50 and 100 to 900 in 100
	/// increments, also features the special values 350 and 850. The 350 value is
	/// used for raised button while pressed in light themes, and 850 is used for
	/// the background color of the dark theme. See [ThemeData.brightness].
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.grey[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [blueGrey] and [brown], somewhat similar colors.
	///  * [black], [black87], [black54], [black45], [black38], [black26], [black12], which
	///    provide a different approach to showing shades of grey.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val grey = MaterialColor(GREY_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFFAFAFA),
		100 to Color(0xFFF5F5F5),
		200 to Color(0xFFEEEEEE),
		300 to Color(0xFFE0E0E0),
		350 to Color(0xFFD6D6D6), // only for raised button while pressed in light theme
		400 to Color(0xFFBDBDBD),
		500 to Color(GREY_PRIMARY_VALUE),
		600 to Color(0xFF757575),
		700 to Color(0xFF616161),
		800 to Color(0xFF424242),
		850 to Color(0xFF303030), // only for background color in dark theme
		900 to Color(0xFF212121),
	))
	const val GREY_PRIMARY_VALUE = 0xFF9E9E9E

	/// The blue-grey primary color and swatch.
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blueGrey.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.grey.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.cyan.png)
	///
	/// ![](https://flutter.github.io/assets-for-api-docs/assets/material/Colors.blue.png)
	///
	/// This swatch has no corresponding accent swatch.
	///
	/// {@tool snippet}
	///
	/// ```dart
	/// Icon(
	///   Icons.widgets,
	///   color: Colors.blueGrey[400],
	/// )
	/// ```
	/// {@end-tool}
	///
	/// See also:
	///
	///  * [grey], [cyan], and [blue], similar colors.
	///  * [Theme.of], which allows you to select colors from the current theme
	///    rather than hard-coding colors in your build methods.
	val blueGrey = MaterialColor(BLUE_GREY_PRIMARY_VALUE, mapOf(
		50 to Color(0xFFECEFF1),
		100 to Color(0xFFCFD8DC),
		200 to Color(0xFFB0BEC5),
		300 to Color(0xFF90A4AE),
		400 to Color(0xFF78909C),
		500 to Color(BLUE_GREY_PRIMARY_VALUE),
		600 to Color(0xFF546E7A),
		700 to Color(0xFF455A64),
		800 to Color(0xFF37474F),
		900 to Color(0xFF263238),
	))
	const val BLUE_GREY_PRIMARY_VALUE = 0xFF607D8B

	/// The Material Design primary color swatches, excluding grey.
	val primaries = listOf(
		red,
		pink,
		purple,
		deepPurple,
		indigo,
		blue,
		lightBlue,
		cyan,
		teal,
		green,
		lightGreen,
		lime,
		yellow,
		amber,
		orange,
		deepOrange,
		brown,
		// The grey swatch is intentionally omitted because when picking a color
		// randomly from this list to colorize an application, picking grey suddenly
		// makes the app look disabled.
		blueGrey,
	)

	/// The Material Design accent color swatches.
	val accents = listOf(
		redAccent,
		pinkAccent,
		purpleAccent,
		deepPurpleAccent,
		indigoAccent,
		blueAccent,
		lightBlueAccent,
		cyanAccent,
		tealAccent,
		greenAccent,
		lightGreenAccent,
		limeAccent,
		yellowAccent,
		amberAccent,
		orangeAccent,
		deepOrangeAccent,
	)
}
