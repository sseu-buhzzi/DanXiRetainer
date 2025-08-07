package com.buhzzi.danxiretainer.util

import android.content.Context
import java.nio.file.Path
import kotlin.io.path.div

val Context.filesDirPath: Path
	get() = filesDir.toPath()

val Context.usersDirPath
	get() = filesDirPath / "users"

val Context.settingsDirPath
	get() = filesDirPath / "settings"

fun Context.backgroundImagePathOf(userId: Long?) = run {
	userId?.let { settingsDirPathOf(it) }
		?: settingsDirPath
} / "background.png"

fun Context.userDirPathOf(userId: Long) =
	usersDirPath / userId.toString()

fun Context.settingsDirPathOf(userId: Long) =
	userDirPathOf(userId) / "settings"

fun Context.holesDirPathOf(userId: Long) =
	userDirPathOf(userId) / "holes"

fun Context.holesIndicesPathOf(userId: Long) =
	holesDirPathOf(userId) / "indices.bin"

fun Context.holePathOf(userId: Long, holeId: Long) =
	holesDirPathOf(userId) / "$holeId.json"

fun Context.floorsDirPathOf(userId: Long) =
	userDirPathOf(userId) / "floors"

fun Context.floorsIndicesPathOf(userId: Long) =
	floorsDirPathOf(userId) / "indices.bin"

fun Context.floorPathOf(userId: Long, floorId: Long) =
	floorsDirPathOf(userId) / "$floorId.json"

fun Context.tagsDirPathOf(userId: Long) =
	userDirPathOf(userId) / "tags"

fun Context.tagPathOf(userId: Long, tagId: Long) =
	tagsDirPathOf(userId) / "$tagId.json"

fun Context.sessionStateDirPathOf(userId: Long) =
	userDirPathOf(userId) / "session_state"

fun Context.sessionStateCurrentPathOf(userId: Long) =
	sessionStateDirPathOf(userId) / "current.json"

fun Context.sessionStateFilterPathOf(userId: Long) =
	sessionStateDirPathOf(userId) / "current-filter.txt"

fun Context.holesSessionStatesDirPathOf(userId: Long) =
	sessionStateDirPathOf(userId) / "holes"

fun Context.holeSessionStatePathOf(userId: Long, holeId: Long) =
	holesSessionStatesDirPathOf(userId) / "$holeId.json"

fun Context.holeSessionStateFilterPathOf(userId: Long, holeId: Long) =
	holesSessionStatesDirPathOf(userId) / "$holeId-filter.txt"
