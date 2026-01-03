# IQ Master - Android Quiz App

A modern Android quiz application built with Jetpack Compose, Firebase Firestore for cloud sync, and Google UMP for GDPR-compliant ad consent management.

## Features

- 📱 **Jetpack Compose UI** - Modern, declarative UI framework
- 🔥 **Firebase Integration** - Anonymous authentication and Firestore cloud sync
- 📊 **High Score Tracking** - Local and cloud-synced leaderboards
- 🎯 **AdMob Integration** - Banner and interstitial ads with consent management
- 🛡️ **UMP Consent** - GDPR-compliant User Messaging Platform integration
- 🔐 **Privacy First** - Complete privacy policy and consent management
- 🧪 **Unit Tests** - Comprehensive testing for core functionality
- 🔒 **ProGuard/R8** - Code shrinking and obfuscation for release builds

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/iqmaster/
│   │   │   ├── MainActivity.kt                 # Main entry point
│   │   │   ├── data/
│   │   │   │   ├── model/                      # Data models
│   │   │   │   ├── local/                      # Local storage
│   │   │   │   └── repository/                 # Repositories
│   │   │   ├── consent/                        # UMP consent management
│   │   │   ├── ads/                            # AdMob integration
│   │   │   └── ui/
│   │   │       ├── screens/                    # Compose screens
│   │   │       ├── theme/                      # Material3 theming
│   │   │       └── navigation/                 # Navigation setup
│   │   ├── res/
│   │   │   ├── raw/privacy_policy.md           # Local privacy policy
│   │   │   └── values/strings.xml              # String resources
│   │   └── AndroidManifest.xml
│   ├── test/                                   # Unit tests
│   └── androidTest/                            # Instrumented tests
├── build.gradle                                 # App-level build config
├── proguard-rules.pro                          # ProGuard rules
└── google-services.json                        # Firebase config (already present)
```

## Setup Instructions

### 1. Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK with minimum API level 24

### 2. Firebase Configuration

The project already includes `google-services.json` in the `app/` directory with the following configuration:
- **Project ID**: `iqmaster-9a3a0700`
- **Package Name**: `com.example.iqmaster`

**No additional Firebase setup is needed** - the file is already configured and in place.

#### Firestore Security Rules

Add these security rules to your Firebase Firestore console:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only access their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Allow users to access their own scores
      match /scores/{scoreId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      // Allow users to access their own consent records
      match /consent/{consentId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

### 3. Release Signing Configuration

To build a signed release APK/AAB, you need to create a keystore and configure signing.

#### Generate a Keystore

Run this command in your terminal:

```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

You'll be prompted for:
- Keystore password
- Key password
- Your name and organization details

#### Configure Signing in gradle.properties

Add these lines to `gradle.properties` (or create `keystore.properties`):

```properties
RELEASE_STORE_FILE=/path/to/my-release-key.jks
RELEASE_STORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=my-key-alias
RELEASE_KEY_PASSWORD=your_key_password
```

**Important**: 
- Never commit `keystore.properties` or the `.jks` file to version control
- These files are already excluded in `.gitignore`
- Store your keystore securely - losing it means you cannot update your app

### 4. UMP Debug Mode

The app includes UMP debug settings for testing consent flows in any geography.

#### Enable Debug Mode

1. **Get your test device hashed ID** from Logcat when you first run the app:
   ```
   UMP Debug Mode: Test device added: YOUR_HASHED_ID
   ```

2. **Add to build.gradle** (already configured in debug build):
   ```gradle
   buildConfigField "String", "TEST_DEVICE_HASHED_ID", '"YOUR_HASHED_ID"'
   ```

3. **Test in EEA geography**: Debug mode automatically simulates EEA geography

#### Testing Consent Flow

1. Run the app in debug mode
2. The UMP consent form will appear automatically
3. To reset consent: Go to Settings → Reset Ads Choice
4. You can also use the "Change Ad Preferences" button

### 5. AdMob Configuration

The app uses **test ad unit IDs** by default. Before releasing to production:

1. Create an AdMob account at https://apps.admob.com/
2. Create an app in AdMob console
3. Get your real ad unit IDs
4. Replace the test IDs in `strings.xml`:

```xml
<!-- Replace with your real AdMob IDs -->
<string name="banner_ad_unit_id">ca-app-pub-XXXXX/YYYYY</string>
<string name="interstitial_ad_unit_id">ca-app-pub-XXXXX/ZZZZZ</string>
```

5. Update `AndroidManifest.xml` with your AdMob App ID:

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-XXXXX~YYYYY"/>
```

### 6. Building the App

#### Debug Build

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

#### Release Build

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

#### Bundle for Play Store

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

### 7. Running Tests

#### Unit Tests

```bash
./gradlew test
```

#### Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

## Play Store Submission Checklist

Before submitting to Google Play Store:

- [ ] Replace test AdMob IDs with production IDs
- [ ] Configure signing with your release keystore
- [ ] Update app version in `build.gradle`
- [ ] Test UMP consent flow in production
- [ ] Verify privacy policy URL is accessible
- [ ] Test with real Firebase project
- [ ] Enable app signing by Google Play (recommended)
- [ ] Complete Play Console privacy section
- [ ] Declare ads and data collection in Play Console
- [ ] Test release build thoroughly
- [ ] Review Firestore security rules
- [ ] Set up Firebase Authentication properly
- [ ] Configure ProGuard rules if needed

## Privacy Policy

The app includes both hosted and local privacy policies:

- **Hosted**: https://adesvirus.github.io/IQ-Master/privacy_policy.html
- **Local**: Bundled in `res/raw/privacy_policy.md`

Both policies explain:
- Data collection practices
- Ad personalization and consent
- Firebase Firestore usage
- User rights and choices

## Development Notes

### Consent Management

- UMP consent is checked on app start
- Consent choices are persisted locally and synced to Firestore
- Users can revoke consent and change preferences anytime
- "No Ads" option disables all ads in the app

### High Score Sync

- Scores are saved locally first (offline support)
- Cloud sync happens when network is available
- Merge strategy: combine local + remote, keep top 20 by score
- Users can clear local or remote scores independently

### Ads Strategy

- Banner ad shown at bottom of quiz screen
- Interstitial ads show randomly (~30% chance) between questions
- Ads respect consent choices (personalized/non-personalized/no-ads)
- `npa=1` parameter added for non-personalized ads

## Troubleshooting

### Firebase Authentication Failed
- Check that `google-services.json` is in the `app/` directory
- Verify package name matches: `com.example.iqmaster`
- Ensure Firebase project is configured correctly

### UMP Consent Form Not Showing
- Enable debug mode in build.gradle
- Add your test device hashed ID
- Check Logcat for UMP messages
- Verify internet connectivity

### Build Errors
- Run `./gradlew clean`
- Sync Gradle files
- Invalidate caches and restart Android Studio
- Check that all dependencies are compatible

### ProGuard Issues
- Review `app/proguard-rules.pro`
- Add keep rules for any missing classes
- Test release build thoroughly

## Architecture

The app follows modern Android architecture guidelines:

- **UI Layer**: Jetpack Compose screens with Material3
- **Data Layer**: Repositories pattern with local and remote sources
- **Firebase**: Anonymous auth for user identification, Firestore for cloud storage
- **Dependency Injection**: Manual DI in MainActivity (can be migrated to Hilt)

## Contributing

This is a sample project demonstrating:
- Jetpack Compose best practices
- Firebase integration
- UMP consent management
- AdMob implementation
- ProGuard configuration

## License

This project is provided as-is for educational purposes.

## Contact

For questions or support:
- Email: adesthebest@gmail.com
- GitHub: [AdesVirus/IQ-Master](https://github.com/AdesVirus/IQ-Master)

## Acknowledgments

- Built with Jetpack Compose
- Firebase for backend services
- Google Mobile Ads SDK
- User Messaging Platform (UMP)
