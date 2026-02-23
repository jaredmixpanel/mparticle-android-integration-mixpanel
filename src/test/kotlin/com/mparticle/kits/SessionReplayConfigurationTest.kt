package com.mparticle.kits

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionReplayConfigurationTest {

    @Test
    fun `default configuration has Session Replay disabled`() {
        val config = SessionReplayConfiguration()

        assertFalse(config.enabled)
        assertEquals(100.0, config.recordSessionsPercent, 0.01)
        assertTrue(config.autoStartRecording)
        assertTrue(config.wifiOnly)
        assertTrue(config.maskImages)
        assertTrue(config.maskText)
        assertTrue(config.maskWebViews)
        assertFalse(config.enableLogging)
        assertEquals(10, config.flushIntervalSeconds)
    }

    @Test
    fun `fromSettings returns defaults when settings is null`() {
        val config = SessionReplayConfiguration.fromSettings(null)

        assertFalse(config.enabled)
        assertEquals(100.0, config.recordSessionsPercent, 0.01)
        assertTrue(config.autoStartRecording)
    }

    @Test
    fun `fromSettings returns defaults when settings is empty`() {
        val config = SessionReplayConfiguration.fromSettings(emptyMap())

        assertFalse(config.enabled)
        assertEquals(100.0, config.recordSessionsPercent, 0.01)
        assertTrue(config.autoStartRecording)
    }

    @Test
    fun `fromSettings parses enabled correctly`() {
        val enabledConfig = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_SESSION_REPLAY_ENABLED to "true")
        )
        assertTrue(enabledConfig.enabled)

        val disabledConfig = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_SESSION_REPLAY_ENABLED to "false")
        )
        assertFalse(disabledConfig.enabled)

        val caseInsensitiveConfig = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_SESSION_REPLAY_ENABLED to "TRUE")
        )
        assertTrue(caseInsensitiveConfig.enabled)
    }

    @Test
    fun `fromSettings parses recordSessionsPercent correctly`() {
        val config = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_RECORD_SESSIONS_PERCENT to "50.5")
        )
        assertEquals(50.5, config.recordSessionsPercent, 0.01)
    }

    @Test
    fun `fromSettings clamps recordSessionsPercent to valid range`() {
        val tooLowConfig = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_RECORD_SESSIONS_PERCENT to "-10.0")
        )
        assertEquals(0.0, tooLowConfig.recordSessionsPercent, 0.01)

        val tooHighConfig = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_RECORD_SESSIONS_PERCENT to "150.0")
        )
        assertEquals(100.0, tooHighConfig.recordSessionsPercent, 0.01)
    }

    @Test
    fun `fromSettings handles invalid recordSessionsPercent`() {
        val config = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_RECORD_SESSIONS_PERCENT to "invalid")
        )
        assertEquals(100.0, config.recordSessionsPercent, 0.01)
    }

    @Test
    fun `fromSettings parses autoStartRecording correctly`() {
        val disabledConfig = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_AUTO_START_RECORDING to "false")
        )
        assertFalse(disabledConfig.autoStartRecording)

        val enabledConfig = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_AUTO_START_RECORDING to "true")
        )
        assertTrue(enabledConfig.autoStartRecording)

        // Default to true for any non-"false" value
        val otherConfig = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_AUTO_START_RECORDING to "yes")
        )
        assertTrue(otherConfig.autoStartRecording)
    }

    @Test
    fun `fromSettings parses wifiOnly correctly`() {
        val disabledConfig = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_WIFI_ONLY to "false")
        )
        assertFalse(disabledConfig.wifiOnly)

        val enabledConfig = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_WIFI_ONLY to "true")
        )
        assertTrue(enabledConfig.wifiOnly)
    }

    @Test
    fun `fromSettings parses masking options correctly`() {
        val config = SessionReplayConfiguration.fromSettings(
            mapOf(
                KEY_MASK_IMAGES to "false",
                KEY_MASK_TEXT to "false",
                KEY_MASK_WEB_VIEWS to "false"
            )
        )
        assertFalse(config.maskImages)
        assertFalse(config.maskText)
        assertFalse(config.maskWebViews)
    }

    @Test
    fun `fromSettings parses enableLogging correctly`() {
        val enabledConfig = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_ENABLE_SESSION_REPLAY_LOGGING to "true")
        )
        assertTrue(enabledConfig.enableLogging)

        val disabledConfig = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_ENABLE_SESSION_REPLAY_LOGGING to "false")
        )
        assertFalse(disabledConfig.enableLogging)
    }

    @Test
    fun `fromSettings parses flushIntervalSeconds correctly`() {
        val config = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_SESSION_REPLAY_FLUSH_INTERVAL to "30")
        )
        assertEquals(30, config.flushIntervalSeconds)
    }

    @Test
    fun `fromSettings clamps flushIntervalSeconds to valid range`() {
        val tooLowConfig = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_SESSION_REPLAY_FLUSH_INTERVAL to "0")
        )
        assertEquals(1, tooLowConfig.flushIntervalSeconds)

        val tooHighConfig = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_SESSION_REPLAY_FLUSH_INTERVAL to "5000")
        )
        assertEquals(3600, tooHighConfig.flushIntervalSeconds)
    }

    @Test
    fun `fromSettings handles invalid flushIntervalSeconds`() {
        val config = SessionReplayConfiguration.fromSettings(
            mapOf(KEY_SESSION_REPLAY_FLUSH_INTERVAL to "invalid")
        )
        assertEquals(10, config.flushIntervalSeconds)
    }

    @Test
    fun `fromSettings parses all options together`() {
        val config = SessionReplayConfiguration.fromSettings(
            mapOf(
                KEY_SESSION_REPLAY_ENABLED to "true",
                KEY_RECORD_SESSIONS_PERCENT to "75.0",
                KEY_AUTO_START_RECORDING to "false",
                KEY_WIFI_ONLY to "false",
                KEY_MASK_IMAGES to "false",
                KEY_MASK_TEXT to "true",
                KEY_MASK_WEB_VIEWS to "false",
                KEY_ENABLE_SESSION_REPLAY_LOGGING to "true",
                KEY_SESSION_REPLAY_FLUSH_INTERVAL to "20"
            )
        )

        assertTrue(config.enabled)
        assertEquals(75.0, config.recordSessionsPercent, 0.01)
        assertFalse(config.autoStartRecording)
        assertFalse(config.wifiOnly)
        assertFalse(config.maskImages)
        assertTrue(config.maskText)
        assertFalse(config.maskWebViews)
        assertTrue(config.enableLogging)
        assertEquals(20, config.flushIntervalSeconds)
    }
}
