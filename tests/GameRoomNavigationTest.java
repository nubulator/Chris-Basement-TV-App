package com.chrisbasement.tv;

/** Standalone regression check: no Android SDK or test dependencies required. */
public final class GameRoomNavigationTest {
    private static int checks;

    private static void expect(boolean expected, String url) {
        if (GameRoomNavigation.isGame(url) != expected) {
            throw new AssertionError("Unexpected Back route for: " + url);
        }
        checks++;
    }

    public static void main(String[] args) {
        String base = "https://nubulator.github.io/Chris-basement/";
        for (String game : new String[]{"kings", "euchre", "trivia", "music", "survey", "casino", "last-call", "bernie"}) {
            expect(true, base + game + ".html");
            expect(true, base + game + ".html?refresh=123#setup");
        }
        expect(true, "https://bernie-bernie-family-card-night.nubulator.chatgpt.site");
        expect(true, "https://bernie-bernie-family-card-night.nubulator.chatgpt.site/room/ABC?view=tv#table");
        expect(false, GameRoomNavigation.HOME_URL);
        expect(false, GameRoomNavigation.HOME_URL + "?refresh=123");
        expect(false, base + "index-v3822.html");
        expect(false, base + "game-day.html?team=browns");
        expect(false, "https://nubulator.github.io/Jim-Amys-Garage/");
        expect(false, "https://isonfamilyhome.com/");
        expect(false, "https://example.com/trivia.html");
        expect(false, "https://nubulator.github.io.example.com/Chris-basement/trivia.html");
        expect(false, "https://example.com/?next=" + base + "trivia.html");
        expect(false, "https://bernie-bernie-family-card-night.nubulator.chatgpt.site.example.com/");
        expect(false, "http://nubulator.github.io/Chris-basement/trivia.html");
        expect(false, "about:blank");
        expect(false, "not a URL");
        expect(false, "");
        expect(false, null);
        System.out.println("Passed " + checks + " Game Room Back route checks.");
    }
}
