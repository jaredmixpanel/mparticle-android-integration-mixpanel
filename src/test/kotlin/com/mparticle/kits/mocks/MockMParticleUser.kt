package com.mparticle.kits.mocks

import com.mparticle.MParticle
import com.mparticle.UserAttributeListenerType
import com.mparticle.audience.AudienceResponse
import com.mparticle.audience.AudienceTask
import com.mparticle.consent.ConsentState
import com.mparticle.identity.MParticleUser
import com.mparticle.kits.TEST_MPID

/**
 * Mock MParticleUser for testing identity operations.
 */
internal class MockMParticleUser(
    private val identities: Map<MParticle.IdentityType, String> = emptyMap(),
    private val mpid: Long = TEST_MPID
) : MParticleUser {

    override fun getId(): Long = mpid
    override fun getUserIdentities(): Map<MParticle.IdentityType, String> = identities
    override fun getUserAttributes(): Map<String, Any> = emptyMap()
    override fun getUserAttributes(listener: UserAttributeListenerType?): MutableMap<String, Any>? = null
    override fun setUserAttributes(map: Map<String, Any>): Boolean = false
    override fun setUserAttribute(key: String, value: Any): Boolean = false
    override fun setUserAttributeList(key: String, value: Any): Boolean = false
    override fun incrementUserAttribute(key: String, value: Number?): Boolean = false
    override fun removeUserAttribute(key: String): Boolean = false
    override fun setUserTag(tag: String): Boolean = false
    override fun getConsentState(): ConsentState = ConsentState.withConsentState("").build()
    override fun setConsentState(state: ConsentState?) {}
    override fun isLoggedIn(): Boolean = false
    override fun getFirstSeenTime(): Long = 0
    override fun getLastSeenTime(): Long = 0
    override fun getUserAudiences(): AudienceTask<AudienceResponse>? = null
}
