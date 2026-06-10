package com.francotte.designsystem.component

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/** Any stable https origin, kept consistent between the base URL and the embed `origin` param. */
private const val ORIGIN = "https://app.local"

/**
 * Plays a YouTube video (by its [videoId]) inside a WebView/iframe. Shared by the fullscreen
 * video player and the recipe detail screen — the only difference is the size, driven by [modifier].
 */
@Composable
fun YouTubeWebViewPlayer(
    videoId: String,
    modifier: Modifier = Modifier,
) {
    val html = remember(videoId) { youTubeEmbedHtml(videoId) }
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
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.userAgentString = WebSettings.getDefaultUserAgent(context) + " YTWebView/1.0"
                webChromeClient = object : WebChromeClient() {
                    override fun onPermissionRequest(request: PermissionRequest?) {
                        request?.grant(request.resources)
                    }

                    override fun onConsoleMessage(msg: ConsoleMessage?): Boolean {
                        Log.d("YTWebView", "${msg?.message()} @${msg?.lineNumber()}")
                        return super.onConsoleMessage(msg)
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onReceivedError(v: WebView, r: WebResourceRequest, e: WebResourceError) {
                        Log.e("YTWebView", "WebError ${e.errorCode}: ${e.description}")
                    }

                    override fun onReceivedHttpError(v: WebView, r: WebResourceRequest, resp: WebResourceResponse) {
                        Log.e("YTWebView", "HTTP ${resp.statusCode} ${resp.reasonPhrase}")
                    }
                }
                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                loadDataWithBaseURL(ORIGIN, html, "text/html", "utf-8", null)
                webView = this
            }
        },
        modifier = modifier,
        update = { view ->
            view.loadDataWithBaseURL(ORIGIN, html, "text/html", "utf-8", null)
        },
    )
}

private fun youTubeEmbedHtml(videoId: String): String {
    val embedUrl =
        "https://www.youtube.com/embed/$videoId" +
            "?autoplay=1&mute=1&playsinline=1&rel=0&enablejsapi=1&origin=$ORIGIN"
    return """
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
}
