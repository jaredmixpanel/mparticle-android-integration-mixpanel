# mParticle Kit Architecture

This document synthesizes understanding of mParticle Kit architecture for implementing `mparticle-android-integration-mixpanel`.

## What is an mParticle Kit?

### Conceptual Definition

An mParticle Kit is a **client-side bridge** between the mParticle SDK and a third-party analytics/marketing SDK. It allows mParticle to forward events, user data, and identity information directly to partner services from the mobile app, rather than routing through mParticle's servers.

### When to Use a Kit

Kits are used when:
1. A partner requires client-side SDK features not available server-side (e.g., push notifications, session tracking)
2. Real-time client-side processing is required
3. The partner SDK provides functionality beyond what mParticle can replicate server-side

### Technical Definition

A Kit is a library that:
1. **Extends `KitIntegration`** - The base class provided by mParticle
2. **Wraps a partner SDK** - Initializes and calls the partner's SDK
3. **Implements listener interfaces** - Receives events from mParticle SDK
4. **Maps mParticle APIs to partner APIs** - Translates events, identities, attributes

## How an mParticle Kit Works

### Lifecycle

```
App Launch
    ↓
MParticle.start(options)
    ↓
mParticle SDK fetches config from server
    ↓
Config includes Kit settings (API keys, filters)
    ↓
Kit Framework detects Kit class in app
    ↓
KitIntegration.onKitCreate(settings, context) called
    ↓
Kit initializes wrapped SDK (e.g., Mixpanel)
    ↓
Kit is now active and receives forwarded events
```

### Event Flow

```
App Code                    mParticle SDK              Kit                     Partner SDK
    |                           |                       |                          |
    |--logEvent(MPEvent)------->|                       |                          |
    |                           |--logEvent(filtered)-->|                          |
    |                           |                       |--track(mapped event)---->|
    |                           |                       |                          |
    |                           |<--ReportingMessage----|                          |
```

### Configuration

Kits receive configuration from mParticle servers at runtime:
- **Settings**: API keys, tokens, endpoint URLs
- **Filters**: Which events/attributes to forward
- **Mappings**: Event name transformations
- **Bracketing**: User percentage for gradual rollout

### Key Methods

| Method | When Called | Purpose |
|--------|-------------|---------|
| `onKitCreate(settings, context)` | Kit initialization | Initialize partner SDK with settings |
| `setOptOut(boolean)` | User opts in/out | Enable/disable tracking |
| `logEvent(MPEvent)` | App logs event | Forward event to partner |
| `logScreen(name, attrs)` | App logs screen | Forward screen view |
| `logEvent(CommerceEvent)` | App logs commerce | Forward purchase/commerce |
| `onLoginCompleted(user, request)` | User logs in | Update partner identity |
| `onLogoutCompleted(user, request)` | User logs out | Reset partner identity |
| `onSetUserAttribute(key, value, user)` | Attribute set | Sync user property |

## What Makes a Good mParticle Kit

### Design Principles

1. **Faithful Mapping**: Map mParticle concepts to partner concepts accurately
2. **Fail Gracefully**: Never crash the host app; log errors and return null
3. **Respect Filters**: Honor data filtering configured in mParticle dashboard
4. **Report Actions**: Return `ReportingMessage` objects for forwarded events
5. **Provide Direct Access**: Expose `getInstance()` for advanced SDK usage

### Implementation Best Practices

1. **Validate settings in `onKitCreate`**: Throw `IllegalArgumentException` if required settings are missing
2. **Check `started` state**: Don't call partner SDK until initialized
3. **Use `getSettings()`**: Access configuration consistently
4. **Handle null gracefully**: User data may be null or incomplete
5. **Thread safety**: Kit methods may be called from any thread

### Code Quality

1. **Unit tests**: Test initialization, event forwarding, error cases
2. **Integration tests**: Verify end-to-end with mock partner SDK
3. **Documentation**: README with setup instructions
4. **Proguard rules**: Prevent obfuscation issues

## Platform Differences: Android vs iOS

| Aspect | Android | iOS |
|--------|---------|-----|
| Base class | `KitIntegration` (abstract) | `MPKitProtocol` (protocol) |
| Event interfaces | Multiple interfaces (`EventListener`, etc.) | Single protocol with optional methods |
| Return type | `List<ReportingMessage>` or null | `MPKitExecStatus` with code |
| Error handling | Throw `IllegalArgumentException` | Return status with `.requirementsNotMet` |
| Kit registration | `KitIntegrationFactory` or `KitOptions` | `+load` with `registerExtension:` |
| Package manager | Maven Central (Gradle) | CocoaPods/SPM |
| Language | Kotlin/Java | Swift/Objective-C |

## The iOS Mixpanel Kit Implementation

### Features Implemented

Based on `mparticle-apple-integration-mixpanel`:

1. **Event Tracking**
   - Custom events → `mixpanel.track()`
   - Screen views → `mixpanel.track("Viewed {screen}")`

2. **Commerce Events**
   - Purchase → `people.trackCharge(revenue)`
   - Other actions → Expand to regular events

3. **Identity Management**
   - Login/Identify/Modify → `mixpanel.identify(userId)`
   - Logout → `mixpanel.reset()`
   - Configurable user ID source (CustomerId, MPID, Other, etc.)

4. **User Attributes**
   - Set attribute → `people.set()` or `registerSuperProperties()`
   - Remove attribute → `people.unset()` or `unregisterSuperProperty()`
   - Increment attribute → `people.increment()`
   - Configurable: People API vs Super Properties

5. **Opt-Out**
   - Opt out → `mixpanel.optOutTracking()`
   - Opt in → `mixpanel.optInTracking()`

### Configuration Settings

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `token` | String | (required) | Mixpanel project token |
| `serverURL` | String | null | Custom Mixpanel endpoint |
| `userIdentificationType` | Enum | CustomerId | How to identify users |
| `useMixpanelPeople` | Boolean | true | Use People API for attributes |

## Relevant Mixpanel Android APIs

### MixpanelAPI (Main Entry Point)

```java
// Initialization
MixpanelAPI.getInstance(context, token, trackAutomaticEvents)

// Event tracking
track(eventName)
track(eventName, properties)  // properties: JSONObject

// Identity
identify(distinctId)
reset()

// Super properties
registerSuperProperties(JSONObject)
unregisterSuperProperty(name)

// Opt-out
optOutTracking()
optInTracking()

// Flush
flush()
```

### People API

```java
MixpanelAPI.People people = mixpanel.getPeople();

// Identity
people.identify(distinctId)

// Properties
people.set(property, value)
people.set(JSONObject)
people.setOnce(property, value)
people.increment(property, value)
people.append(property, value)
people.union(property, JSONArray)
people.unset(property)

// Revenue
people.trackCharge(amount, properties)

// Deletion
people.deleteUser()
```

## Implementation Roadmap

### Phase 1: Core Structure
- [ ] Create Kotlin Kit class extending `KitIntegration`
- [ ] Implement required methods (`getName`, `onKitCreate`, `setOptOut`)
- [ ] Add Mixpanel SDK dependency
- [ ] Parse configuration settings

### Phase 2: Event Tracking
- [ ] Implement `EventListener` interface
- [ ] Forward custom events to `mixpanel.track()`
- [ ] Forward screen views with "Viewed" prefix

### Phase 3: Commerce
- [ ] Implement `CommerceListener` interface
- [ ] Handle purchase events with `trackCharge()`
- [ ] Expand non-purchase events

### Phase 4: Identity
- [ ] Implement `IdentityListener` interface
- [ ] Map user ID based on configuration
- [ ] Handle login/logout/identify/modify

### Phase 5: User Attributes
- [ ] Implement `UserAttributeListener` interface
- [ ] Support both People API and Super Properties modes
- [ ] Handle set/remove/increment operations

### Phase 6: Testing & Polish
- [ ] Unit tests for all functionality
- [ ] Integration tests
- [ ] README documentation
- [ ] Proguard rules
