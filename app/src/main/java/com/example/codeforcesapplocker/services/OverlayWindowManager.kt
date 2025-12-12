package com.example.codeforcesapplocker.services

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayWindowManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: ComposeView? = null
    private var lifecycleOwner: MyLifecycleOwner? = null

    fun show() {
        try {
            if (overlayView != null) return // Already showing

            Log.d("OverlayManager", "Attempting to show overlay...")

            // 1. Create Layout Parameters
            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
            layoutParams.gravity = Gravity.CENTER

            // 2. Initialize Lifecycle
            lifecycleOwner = MyLifecycleOwner()
            lifecycleOwner?.onCreate()

            // 3. Create the View
            overlayView = ComposeView(context).apply {
                // IMPORTANT: We must set the owners on the view itself
                val lifecycleOwner = this@OverlayWindowManager.lifecycleOwner!!
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeViewModelStoreOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)

                setContent {
                    OverlayScreen(
                        onGoToCodeforces = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://codeforces.com"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // 4. Start Lifecycle
            lifecycleOwner?.onStart()
            lifecycleOwner?.onResume()

            // 5. Add to Window
            windowManager.addView(overlayView, layoutParams)
            Log.d("OverlayManager", "Overlay shown successfully!")

        } catch (e: Exception) {
            Log.e("OverlayManager", "FAILED to show overlay", e)
        }
    }

    fun hide() {
        try {
            if (overlayView != null) {
                lifecycleOwner?.onPause()
                lifecycleOwner?.onStop()
                lifecycleOwner?.onDestroy()
                windowManager.removeView(overlayView)
                overlayView = null
                lifecycleOwner = null
                Log.d("OverlayManager", "Overlay hidden.")
            }
        } catch (e: Exception) {
            Log.e("OverlayManager", "Error hiding overlay", e)
        }
    }
}