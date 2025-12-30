# Launcher Icons

The Android app requires launcher icons in various sizes. Since we cannot create actual image files in this environment, here's what needs to be added:

## Required Icons

Place icons in the following directories with the specified sizes:

- `mipmap-mdpi/ic_launcher.png` - 48x48 pixels
- `mipmap-hdpi/ic_launcher.png` - 72x72 pixels
- `mipmap-xhdpi/ic_launcher.png` - 96x96 pixels
- `mipmap-xxhdpi/ic_launcher.png` - 144x144 pixels
- `mipmap-xxxhdpi/ic_launcher.png` - 192x192 pixels

For round icons (Android 7.1+):
- `mipmap-mdpi/ic_launcher_round.png` - 48x48 pixels
- `mipmap-hdpi/ic_launcher_round.png` - 72x72 pixels
- `mipmap-xhdpi/ic_launcher_round.png` - 96x96 pixels
- `mipmap-xxhdpi/ic_launcher_round.png` - 144x144 pixels
- `mipmap-xxxhdpi/ic_launcher_round.png` - 192x192 pixels

## Design Recommendations

The icon should:
- Feature a Bluetooth symbol or ESP32 chip graphic
- Use the app's color scheme (Material Design 3 primary color)
- Be simple and recognizable at small sizes
- Follow Material Design icon guidelines

## Generating Icons

You can use these tools to generate launcher icons:

1. **Android Studio**: Tools → Image Asset → Launcher Icons
2. **Online Tools**: 
   - https://romannurik.github.io/AndroidAssetStudio/
   - https://appicon.co/
3. **Figma/Sketch**: Design and export at multiple resolutions

## Temporary Workaround

Until proper icons are added, the app will use the default Android robot icon or may appear blank on some devices. This doesn't affect functionality but should be addressed before publishing.
