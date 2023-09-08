package com.cesoft.cesardoom

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication.Callbacks
import games.rednblack.gdxar.GdxARConfiguration
import games.rednblack.gdxar.GdxArApplicationListener
import games.rednblack.gdxar.GdxLightEstimationMode
import games.rednblack.gdxar.GdxPlaneFindingMode
import games.rednblack.gdxar.android.ARFragmentApplication
import games.rednblack.gdxar.android.ARSupportFragment

// THANKS TO: https://github.com/fgnm/ARPlayground
// SPIDER : https://www.turbosquid.com/es/3d-models/ready-spider-monster-model-1505616
////https://www.turbosquid.com/3d-models/military-free-3d-2098161
class AndroidLauncher : FragmentActivity(), Callbacks {

    private var applicationListener: GdxArApplicationListener? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if(isGranted) {
                val configuration = AndroidApplicationConfiguration()
                configuration.useGL30 = true
                configuration.a = 8
                configuration.depth = 16
                configuration.stencil = 8
                configuration.numSamples = 2
                launchAR(configuration)
            }
            else {
                Toast.makeText(this, "Camera permission not granted!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applicationListener = ArPlayground()
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun launchAR(configuration: AndroidApplicationConfiguration) {
        val supportFragment = ARSupportFragment()
        supportFragmentManager.beginTransaction()
            .add(supportFragment, ARSupportFragment.TAG)
            .commitAllowingStateLoss()

        supportFragment.arSupported.thenAccept { useAR: Boolean ->
            removeSupportFragment()
            if (useAR) {
                val fragment = ARFragmentApplication()
                fragment.configuration = configuration
                val gdxARConfiguration = GdxARConfiguration()
                gdxARConfiguration.debugMode = true
                gdxARConfiguration.enableDepth = false
                gdxARConfiguration.planeFindingMode = GdxPlaneFindingMode.HORIZONTAL
                gdxARConfiguration.lightEstimationMode = GdxLightEstimationMode.ENVIRONMENTAL_HDR
                fragment.setArApplication(applicationListener, gdxARConfiguration)
                setFragment(fragment)
            } else {
                Toast.makeText(this, "ARCore is not supported", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.exceptionally { ex: Throwable? ->
            removeSupportFragment()
            Toast.makeText(this, "Failed to load ARCore check errors", Toast.LENGTH_SHORT).show()
            finish()
            null
        }
    }

    private fun setFragment(fragment: Fragment) {
        // Finally place it in the layout.
        supportFragmentManager.beginTransaction()
            .add(android.R.id.content, fragment)
            .commitAllowingStateLoss()
    }

    private fun removeSupportFragment() {
        val fragment = supportFragmentManager.findFragmentByTag(ARSupportFragment.TAG)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
    }

    override fun exit() {}
}