# Project Requirements: mparticle-android-integration-mixpanel

## Executive Summary

This project implements an mParticle Android Kit for [Mixpanel](https://mixpanel.com), enabling Android applications using the mParticle SDK to seamlessly forward analytics data to Mixpanel. The kit wraps the `mixpanel-android` SDK and maps mParticle events, identity, and user attributes to their Mixpanel equivalents.

### Goals
1. Provide feature parity with the existing iOS Mixpanel Kit (`mparticle-apple-integration-mixpanel`)
2. Follow mParticle Kit development best practices
3. Enable configuration via the mParticle dashboard
4. Support all major Mixpanel analytics features

### Scope
- Event tracking (custom events, screen views)
- Commerce events (purchase tracking with revenue)
- Identity management (login, logout, identify, modify)
- User attributes (People API and Super Properties)
- Opt-in/opt-out tracking

---

## Technical Architecture Overview

### Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Android Application                       │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐     ┌──────────────────────────────────┐  │
│  │   App Code      │────▶│       mParticle SDK              │  │
│  │                 │     │  (android-core)                  │  │
│  └─────────────────┘     └──────────────┬───────────────────┘  │
│                                         │                       │
│                                         ▼                       │
│                          ┌──────────────────────────────────┐  │
│                          │   MixpanelKit                    │  │
│                          │   (mparticle-android-integration │  │
│                          │    -mixpanel)                    │  │
│                          └──────────────┬───────────────────┘  │
│                                         │                       │
│                                         ▼                       │
│                          ┌──────────────────────────────────┐  │
│                          │   Mixpanel SDK                   │  │
│                          │   (mixpanel-android)             │  │
│                          └──────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                         │
                                         ▼
                          ┌──────────────────────────────────┐
                          │   Mixpanel Servers               │
                          │   api.mixpanel.com               │
                          └──────────────────────────────────┘
```

### Project Structure

```
mparticle-android-integration-mixpanel/
├── build.gradle.kts              # Root build configuration
├── settings.gradle.kts           # Gradle settings
├── gradle.properties             # Gradle properties
├── src/
│   ├── main/
│   │   ├── kotlin/com/mparticle/kits/
│   │   │   ├── MixpanelKit.kt            # Main Kit implementation
│   │   │   └── UserIdentificationType.kt # User ID type enum
│   │   └── AndroidManifest.xml
│   └── test/
│       └── kotlin/com/mparticle/kits/
│           ├── MixpanelKitTest.kt        # Unit tests
│           ├── EventForwardingTest.kt
│           ├── CommerceTest.kt
│           ├── IdentityTest.kt
│           └── UserAttributeTest.kt
├── consumer-proguard.pro         # ProGuard rules for consumers
├── README.md                     # Setup documentation
├── LICENSE                       # Apache 2.0
├── ARCHITECTURE.md               # Architecture documentation
└── PROJECT_REQUIREMENTS.md       # This file
```

### Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| `com.mparticle:android-core` | 5.+ | mParticle SDK and KitIntegration base |
| `com.mixpanel.android:mixpanel-android` | 7.+ | Mixpanel Android SDK |
| Kotlin stdlib | 1.9+ | Kotlin language support |
| JUnit 4 | 4.13+ | Unit testing |
| Mockito | 5.+ | Mocking for tests |

---

## Feature Requirements

### 1. Kit Initialization

**Requirement**: Initialize Mixpanel SDK with configuration from mParticle dashboard.

| Config Key | Type | Required | Default | Description |
|------------|------|----------|---------|-------------|
| `token` | String | Yes | - | Mixpanel project token |
| `serverURL` | String | No | null | Custom Mixpanel API endpoint |
| `userIdentificationType` | String | No | "CustomerId" | Identity type for user ID |
| `useMixpanelPeople` | String | No | "True" | Use People API for attributes |

**Implementation**:
```kotlin
override fun onKitCreate(
    settings: MutableMap<String, String>?,
    context: Context?
): MutableList<ReportingMessage>? {
    val token = settings?.get(KEY_TOKEN)
        ?: throw IllegalArgumentException("Mixpanel token is required")

    // Initialize Mixpanel
    mixpanelInstance = MixpanelAPI.getInstance(context, token, false)

    // Configure optional settings
    settings[KEY_SERVER_URL]?.let { url ->
        mixpanelInstance?.setServerURL(url)
    }

    return null
}
```

### 2. Event Tracking

**Requirement**: Forward mParticle events to Mixpanel tracking.

| mParticle API | Mixpanel API | Notes |
|---------------|--------------|-------|
| `logEvent(MPEvent)` | `track(eventName, properties)` | Direct mapping |
| `logScreen(name, attrs)` | `track("Viewed {name}", properties)` | Prefix with "Viewed " |
| `logError(msg, attrs)` | `track("Error", properties)` | Include error details |
| `logException(ex, attrs, msg)` | `track("Exception", properties)` | Include exception info |
| `leaveBreadcrumb(text)` | `track("Breadcrumb", {text: ...})` | Simple event |

**Interface**: `KitIntegration.EventListener`

### 3. Commerce Events

**Requirement**: Forward commerce events with special handling for purchases.

| mParticle Action | Mixpanel API | Notes |
|------------------|--------------|-------|
| `PURCHASE` | `people.trackCharge(revenue, props)` | Revenue tracking |
| Other actions | Expand to regular events | Add to cart, etc. |

**Interface**: `KitIntegration.CommerceListener`

**Implementation**:
```kotlin
override fun logEvent(event: CommerceEvent): List<ReportingMessage>? {
    if (event.productAction == Product.PURCHASE) {
        if (useMixpanelPeople) {
            val revenue = event.transactionAttributes?.revenue ?: 0.0
            mixpanel.people.trackCharge(revenue, convertAttributes(event.customAttributes))
        }
        return listOf(ReportingMessage.fromEvent(this, event))
    } else {
        // Expand to regular events
        return CommerceEventUtils.expand(event)?.mapNotNull { logEvent(it.event) }?.flatten()
    }
}
```

### 4. Identity Management

**Requirement**: Sync user identity between mParticle and Mixpanel.

| mParticle Event | Mixpanel API | Notes |
|-----------------|--------------|-------|
| Identify complete | `identify(userId)` | Set distinct ID |
| Login complete | `identify(userId)` | Set distinct ID |
| Logout complete | `reset()` | Clear identity |
| Modify complete | `identify(userId)` | Update distinct ID |

**Interface**: `KitIntegration.IdentityListener`

**User ID Extraction**:
```kotlin
private fun extractUserId(user: MParticleUser?): String? {
    val identities = user?.userIdentities ?: return null
    return when (userIdentificationType) {
        UserIdentificationType.CUSTOMER_ID ->
            identities[MParticle.IdentityType.CustomerId]
        UserIdentificationType.MPID ->
            user.id.toString()
        UserIdentificationType.OTHER ->
            identities[MParticle.IdentityType.Other]
        UserIdentificationType.OTHER_2 ->
            identities[MParticle.IdentityType.Other2]
        UserIdentificationType.OTHER_3 ->
            identities[MParticle.IdentityType.Other3]
        UserIdentificationType.OTHER_4 ->
            identities[MParticle.IdentityType.Other4]
        else -> null
    }
}
```

### 5. User Attributes

**Requirement**: Sync user attributes with Mixpanel.

| mParticle Operation | Mixpanel (People Mode) | Mixpanel (Super Props Mode) |
|---------------------|------------------------|----------------------------|
| Set attribute | `people.set(key, value)` | `registerSuperProperties({key: value})` |
| Remove attribute | `people.unset([key])` | `unregisterSuperProperty(key)` |
| Increment attribute | `people.increment(key, value)` | N/A (return success anyway) |
| Set attribute list | `people.set(key, list)` | `registerSuperProperties({key: list})` |
| Set all attributes | `people.set(allProps)` | `registerSuperProperties(allProps)` |

**Interface**: `KitIntegration.UserAttributeListener`

### 6. Opt-Out Tracking

**Requirement**: Honor user opt-out preferences.

| mParticle | Mixpanel |
|-----------|----------|
| `setOptOut(true)` | `optOutTracking()` |
| `setOptOut(false)` | `optInTracking()` |

---

## API Mapping: iOS Kit → Android Kit

This table maps the iOS Mixpanel Kit implementation to Android equivalents:

| iOS Method | Android Method | Notes |
|------------|----------------|-------|
| `didFinishLaunching(withConfiguration:)` | `onKitCreate(settings, context)` | |
| `logBaseEvent(_:)` | `logBaseEvent(baseEvent)` | |
| `logEvent(_:)` | `logEvent(event: MPEvent)` | EventListener |
| `logScreen(_:)` | `logScreen(name, attrs)` | EventListener |
| `logCommerceEvent(_:)` | `logEvent(event: CommerceEvent)` | CommerceListener |
| `onIdentifyComplete(_:request:)` | `onIdentifyCompleted(user, request)` | IdentityListener |
| `onLoginComplete(_:request:)` | `onLoginCompleted(user, request)` | IdentityListener |
| `onLogoutComplete(_:request:)` | `onLogoutCompleted(user, request)` | IdentityListener |
| `onModifyComplete(_:request:)` | `onModifyCompleted(user, request)` | IdentityListener |
| `onSetUserAttribute(_:)` | `onSetUserAttribute(key, value, user)` | UserAttributeListener |
| `onRemoveUserAttribute(_:)` | `onRemoveUserAttribute(key, user)` | UserAttributeListener |
| `incrementUserAttribute(_:byValue:)` | `onIncrementUserAttribute(key, value, newValue, user)` | UserAttributeListener |
| `setUserAttribute(_:values:)` | `onSetUserAttributeList(key, values, user)` | UserAttributeListener |
| `setOptOut(_:)` | `setOptOut(optedOut)` | |
| `providerKitInstance` | `getInstance()` | Returns MixpanelAPI |

---

## Implementation Phases

### Phase 1: Project Setup & Core Structure
**Goal**: Create project skeleton with basic Kit structure

Tasks:
- [ ] Initialize Gradle project with Kotlin DSL
- [ ] Add dependencies (mParticle, Mixpanel, testing)
- [ ] Create `MixpanelKit.kt` extending `KitIntegration`
- [ ] Create `UserIdentificationType.kt` enum
- [ ] Implement `getName()` returning "Mixpanel"
- [ ] Implement `onKitCreate()` with configuration parsing
- [ ] Implement `setOptOut()` with opt-in/opt-out
- [ ] Implement `getInstance()` returning MixpanelAPI
- [ ] Add basic unit tests for initialization

### Phase 2: Event Tracking
**Goal**: Implement EventListener interface

Tasks:
- [ ] Implement `logEvent(MPEvent)` → `track()`
- [ ] Implement `logScreen()` → `track("Viewed {name}")`
- [ ] Implement `logError()` → `track("Error", ...)`
- [ ] Implement `logException()` → `track("Exception", ...)`
- [ ] Implement `leaveBreadcrumb()` → `track("Breadcrumb", ...)`
- [ ] Add unit tests for event forwarding
- [ ] Handle event attributes conversion (Map → JSONObject)

### Phase 3: Commerce Events
**Goal**: Implement CommerceListener interface

Tasks:
- [ ] Implement `logEvent(CommerceEvent)` for purchases
- [ ] Call `people.trackCharge()` for purchase events
- [ ] Expand non-purchase events to regular events
- [ ] Implement `logLtvIncrease()` (optional, may return null)
- [ ] Add unit tests for commerce events

### Phase 4: Identity Management
**Goal**: Implement IdentityListener interface

Tasks:
- [ ] Implement `onIdentifyCompleted()` → `identify()`
- [ ] Implement `onLoginCompleted()` → `identify()`
- [ ] Implement `onLogoutCompleted()` → `reset()`
- [ ] Implement `onModifyCompleted()` → `identify()`
- [ ] Implement `onUserIdentified()` → `identify()`
- [ ] Create user ID extraction logic based on configuration
- [ ] Add unit tests for identity flows

### Phase 5: User Attributes
**Goal**: Implement UserAttributeListener interface

Tasks:
- [ ] Implement `onSetUserAttribute()` → `people.set()` or `registerSuperProperties()`
- [ ] Implement `onRemoveUserAttribute()` → `people.unset()` or `unregisterSuperProperty()`
- [ ] Implement `onIncrementUserAttribute()` → `people.increment()`
- [ ] Implement `onSetUserTag()` → `people.set(key, true)`
- [ ] Implement `onSetUserAttributeList()` → handle list values
- [ ] Implement `onSetAllUserAttributes()` → bulk sync
- [ ] Implement `supportsAttributeLists()` → return true
- [ ] Implement `onConsentStateUpdated()` (optional)
- [ ] Add unit tests for attribute operations

### Phase 6: Testing & Polish
**Goal**: Ensure quality and documentation

Tasks:
- [ ] Comprehensive unit test coverage (>80%)
- [ ] Integration tests with mocked Mixpanel
- [ ] ProGuard rules for consumer apps
- [ ] README.md with setup instructions
- [ ] Code review and cleanup
- [ ] Version and license information

---

## Testing Strategy

### Unit Testing

Test each interface implementation independently:

```kotlin
class MixpanelKitTest {
    private lateinit var kit: MixpanelKit
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        kit = MixpanelKit()
    }

    @Test
    fun `getName returns Mixpanel`() {
        assertEquals("Mixpanel", kit.name)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `onKitCreate throws when token is missing`() {
        kit.onKitCreate(emptyMap(), mockContext)
    }

    @Test
    fun `onKitCreate succeeds with valid token`() {
        val settings = mapOf("token" to "test-token")
        val result = kit.onKitCreate(settings, mockContext)
        // Assert kit is started
    }
}
```

### Test Categories

| Category | Coverage | Focus |
|----------|----------|-------|
| Initialization | Token validation, config parsing | onKitCreate, settings |
| Event Forwarding | All event types | logEvent, logScreen |
| Commerce | Purchase and non-purchase | logEvent(CommerceEvent) |
| Identity | All identity operations | login, logout, identify |
| User Attributes | Set, remove, increment | UserAttributeListener |
| Opt-Out | Both states | setOptOut |

---

## Success Criteria

### Functional Criteria
- [ ] All iOS Mixpanel Kit features are implemented in Android
- [ ] Configuration settings work identically to iOS
- [ ] Events appear in Mixpanel dashboard correctly
- [ ] User identity syncs properly
- [ ] User attributes appear in Mixpanel People

### Quality Criteria
- [ ] Unit test coverage >80%
- [ ] No crashes on edge cases (null data, missing settings)
- [ ] ProGuard-compatible
- [ ] Clean lint results
- [ ] Documentation complete

### Performance Criteria
- [ ] Initialization <100ms
- [ ] No main thread blocking
- [ ] Memory-efficient attribute conversion

---

## Appendix: Configuration Reference

### mParticle Dashboard Settings

| Setting Name | Setting Key | Type | Default | Description |
|--------------|-------------|------|---------|-------------|
| Project Token | `token` | Text | (required) | Mixpanel project token from dashboard |
| Server URL | `serverURL` | Text | - | Custom API endpoint for EU or proxy |
| User Identification | `userIdentificationType` | Dropdown | CustomerId | How to identify users |
| Use People API | `useMixpanelPeople` | Boolean | True | Store attributes in People vs Super Props |

### User Identification Types

| Value | Description |
|-------|-------------|
| `CustomerId` | mParticle Customer ID identity |
| `MPID` | mParticle ID (numeric) |
| `Other` | Custom identity type "Other" |
| `Other2` | Custom identity type "Other2" |
| `Other3` | Custom identity type "Other3" |
| `Other4` | Custom identity type "Other4" |

---

## Appendix: Reference Materials

### Primary References
- iOS Mixpanel Kit: `mparticle-kit-context/example-kits/mparticle-apple-integration-mixpanel/`
- Android Kit Example: `mparticle-kit-context/example-kits/mparticle-android-integration-example/`

### SDK Documentation
- [mParticle Android Kit Development](https://docs.mparticle.com/developers/guides/partners/kit-integrations/android-kit/)
- [Mixpanel Android SDK](https://developer.mixpanel.com/docs/android)

### Source Code References
- Mixpanel Android SDK: `mparticle-kit-context/sdk-source-references/mixpanel-android/`
- mParticle Android SDK: `mparticle-kit-context/sdk-source-references/mparticle-android-sdk/`
