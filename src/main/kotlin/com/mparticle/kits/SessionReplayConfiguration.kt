package com.mparticle.kits

/**
 * Configuration for Mixpanel Session Replay integration.
 *
 * All masking options default to true for privacy-first behavior.
 * Session Replay is disabled by default and must be explicitly enabled.
 */
data class SessionReplayConfiguration(
    /** Master switch for Session Replay. Must be true to enable recording. */
    val enabled: Boolean = false,

    /** Sampling rate for recording sessions (0.0 - 100.0). */
    val recordSessionsPercent: Double = 100.0,

    /** Whether to automatically start recording when initialized. */
    val autoStartRecording: Boolean = true,

    /** Whether to only upload recordings on WiFi. */
    val wifiOnly: Boolean = true,

    /** Whether to mask ImageView content. */
    val maskImages: Boolean = true,

    /** Whether to mask text content. */
    val maskText: Boolean = true,

    /** Whether to mask WebView content. */
    val maskWebViews: Boolean = true,

    /** Whether to enable Session Replay debug logging. */
    val enableLogging: Boolean = false,

    /** Flush interval in seconds for uploading recordings. */
    val flushIntervalSeconds: Int = 10
) {
    companion object {
        /**
         * Parse Session Replay configuration from mParticle settings.
         *
         * @param settings Map of configuration key-value pairs from mParticle dashboard
         * @return SessionReplayConfiguration with parsed values or defaults
         */
        fun fromSettings(settings: Map<String, String>?): SessionReplayConfiguration {
            if (settings == null) return SessionReplayConfiguration()

            return SessionReplayConfiguration(
                enabled = settings[KEY_SESSION_REPLAY_ENABLED]?.lowercase() == "true",
                recordSessionsPercent = settings[KEY_RECORD_SESSIONS_PERCENT]
                    ?.toDoubleOrNull()
                    ?.coerceIn(0.0, 100.0)
                    ?: 100.0,
                autoStartRecording = settings[KEY_AUTO_START_RECORDING]?.lowercase() != "false",
                wifiOnly = settings[KEY_WIFI_ONLY]?.lowercase() != "false",
                maskImages = settings[KEY_MASK_IMAGES]?.lowercase() != "false",
                maskText = settings[KEY_MASK_TEXT]?.lowercase() != "false",
                maskWebViews = settings[KEY_MASK_WEB_VIEWS]?.lowercase() != "false",
                enableLogging = settings[KEY_ENABLE_SESSION_REPLAY_LOGGING]?.lowercase() == "true",
                flushIntervalSeconds = settings[KEY_SESSION_REPLAY_FLUSH_INTERVAL]
                    ?.toIntOrNull()
                    ?.coerceIn(1, 3600)
                    ?: 10
            )
        }
    }
}
