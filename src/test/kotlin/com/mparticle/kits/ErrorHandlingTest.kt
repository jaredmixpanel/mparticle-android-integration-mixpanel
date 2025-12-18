package com.mparticle.kits

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.mparticle.MPEvent
import com.mparticle.MParticle
import com.mparticle.kits.mocks.MockMParticleUser
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner

/**
 * Tests that verify error handling behavior - methods should return
 * appropriate defaults when not started or with invalid input.
 */
@RunWith(RobolectricTestRunner::class)
class ErrorHandlingTest {

    private lateinit var kit: TestableMixpanelKit
    private lateinit var mockMixpanel: MixpanelAPI
    private lateinit var mockPeople: MixpanelAPI.People
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        kit = TestableMixpanelKit()
        mockContext = mock(Context::class.java)
        mockMixpanel = mock(MixpanelAPI::class.java)
        mockPeople = mock(MixpanelAPI.People::class.java)
        `when`(mockMixpanel.people).thenReturn(mockPeople)
    }

    private fun initializeKit() {
        kit.setMockMixpanelAPI(mockMixpanel)
        kit.onKitCreate(mapOf(KEY_TOKEN to TEST_TOKEN), mockContext)
    }

    // Methods should return appropriate values when not started

    @Test
    fun `logEvent returns null when not started`() {
        val event = MPEvent.Builder("Test", MParticle.EventType.Other).build()
        val result = kit.logEvent(event)
        assertNull(result)
    }

    @Test
    fun `logScreen returns null when not started`() {
        val result = kit.logScreen("TestScreen", mutableMapOf())
        assertNull(result)
    }

    @Test
    fun `logError returns null when not started`() {
        val result = kit.logError("Error", mutableMapOf())
        assertNull(result)
    }

    @Test
    fun `logException returns null when not started`() {
        val result = kit.logException(RuntimeException(), mutableMapOf(), "Error")
        assertNull(result)
    }

    @Test
    fun `leaveBreadcrumb returns null when not started`() {
        val result = kit.leaveBreadcrumb("breadcrumb")
        assertNull(result)
    }

    @Test
    fun `setOptOut returns empty list when not started`() {
        val result = kit.setOptOut(true)
        assertTrue(result.isEmpty())
    }

    // Methods should handle null/empty inputs gracefully

    @Test
    fun `logEvent returns null for empty event name`() {
        initializeKit()
        val event = MPEvent.Builder("", MParticle.EventType.Other).build()
        val result = kit.logEvent(event)
        assertNull(result)
    }

    @Test
    fun `logScreen returns null for null screen name`() {
        initializeKit()
        val result = kit.logScreen(null, mutableMapOf())
        assertNull(result)
    }

    @Test
    fun `logScreen returns null for empty screen name`() {
        initializeKit()
        val result = kit.logScreen("", mutableMapOf())
        assertNull(result)
    }

    @Test
    fun `leaveBreadcrumb returns null for null breadcrumb`() {
        initializeKit()
        val result = kit.leaveBreadcrumb(null)
        assertNull(result)
    }

    @Test
    fun `leaveBreadcrumb returns null for empty breadcrumb`() {
        initializeKit()
        val result = kit.leaveBreadcrumb("")
        assertNull(result)
    }

    // Identity methods should handle null user gracefully

    @Test
    fun `onLoginCompleted does not call identify with null user`() {
        initializeKit()
        kit.onLoginCompleted(null, null)
        verify(mockMixpanel, never()).identify(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `onIdentifyCompleted does not call identify with null user`() {
        initializeKit()
        kit.onIdentifyCompleted(null, null)
        verify(mockMixpanel, never()).identify(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `onUserIdentified does not call identify with null user`() {
        initializeKit()
        kit.onUserIdentified(null)
        verify(mockMixpanel, never()).identify(org.mockito.ArgumentMatchers.any())
    }

    // User attribute methods should handle null/empty inputs gracefully

    @Test
    fun `onSetUserAttribute does nothing with null key`() {
        initializeKit()
        val mockUser = mock(FilteredMParticleUser::class.java)
        kit.onSetUserAttribute(null, "value", mockUser)
        verify(mockPeople, never()).set(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `onSetUserAttribute does nothing with empty key`() {
        initializeKit()
        val mockUser = mock(FilteredMParticleUser::class.java)
        kit.onSetUserAttribute("", "value", mockUser)
        verify(mockPeople, never()).set(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `onSetUserAttribute does nothing with null value`() {
        initializeKit()
        val mockUser = mock(FilteredMParticleUser::class.java)
        kit.onSetUserAttribute("key", null, mockUser)
        verify(mockPeople, never()).set(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `onRemoveUserAttribute does nothing with null key`() {
        initializeKit()
        val mockUser = mock(FilteredMParticleUser::class.java)
        kit.onRemoveUserAttribute(null, mockUser)
        verify(mockPeople, never()).unset(org.mockito.ArgumentMatchers.anyString())
    }

    @Test
    fun `onRemoveUserAttribute does nothing with empty key`() {
        initializeKit()
        val mockUser = mock(FilteredMParticleUser::class.java)
        kit.onRemoveUserAttribute("", mockUser)
        verify(mockPeople, never()).unset(org.mockito.ArgumentMatchers.anyString())
    }

    // User with missing customer ID should not call identify

    @Test
    fun `onLoginCompleted does not call identify when user has no customer ID`() {
        initializeKit()
        val mockUser = MockMParticleUser(emptyMap())
        kit.onLoginCompleted(mockUser, null)
        verify(mockMixpanel, never()).identify(org.mockito.ArgumentMatchers.any())
    }
}
