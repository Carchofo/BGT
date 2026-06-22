# BGT — Board Game Tools

Free Android assistant for solo board gaming.

## What it does

BGT simplifies playing solo by managing bot opponents, tracking scores, and putting fan-made solo modes in one place. No ads, no subscriptions, always free.

**Games included:**
| Game | Mode | Bot |
|------|------|-----|
| Maracaibo | Solo vs Jordán | Official (A. Pfister) |
| Tiletum | Solo vs Titus | Official (D. Turczi) |
| Criaturas Maravillosas | Solo vs Tingent | Official (D. Turczi) |
| Castle Combo | Solo vs Anton | Fan-made (ben_uez, BGG) |
| Coimbra | Interference Bot | Fan-made (skybowl, BGG) |
| Cascadia | Score calculator | — |
| Spooktacular | Solo vs Killtron | Official (Samaruc Games) |

## Install

1. Go to [Releases](https://github.com/Carchofo/BGT/releases/latest)
2. Download `BGT-vX.X.apk`
3. On Android: Settings → Install unknown apps → enable for your browser/file manager
4. Open the APK and install

The app checks for updates automatically on launch.

## Build

Requirements: JDK 17, Android SDK 35

```bash
git clone https://github.com/Carchofo/BGT.git
cd BGT
./gradlew assembleDebug
```

For signed release builds, create `keystore.properties` (see `keystore.properties.example` if present) and run `./gradlew assembleRelease`.

## Release process

Push a version tag to trigger an automated build + signed APK release:

```bash
git tag v1.2
git push origin v1.2
```

GitHub Actions builds, signs, and publishes the APK to Releases automatically.

## Support the project

BGT is built in spare time, with a newborn at home. If it's useful, [buy me a coffee on Ko-fi](https://ko-fi.com/bgtapp) ☕

## Credits

Solo modes are based on official rules and fan-made variants published freely on BoardGameGeek. All credit to their authors.

## License

MIT — use and modify freely, don't sell the app as-is.
