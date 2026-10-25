package com.fsdspin.app

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds

/**
 * Unity Ads integration for FSD SPIN — the app is free, with ads.
 *
 * Live setup (Unity Dashboard, project "FSD SPIN", Android):
 * - Game ID: 800410936
 * - Interstitial placement: BP_Interstitial_Android (status: Active)
 * - Audience: general audience (not primarily targeted to children)
 *
 * How ads are shown: tapping the SPIN button first tries to show an
 * interstitial; the wheel spins as soon as the ad closes. If no ad is
 * loaded or anything fails, [onFinished] runs immediately, so a spin
 * is never blocked by an ad. Test mode is OFF, so real ads are served.
 */
object UnityAdsManager {
    const val GAME_ID = "800410936"
    private const val INTERSTITIAL_PLACEMENT = "BP_Interstitial_Android"

    /** True only when init succeeded with the real Game ID. */
    var adsReady: Boolean = false
        private set

    /** True when an interstitial is preloaded and ready to show at once. */
    private var adLoaded: Boolean = false

    /** Readable in Settings, so the user can see why an ad did not show. */
    val isAdLoaded: Boolean get() = adLoaded

    /**
     * Last Unity status, shown in Settings for diagnosis: "Ready",
     * "Not initialized yet", or the exact init/load/show failure text.
     */
    var lastError by mutableStateOf("Not initialized yet")
        private set

    fun initialize(context: Context) {
        try {
            UnityAds.initialize(
                context,
                GAME_ID,
                false, // test mode OFF: real ads for the live app
                object : IUnityAdsInitializationListener {
                    override fun onInitializationComplete() {
                        adsReady = true
                        lastError = "Ready"
                        preload()
                    }

                    override fun onInitializationFailed(
                        error: UnityAds.UnityAdsInitializationError,
                        message: String
                    ) {
                        adsReady = false
                        lastError = "Init failed: $message"
                    }
                }
            )
        } catch (_: Exception) {
            adsReady = false
        }
    }

    /** Preloads an interstitial so the SPIN button can show one at once. */
    private fun preload() {
        if (!adsReady || adLoaded) return
        try {
            UnityAds.load(
                INTERSTITIAL_PLACEMENT,
                object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placementId: String) {
                        adLoaded = true
                    }

                    override fun onUnityAdsFailedToLoad(
                        placementId: String,
                        error: UnityAds.UnityAdsLoadError,
                        message: String
                    ) {
                        adLoaded = false
                        lastError = "Load failed: $message"
                    }
                }
            )
        } catch (_: Exception) {
            adLoaded = false
        }
    }

    /**
     * Shows one interstitial ad if one is ready, then always runs
     * [onFinished] — on ad close, on ad failure, or immediately when
     * no ad is available. The caller (SPIN button) spins in [onFinished],
     * so the wheel is never stuck waiting for an ad.
     */
    fun showInterstitial(activity: Activity, onFinished: () -> Unit) {
        if (!adsReady) {
            onFinished()
            return
        }
        val showListener = object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(
                placementId: String,
                error: UnityAds.UnityAdsShowError,
                message: String
            ) {
                adLoaded = false
                lastError = "Show failed: $message"
                preload()
                onFinished()
            }

            override fun onUnityAdsShowStart(placementId: String) {
            }

            override fun onUnityAdsShowClick(placementId: String) {
            }

            override fun onUnityAdsShowComplete(
                placementId: String,
                state: UnityAds.UnityAdsShowCompletionState
            ) {
                adLoaded = false
                preload()
                onFinished()
            }
        }
        try {
            if (adLoaded) {
                adLoaded = false
                UnityAds.show(activity, INTERSTITIAL_PLACEMENT, showListener)
            } else {
                // Nothing preloaded: load now, show as soon as it loads.
                UnityAds.load(
                    INTERSTITIAL_PLACEMENT,
                    object : IUnityAdsLoadListener {
                        override fun onUnityAdsAdLoaded(placementId: String) {
                            try {
                                UnityAds.show(activity, placementId, showListener)
                            } catch (_: Exception) {
                                onFinished()
                            }
                        }

                        override fun onUnityAdsFailedToLoad(
                            placementId: String,
                            error: UnityAds.UnityAdsLoadError,
                            message: String
                        ) {
                            lastError = "Load failed: $message"
                            onFinished()
                        }
                    }
                )
            }
        } catch (_: Exception) {
            onFinished()
        }
    }
}
