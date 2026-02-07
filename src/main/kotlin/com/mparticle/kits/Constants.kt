package com.mparticle.kits

// Kit identification
internal const val NAME = "Mixpanel"

// Configuration keys (from mParticle dashboard settings)
internal const val KEY_TOKEN = "token"
internal const val KEY_BASE_URL = "baseUrl"
internal const val KEY_USER_ID_TYPE = "userIdentificationType"
internal const val KEY_USE_PEOPLE = "useMixpanelPeople"

// Session Replay configuration keys
internal const val KEY_SESSION_REPLAY_ENABLED = "sessionReplayEnabled"
internal const val KEY_RECORD_SESSIONS_PERCENT = "recordSessionsPercent"
internal const val KEY_AUTO_START_RECORDING = "autoStartRecording"
internal const val KEY_WIFI_ONLY = "wifiOnly"
internal const val KEY_MASK_IMAGES = "maskImages"
internal const val KEY_MASK_TEXT = "maskText"
internal const val KEY_MASK_WEB_VIEWS = "maskWebViews"
internal const val KEY_ENABLE_SESSION_REPLAY_LOGGING = "enableSessionReplayLogging"
internal const val KEY_SESSION_REPLAY_FLUSH_INTERVAL = "sessionReplayFlushInterval"

// Event property keys
internal const val EVENT_TYPE_PROPERTY = "event_type"

// Logging tag
internal const val LOG_TAG = "MixpanelKit"
