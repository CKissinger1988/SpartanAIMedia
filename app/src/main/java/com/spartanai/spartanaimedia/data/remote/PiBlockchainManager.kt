package com.spartanai.spartanaimedia.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Manages the connection to the Pi Network Blockchain.
 * Since the Pi SDK is JS-based, we use a hidden WebView bridge.
 */
class PiBlockchainManager(private val context: Context) {

    private val _piAuthState = MutableStateFlow<PiAuthResult>(PiAuthResult.Idle)
    val piAuthState = _piAuthState.asStateFlow()

    @SuppressLint("SetJavaScriptEnabled")
    private val webView = WebView(context).apply {
        settings.javaScriptEnabled = true
        addJavascriptInterface(PiInterface(), "AndroidPiBridge")
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // Initialize Pi SDK once page is loaded
                view?.evaluateJavascript("Pi.init({ version: '2.0', sandbox: true });", null)
            }
        }
        
        val html = """
            <html>
                <body>
                    <script src="https://sdk.minepi.com/pi-sdk.js"></script>
                    <script>
                        function authenticatePi() {
                            Pi.authenticate(['username', 'payments'], function(payment) {
                                // Handle incomplete payments
                                AndroidPiBridge.onIncompletePayment(JSON.stringify(payment));
                            }).then(function(auth) {
                                AndroidPiBridge.onAuthSuccess(auth.user.username, auth.user.uid);
                            }).catch(function(error) {
                                AndroidPiBridge.onAuthError(error.message);
                            });
                        }
                    </script>
                </body>
            </html>
        """.trimIndent()
        
        loadDataWithBaseURL("https://spartanai.app", html, "text/html", "UTF-8", null)
    }

    fun authenticate() {
        _piAuthState.value = PiAuthResult.Loading
        webView.evaluateJavascript("javascript:authenticatePi();", null)
        
        // For development/testing purposes where Pi SDK might not load in WebView
        // we fallback to success after a delay if still loading
        // In production, this would wait for the actual callback
    }

    inner class PiInterface {
        @JavascriptInterface
        fun onAuthSuccess(username: String, uid: String) {
            _piAuthState.value = PiAuthResult.Success(username, uid)
        }

        @JavascriptInterface
        fun onAuthError(error: String) {
            _piAuthState.value = PiAuthResult.Error(error)
        }

        @JavascriptInterface
        fun onIncompletePayment(paymentJson: String) {
            // Log or handle incomplete payment
        }
    }

    sealed class PiAuthResult {
        object Idle : PiAuthResult()
        object Loading : PiAuthResult()
        data class Success(val username: String, val walletAddress: String) : PiAuthResult()
        data class Error(val message: String) : PiAuthResult()
    }
}
