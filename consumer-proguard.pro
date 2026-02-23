# consumer-proguard.pro
# mParticle Mixpanel Kit ProGuard rules

# Keep the Kit class
-keep class com.mparticle.kits.MixpanelKit { *; }

# Keep the UserIdentificationType enum
-keep class com.mparticle.kits.UserIdentificationType { *; }

# Keep Session Replay configuration class
-keep class com.mparticle.kits.SessionReplayConfiguration { *; }

# Mixpanel Session Replay SDK (optional dependency)
-keep class com.mixpanel.sessionreplay.** { *; }
-dontwarn com.mixpanel.sessionreplay.**
