package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.RestaurantViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testAppCompositionAndSplash() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = RestaurantViewModel(application)
    
    composeTestRule.setContent {
      CeylonBiteMainApp(viewModel)
    }
    
    composeTestRule.waitForIdle()
    
    // Test language changes
    composeTestRule.onNodeWithTag("lang_btn_si").performClick()
    composeTestRule.waitForIdle()
    
    composeTestRule.onNodeWithTag("lang_btn_ta").performClick()
    composeTestRule.waitForIdle()
    
    composeTestRule.onNodeWithTag("lang_btn_en").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun testNavigateToCustomerPortal() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = RestaurantViewModel(application)
    
    composeTestRule.setContent {
      CeylonBiteMainApp(viewModel)
    }
    composeTestRule.waitForIdle()
    
    // Click Customer Portal
    composeTestRule.onNodeWithTag("customer_portal_button").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun testNavigateToStaffPortal() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = RestaurantViewModel(application)
    
    composeTestRule.setContent {
      CeylonBiteMainApp(viewModel)
    }
    composeTestRule.waitForIdle()
    
    // Click Staff Portal
    composeTestRule.onNodeWithTag("staff_portal_button").performClick()
    composeTestRule.waitForIdle()
  }
}
