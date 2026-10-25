package com.fsdspin.app

import android.app.Activity
import android.content.Context
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds

/**
 * Unity Ads integration for FSD SPIN — the app is free, with ads.
 *
 * IMPORTANT: replace [GAME_ID] with the real Game ID from the Unity
 * dashboard (Unity Ads > Monetization > your Android game) once the
 * Unity account is connected. Until then the placeholder below keeps
 * ads disabled: initialisation fails gracefully and the app simply
 * never shows an ad — it never crashes or blocks the wheel.
 *
 * Test mode is ON while the placeholder is in place, so no real
 * impressions are served during development.
 *
 * Placements use Unity's standard Android names; if the dashboard
 * renames them, update the two constants below to match.
 */
object UnityAdsManager {
    // TODO: paste the real Unity Game ID here after connecting the account.
    const val GAME_ID = "YOUR_UNITY_GAME_ID"
    private const val INTERSTITIAL_PLACEMENT = "Interstitial_Android"

    /** True only when a real Game ID has been set AND init succeeded. */
    var adsReady: Boolean = false
        private set

    private val isConfigured: Boolean
        get() = GAME_ID != "YOUR_UNITY_GAME_ID" && GAME_ID.isNotBlank()

    fun initialize(context: Context) {
        if (!isConfigured) return
        try {
            UnityAds.initialize(
                context,
                GAME_ID,
                true, // test mode until the real launch build
                object : IUnityAdsInitializationListener {
                    override fun onInitializationComplete() {
                        adsReady = true
                    }

                    override fun onInitializationFailed(
                        error: UnityAds.UnityAdsInitializationError,
                        message: String
                    ) {
                        adsReady = false
                    }
                }
            )
        } catch (_: Exception) {
            adsReady = false
        }
    }

    /**
     * Shows one interstitial ad if ads are ready. Silent no-op otherwise,
     * so the game flow is never interrupted by an ad failure.
     */
    fun showInterstitial(activity: Activity) {
        if (!adsReady) return
        try {
            UnityAds.load(
                INTERSTITIAL_PLACEMENT,
                object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placementId: String) {
                        try {
                            UnityAds.show(
                                activity,
                                placementId,
                                object : IUnityAdsShowListener {
                                    override fun onUnityAdsShowFailure(
                                        placementId: String,
                                        error: UnityAds.UnityAdsShowError,
                                        message: String
                                    ) {
                                    }

                                    override fun onUnityAdsShowStart(
                                        placementId: String
                                    ) {
                                    }

                                    override fun onUnityAdsShowClick(
                                        placementId: String
                                    ) {
                                    }

                                    override fun onUnityAdsShowComplete(
                                        placementId: String,
                                        state: UnityAds.UnityAdsShowCompletionState
                                    ) {
                                    }
                                }
                            )
                        } catch (_: Exception) {
                        }
                    }

                    override fun onUnityAdsFailedToLoad(
                        placementId: String,
                        error: UnityAds.UnityAdsLoadError,
                        message: String
                    ) {
                    }
                }
            )
        } catch (_: Exception) {
        }
    }
}
