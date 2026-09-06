package com.example.media

import android.content.Context
import android.content.res.AssetFileDescriptor
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Centralized registry for game media files provided by the user.
 * 
 * Strict constraints:
 * - Direct playback without alteration, re-encoding, or modification.
 * - Under no condition will placeholder or AI-generated media be created.
 * - Missing assets are explicitly reported by path rather than silently replaced.
 */
object GameMediaAssets {

    /**
     * Relative path to the opening video in assets.
     */
    const val INTRO_VIDEO_PATH = "videos/intro.mp4"

    /**
     * Fallback relative path for intro if placed directly in root assets.
     */
    const val INTRO_VIDEO_ROOT_PATH = "intro.mp4"

    /**
     * Relative path to the login screen MP3 sound effect in assets.
     */
    const val LOGIN_AUDIO_PATH = "audio/effect.mp3"

    /**
     * Fallback relative path for login effect if placed directly in root assets.
     */
    const val LOGIN_AUDIO_ROOT_PATH = "effect.mp3"

    /**
     * Check if a given asset path is present in the application assets.
     */
    fun isAssetAvailable(context: Context, relativePath: String): Boolean {
        return try {
            val stream: InputStream = context.assets.open(relativePath)
            stream.close()
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Checks if intro video is available in assets or raw resources.
     */
    fun isIntroVideoAvailable(context: Context): Boolean {
        if (isAssetAvailable(context, INTRO_VIDEO_PATH)) return true
        if (isAssetAvailable(context, INTRO_VIDEO_ROOT_PATH)) return true
        val rawResId = context.resources.getIdentifier("intro", "raw", context.packageName)
        return rawResId != 0
    }

    /**
     * Checks if login sound effect is available in assets or raw resources.
     */
    fun isLoginAudioAvailable(context: Context): Boolean {
        if (isAssetAvailable(context, LOGIN_AUDIO_PATH)) return true
        if (isAssetAvailable(context, LOGIN_AUDIO_ROOT_PATH)) return true
        val rawResId = context.resources.getIdentifier("effect", "raw", context.packageName)
        return rawResId != 0
    }

    /**
     * Retrieves the AssetFileDescriptor for the intro video if present.
     */
    fun openIntroAssetFd(context: Context): AssetFileDescriptor? {
        return try {
            if (isAssetAvailable(context, INTRO_VIDEO_PATH)) {
                context.assets.openFd(INTRO_VIDEO_PATH)
            } else if (isAssetAvailable(context, INTRO_VIDEO_ROOT_PATH)) {
                context.assets.openFd(INTRO_VIDEO_ROOT_PATH)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Extracts an asset to cache file for video players requiring a file path or URI.
     */
    fun getOrExtractAssetToCache(context: Context, relativePath: String, outputFileName: String): File? {
        return try {
            val cacheFile = File(context.cacheDir, outputFileName)
            if (!isAssetAvailable(context, relativePath)) {
                if (cacheFile.exists() && cacheFile.length() > 0) return cacheFile
                return null
            }
            // Always ensure latest non-empty file
            context.assets.open(relativePath).use { input ->
                val availableBytes = input.available()
                if (cacheFile.exists() && cacheFile.length() > 0 && (availableBytes <= 0 || cacheFile.length() == availableBytes.toLong())) {
                    return cacheFile
                }
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (cacheFile.exists() && cacheFile.length() > 0) cacheFile else null
        } catch (_: Exception) {
            null
        }
    }
}
