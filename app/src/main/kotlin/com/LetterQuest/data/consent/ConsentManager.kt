package com.LetterQuest.data.consent

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps the UMP SDK. Call [requestConsent] once per Activity start; the SDK
 * handles "already obtained" and "not required" states internally so it is safe
 * to call every time MainActivity is created.
 *
 * The callback receives true if [canRequestAds] is true after the flow completes,
 * meaning either consent was obtained or the user is in a region where consent
 * is not required. MobileAds should only be initialized when this returns true.
 */
@Singleton
class ConsentManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun requestConsent(activity: Activity, onResult: (canShowAds: Boolean) -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInfo = UserMessagingPlatform.getConsentInformation(context)
        consentInfo.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                    onResult(consentInfo.canRequestAds())
                }
            },
            { _ ->
                // On error, do not show ads (conservative — avoids GDPR violation).
                onResult(false)
            }
        )
    }

    /** True if the last known consent state allows ads (cached, no network call). */
    fun canRequestAds(): Boolean =
        UserMessagingPlatform.getConsentInformation(context).canRequestAds()

    /** Resets consent state — use in debug builds to re-test the form. */
    fun resetForTesting() {
        UserMessagingPlatform.getConsentInformation(context).reset()
    }
}
