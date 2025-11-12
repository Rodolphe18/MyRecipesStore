package com.francotte.video

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.francotte.common.ScreenCounter

@Composable
fun VideoFullScreen(youtubeUrl: String) {
    val videoId = remember(youtubeUrl) {
        when {
            youtubeUrl.contains("watch?v=") ->
                youtubeUrl.substringAfter("v=").substringBefore("&")
            youtubeUrl.contains("youtu.be/") ->
                youtubeUrl.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
            youtubeUrl.contains("/shorts/") ->
                youtubeUrl.substringAfter("/shorts/").substringBefore("?").substringBefore("&")
            youtubeUrl.contains("/embed/") ->
                youtubeUrl.substringAfter("/embed/").substringBefore("?").substringBefore("&")
            else -> ""
        }
    }
    if (videoId.isNotBlank()) {
        val origin = "https://app.local" // n'importe quel https stable, mais garde-le partout
        val embedUrl = "https://www.youtube.com/embed/$videoId" +
                "?autoplay=1&mute=1&playsinline=1&rel=0&enablejsapi=1&origin=$origin"

        val html = """
<!doctype html><html>
<head>
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <style>html,body{margin:0;background:#000;height:100%}#wrap{position:fixed;inset:0}</style>
</head>
<body>
  <div id="wrap">
    <iframe
      src="$embedUrl"
      title="YouTube video player"
      allow="autoplay; encrypted-media; picture-in-picture; clipboard-write"
      allowfullscreen
      referrerpolicy="origin-when-cross-origin"
      style="border:0;width:100%;height:100%"></iframe>
  </div>
</body>
</html>
""".trimIndent()
        var webView by remember { mutableStateOf<WebView?>(null) }
        DisposableEffect(videoId) {
            onDispose {
                webView?.apply {
                    stopLoading()
                    loadUrl("about:blank")
                    postDelayed({
                        removeAllViews()
                        destroy()
                    }, 100)
                }
                webView = null
            }
        }
        AndroidView(
            factory = { context ->
                WebView(context).apply {

                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.javaScriptCanOpenWindowsAutomatically = true

                    // UA : repartir d'un UA Chrome standard + suffixe
                    settings.userAgentString =
                        WebSettings.getDefaultUserAgent(context) + " YTWebView/1.0"

                    webChromeClient = object : WebChromeClient() {
                        override fun onPermissionRequest(request: PermissionRequest?) {
                            // Autoriser audio/vidéo pour l'iFrame
                            request?.grant(request.resources)
                        }

                        override fun onConsoleMessage(msg: ConsoleMessage?): Boolean {
                            Log.d("YTWebView", "${msg?.message()} @${msg?.lineNumber()}")
                            return super.onConsoleMessage(msg)
                        }
                    }
                    webViewClient = WebViewClient()

                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                    // Accélération matérielle conseillée
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)

                    loadDataWithBaseURL(origin, html, "text/html", "utf-8", null)
                    webView = this
                }
            },
            modifier = Modifier.fillMaxWidth(),
            update = { view ->
                view.loadDataWithBaseURL(
                    origin,
                    html,
                    "text/html",
                    "utf-8",
                    null
                )
            }
        )
    }
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
