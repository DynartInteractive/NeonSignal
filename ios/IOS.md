# iOS Platform Setup Guide

This document covers the setup and deployment of Neon Signal on iOS using LibGDX + RoboVM (MobiVM).

## Prerequisites

- **macOS** with **Xcode** installed
- A valid **Apple Developer account**
- **JDK 17+**
- For device deployment: iPhone connected via USB with **Developer Mode** enabled

## Signing Certificate Setup

Before deploying to a physical device, you need an Apple Development signing certificate:

1. Open **Xcode > Settings > Accounts**
2. Add your Apple ID if not already present
3. Select your team, click **Manage Certificates...**
4. Click **+** > **Apple Development**

Verify the certificate is installed:
```bash
security find-identity -v -p codesigning
```

If it shows `0 valid identities found` even after creating the certificate in Xcode, the private key may not be linked properly. The most reliable fix:

1. Create a dummy iOS project in Xcode (File > New > Project > iOS App)
2. Select your team under Signing & Capabilities with "Automatically manage signing" checked
3. Build it (Cmd+B) targeting your connected iPhone
4. This forces Xcode to properly install the certificate + private key pair
5. Verify again with `security find-identity -v -p codesigning`
6. Delete the dummy project

The signing identity is configured in `ios/robovm.properties`:
```
ios.signingIdentity=Apple Development: Your Name (XXXXXXXXXX)
```

## Xcode Command Line Tools

Ensure the Xcode command line tools point to the full Xcode installation, not the standalone CLI tools:
```bash
sudo xcode-select -switch /Applications/Xcode.app/Contents/Developer
```

## Graphics Backend: MetalANGLE

Apple has deprecated and removed OpenGL ES on recent iOS versions. This project uses the **MetalANGLE** backend (`gdx-backend-robovm-metalangle`) instead of the standard RoboVM backend. MetalANGLE translates OpenGL calls into Metal, Apple's supported graphics API.

The dependency in `build.gradle`:
```groovy
api "com.badlogicgames.gdx:gdx-backend-robovm-metalangle:$gdxVersion"
```

The `ios/robovm.xml` frameworks list includes `Metal` and `MetalKit` instead of `OpenGLES`.

## Building and Deploying

### Simulator
```bash
./gradlew ios:launchIPhoneSimulator
```

### Physical Device (IPA method)

RoboVM's `launchIOSDevice` task uses the legacy `DeveloperDiskImage.dmg` approach, which Apple removed in iOS 17+. For modern iOS versions, build an IPA and install manually:

1. Build the IPA:
   ```bash
   ./gradlew ios:createIPA
   ```

2. Install via Xcode:
   - Open **Window > Devices and Simulators**
   - Select your iPhone
   - Drag the `.ipa` file onto the device

   Or use `ios-deploy`:
   ```bash
   brew install ios-deploy
   ios-deploy --bundle build/robovm/IOSDevice-arm64/NeonSignal.app
   ```

3. On first install, trust the developer on your iPhone:
   **Settings > General > VPN & Device Management** > tap your developer certificate > Trust

## RoboVM API Limitations

RoboVM has an incomplete JDK runtime. The following Java APIs are **not available** and will crash at runtime:

| API | Alternative |
|-----|-------------|
| `java.nio.file.*` (`Paths`, `Path`) | `Gdx.files` API |
| `java.net.URL` (HTTPS) | Avoid or guard with platform check |
| `Map.getOrDefault()` | `Map.get()` with null check |
| `List.of()`, `Set.of()`, `Map.of()` (Java 9+) | `Arrays.asList()`, `new HashSet<>()` |
| `String.isBlank()`, `String.strip()` (Java 11+) | `String.trim()`, `String.isEmpty()` |

Analytics (`AnalyticsManager`) is disabled on iOS because `Gdx.net` HTTP requests crash due to the missing HTTPS handler.

## Configuration Files

- `ios/build.gradle` — module build config
- `ios/robovm.xml` — RoboVM compiler config (frameworks, force-linked classes, resources)
- `ios/robovm.properties` — app metadata (bundle ID, signing identity, version)
- `ios/Info.plist.xml` — iOS app configuration (orientations, launch screen, device families)
- `ios/src/net/dynart/neonsignal/IOSLauncher.java` — app entry point

## Version History

- **RoboVM**: 2.3.24 (MobiVM fork)
- **LibGDX**: 1.13.5
- **Backend**: `gdx-backend-robovm-metalangle`
- **Architecture**: arm64 only (32-bit armv7 removed)
