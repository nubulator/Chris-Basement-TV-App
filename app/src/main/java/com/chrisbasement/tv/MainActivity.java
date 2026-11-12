package com.chrisbasement.tv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final String REGISTRY = "https://nubulator.github.io/Chris-basement/dashboards.json";
    private static final String DEFAULT_URL = "https://nubulator.github.io/Chris-basement/index-v3822.html";
    private WebView webView;
    private final ArrayList<String> names = new ArrayList<>();
    private final ArrayList<String> urls = new ArrayList<>();
    private final ArrayList<String> ids = new ArrayList<>();
    private SharedPreferences prefs;
    private boolean menuOpen = false;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private int backRequestId = 0;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(5894 | 1024 | 512);
        prefs = getSharedPreferences("dashboard", MODE_PRIVATE);
        names.add("Chris' Basement"); urls.add(DEFAULT_URL); ids.add("basement");
        webView = new WebView(this);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        setContentView(webView);
        loadSelected();
        refreshRegistry();
    }

    private void loadSelected() {
        String selected = prefs.getString("selected_id", "basement");
        int index = ids.indexOf(selected);
        if (index < 0) index = 0;
        webView.loadUrl(urls.get(index));
        webView.requestFocus();
    }

    private void refreshRegistry() {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection)new URL(REGISTRY + "?t=" + System.currentTimeMillis()).openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setUseCaches(false);
                try (InputStream input = connection.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    byte[] buf = new byte[4096]; int n;
                    while ((n = input.read(buf)) != -1) output.write(buf, 0, n);
                    JSONObject root = new JSONObject(output.toString("UTF-8"));
                    JSONArray array = root.getJSONArray("dashboards");
                    ArrayList<String> nextNames = new ArrayList<>(), nextUrls = new ArrayList<>(), nextIds = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject item = array.getJSONObject(i);
                        String url = item.getString("url");
                        if (!url.startsWith("https://")) continue;
                        nextNames.add(item.getString("name"));
                        nextUrls.add(url);
                        nextIds.add(item.getString("id"));
                    }
                    if (!nextIds.isEmpty()) runOnUiThread(() -> {
                        names.clear(); names.addAll(nextNames);
                        urls.clear(); urls.addAll(nextUrls);
                        ids.clear(); ids.addAll(nextIds);
                        loadSelected();
                    });
                }
            } catch (Exception ignored) {
                // Keep the built-in dashboard available while offline.
            } finally { if (connection != null) connection.disconnect(); }
        }).start();
    }

    private void showDashboardMenu() {
        if (menuOpen) return;
        menuOpen = true;
        String[] choices = names.toArray(new String[0]);
        new AlertDialog.Builder(this)
            .setTitle("Choose dashboard")
            .setItems(choices, (dialog, which) -> {
                prefs.edit().putString("selected_id", ids.get(which)).apply();
                webView.loadUrl(urls.get(which));
                webView.requestFocus();
            })
            .setNegativeButton("Cancel", (dialog, which) -> {})
            .setOnDismissListener(dialog -> { menuOpen = false; webView.requestFocus(); })
            .show();
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BUTTON_START) {
            showDashboardMenu();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Holding Back must not leave the Game Room after returning there.
            if (event.getRepeatCount() > 0) return true;
            if (GameRoomNavigation.isGame(webView.getUrl())) {
                // Cancel any pending picker fallback and route natively, even
                // when a game has replaced its history or JavaScript is busy.
                backRequestId++;
                webView.loadUrl(GameRoomNavigation.HOME_URL);
                webView.requestFocus();
                return true;
            }
            // The embedded radar can intercept navigation; never let a stalled
            // WebView JavaScript callback prevent the dashboard picker opening.
            final int requestId = ++backRequestId;
            uiHandler.postDelayed(() -> {
                if (requestId == backRequestId && !menuOpen) {
                    backRequestId++;
                    showDashboardMenu();
                }
            }, 500);
            try {
                webView.evaluateJavascript(
                    "(function(){var modal=document.getElementById('radarModal');" +
                    "if(modal&&modal.classList.contains('open')){" +
                    "if(typeof closeRadar==='function')closeRadar();" +
                    "else modal.classList.remove('open');return true;}" +
                    "return false;})()",
                    result -> {
                        if (requestId != backRequestId) return;
                        backRequestId++;
                        if (!"true".equals(result)) showDashboardMenu();
                    });
            } catch (Exception ignored) {
                if (requestId == backRequestId) {
                    backRequestId++;
                    showDashboardMenu();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

