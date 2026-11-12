# Chris' Basement TV — Fire TV app

This is an Android TV/Fire TV application project. It loads the live Chris' Basement dashboard in a fullscreen WebView, supports Fire TV remote navigation, and keeps the display awake while open.

## Build without installing Android Studio
1. Create a new **private** GitHub repository (for example, `Chris-Basement-TV-App`).
2. Upload the **contents** of this folder to the repository root, including the `.github/workflows/build-apk.yml` file. (GitHub's web uploader may hide dotfolders; check that the workflow file is present.)
3. Open **Actions > Build Fire TV APK > Run workflow**, or push to the `main` branch.
4. When the workflow completes, open its run, download the `Chris-Basement-TV-APK` artifact, and unzip it to get `app-debug.apk`.
5. Transfer the APK to your Fire TV using a suitable app or ADB and install it after enabling developer options and permission to install from the chosen source.
6. Launch **Chris' Basement TV** from Fire TV's apps list.

## Build in Android Studio
Open this folder in Android Studio with Android SDK 35 and Java 17, let Gradle sync, then use **Build > Build APK(s)**. The generated debug APK is `app/build/outputs/apk/debug/app-debug.apk`.

## Controls
- Fire TV directional/select remote controls navigate the website where supported by the page.
- Back from a Game Room game returns directly to Chris's Game Room, including Bernie Bernie after its redirect. Holding Back does not also open the dashboard picker.
- Back on a dashboard closes an expanded radar first; otherwise it opens Choose dashboard. Back from the Game Room landing page also opens Choose dashboard.
- The Menu button (three horizontal lines) opens Choose dashboard from any page.

## Version 2.1
This APK changes the native remote Back behavior. Install the new APK on the Fire TV to receive this fix; refreshing the website alone cannot update the installed app's key handling. No game records or website content are changed.

### Navigation regression check (Java 17 or newer)
```sh
javac --release 17 -d build/navigation-test app/src/main/java/com/chrisbasement/tv/GameRoomNavigation.java tests/GameRoomNavigationTest.java
java -cp build/navigation-test com.chrisbasement.tv.GameRoomNavigationTest
```

On a Fire TV, open each game from the Game Room and press Back once, then test holding Back. Both should leave the Game Room visible. Check that Menu still opens the dashboard picker and Back still closes expanded radar on the dashboard.

## Dashboard updates
This version loads `https://nubulator.github.io/Chris-basement/index-v3822.html`. To update to a new numbered HTML page, change `DASHBOARD_URL` in `app/src/main/java/com/chrisbasement/tv/MainActivity.java` and rebuild the APK. For future no-reinstall updates, point this constant at a stable URL such as `https://nubulator.github.io/Chris-basement/index.html` and update that HTML file in the existing dashboard repo. Do not switch until that stable URL exists.

## Notes
- The app requires internet access and relies on Fire TV's Android System WebView, which may behave differently from Silk. Test live scores, radar, and remote selection on the actual device.
- This is a private, sideloadable debug APK, not a published Amazon Appstore release.
- GitHub Actions compiles the APK. Remote-button behavior still needs a check on the actual Fire TV after installation.

