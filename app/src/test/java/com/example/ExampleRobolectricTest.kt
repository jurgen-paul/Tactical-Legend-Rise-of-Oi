package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.OiSpecialistRoster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Oistars Ops 1", appName)
  }

  @Test
  fun `the oi squad roster contains all specialists`() {
    val roster = OiSpecialistRoster.specialists
    assertEquals(9, roster.size)
    val callsigns = roster.map { it.callsign }
    assertTrue(callsigns.contains("RUIN"))
    assertTrue(callsigns.contains("PROPHET"))
    assertTrue(callsigns.contains("OUTRIDER"))
    assertTrue(callsigns.contains("BATTERY"))
    assertTrue(callsigns.contains("SERAPH"))
    assertTrue(callsigns.contains("NOMAD"))
    assertTrue(callsigns.contains("SPECTRE"))
    assertTrue(callsigns.contains("REAPER"))
    assertTrue(callsigns.contains("FIREBREAK"))
  }

  @Test
  fun `specialist lookup by id and name works`() {
    val ruin = OiSpecialistRoster.getSpecialist("hero_vanguard")
    assertNotNull(ruin)
    assertEquals("RUIN", ruin?.callsign)
    assertEquals("Gravity Spikes", ruin?.signatureWeapon)

    val prophet = OiSpecialistRoster.getSpecialistByName("Prophet")
    assertNotNull(prophet)
    assertEquals("Tempest Arc Rifle", prophet?.signatureWeapon)
  }
}
