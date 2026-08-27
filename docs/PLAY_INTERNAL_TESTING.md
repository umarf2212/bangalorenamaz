# Play internal testing handoff

Use a Google Play internal testing track for this phone. It installs through the normal Play Store, so it avoids the “unknown app” and sideload path that Samsung Auto Blocker and Play Protect object to. It does **not** override an enterprise device policy: if the device administrator blocks unapproved personal-profile apps, IT still has the final word.

## 0. Confirm the permanent identity

The current application ID is `one.umar.namazrings`. Google Play fixes the package name after the first artifact upload. Change `applicationId` and `namespace` in `app/build.gradle.kts`, plus the Kotlin package folders, **before** the first upload if you prefer another identifier. The visible name “Namaz Rings” can be changed later.

The build targets API 36 so a new upload remains eligible after Google Play's 31 August 2026 target-API deadline.

## 1. Create and protect an upload key

For a new Play app, let Play App Signing manage the app-signing key and use a separate local upload key.

On macOS, the included helper creates a 4096-bit upload key, generates a random password, and stores the password in your login Keychain rather than a plaintext project file:

```bash
./scripts/create-upload-key.sh
./scripts/build-signed-release.sh
```

Back up `namaz-rings-upload.jks` somewhere private. The password is stored under the macOS Keychain service `one.umar.namazrings.upload`.

The manual cross-platform fallback is to copy `keystore.properties.example` to the ignored `keystore.properties` file and fill it in:

```properties
storeFile=/a/private/backup/location/namaz-rings-upload.jks
storePassword=your-store-password
keyAlias=upload
keyPassword=your-key-password
```

Back up the keystore and passwords somewhere private. Do not commit either file.

Build and verify the signed bundle manually:

```bash
./gradlew clean testDebugUnitTest bundleRelease
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
```

Upload `app/build/outputs/bundle/release/app-release.aab`, not the debug APK and not an unsigned bundle.

## 2. Create the Play app

1. Open [Play Console](https://play.google.com/console/), choose **Create app**, and use “Namaz Rings” as the name.
2. Choose **App**, **Free**, enter the contact email, accept the declarations, and accept Play App Signing.
3. Keep the Google-generated app-signing key. The local key above is only the upload key.

An internal test can start before the public store listing is complete. Google Play may show a temporary app name/store page for the first test release.

## 3. Publish the internal release

1. Go to **Test and release → Testing → Internal testing**.
2. Open **Testers**, create an email list, and add the Google account used by the Play Store in the phone's **personal profile**. Save and select that list.
3. Open **Releases**, create a new internal release, and upload the signed `.aab`.
4. Add a short release note, review the release, and start the rollout to internal testing.
5. Return to **Testers**, copy the opt-in link, and open it on the phone with the same allow-listed Google account.
6. Tap **Become a tester**, then **Download it on Google Play** and install normally from the Play Store.

The first link/release can take several minutes to propagate. Internal testing supports up to 100 testers.

## 4. Add the widget

Open Namaz Rings and tap **Add widget to home screen**, then approve the launcher prompt. If Samsung's launcher does not offer that prompt, long-press the home screen, choose **Widgets → Namaz Rings**, and drag the 4×2 widget into place.

## Managed Samsung troubleshooting

- Use the Play Store and browser in the personal profile, without the briefcase badge. A work-profile Play Store only exposes apps approved by the organization.
- Do not disable Auto Blocker or Play Protect for this flow; the install should be Play-delivered and Play-signed.
- If the page says the app is unavailable, confirm the tester email exactly matches the Play Store account, the tester list is selected and saved, the tester opted in, and the release rollout finished.
- If Android explicitly says **Blocked by your admin**, the internal track cannot bypass that policy. Ask IT to allow the Play package `one.umar.namazrings` in the personal profile, or to approve it through Managed Google Play if installation must happen in the work profile.
- When shipping an update, increment `versionCode` in `app/build.gradle.kts`, rebuild the signed bundle with the same upload key, and create another internal release.

## Official references

- [Set up an internal test](https://support.google.com/googleplay/android-developer/answer/9845334)
- [Use Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756)
- [Sign an Android app bundle](https://developer.android.com/studio/publish/app-signing)
- [Google Play target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878)
