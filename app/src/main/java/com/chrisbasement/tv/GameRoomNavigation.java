package com.chrisbasement.tv;

import java.net.URI;
import java.net.URISyntaxException;

/** Native Back routing, independent of WebView history or page JavaScript. */
final class GameRoomNavigation {
    static final String HOME_URL = "https://nubulator.github.io/Chris-basement/games.html";

    private GameRoomNavigation() {}

    static boolean isGame(String url) {
        if (url == null) return false;
        try {
            URI page = new URI(url);
            if (!"https".equalsIgnoreCase(page.getScheme())) return false;
            if ("bernie-bernie-family-card-night.nubulator.chatgpt.site".equalsIgnoreCase(page.getHost())) {
                return true;
            }
            if (!"nubulator.github.io".equalsIgnoreCase(page.getHost())) return false;
            String path = page.getPath();
            if (path == null) return false;
            switch (path) {
                case "/Chris-basement/kings.html":
                case "/Chris-basement/euchre.html":
                case "/Chris-basement/trivia.html":
                case "/Chris-basement/music.html":
                case "/Chris-basement/survey.html":
                case "/Chris-basement/casino.html":
                case "/Chris-basement/last-call.html":
                case "/Chris-basement/bernie.html":
                    return true;
                default:
                    return false;
            }
        } catch (URISyntaxException ignored) {
            return false;
        }
    }
}
