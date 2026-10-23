# Encore

[![Android TV 9+](https://img.shields.io/badge/Android%20TV%209%2B-2A1838?style=flat-square&logo=android&logoColor=FFD6A6)](#devices)
[![Home Assistant](https://img.shields.io/badge/Home%20Assistant-2A1838?style=flat-square&logo=homeassistant&logoColor=FFD6A6)](https://www.home-assistant.io)

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/mockup-now-playing.jpg">
  <img src="docs/screenshots/mockup-now-playing-light.jpg" alt="The album cover on a TV with the lyrics beside it and the progress bar across the screen">
</picture>

Encore turns your TV into a music screen for Home Assistant. It shows the album cover with the song under it, the lyrics beside it as they're sung, and a progress bar across the screen with two buttons below, one for the lyrics and one for the settings.

<a href="https://github.com/vednolacni/encore/releases/latest/download/encore.apk"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/download.png"><img src="docs/screenshots/download-light.png" width="236" alt="Download for Android TV"></picture></a>

## What it does

Encore works with any media player in Home Assistant. Music Assistant adds the lyrics, what's coming up next, and chapters for audiobooks and podcasts. Through Sendspin, the TV can also play music itself, in time with your other speakers. And if Home Assistant controls the lights behind your TV, they can take on the colors of the album cover.

During the day the background is light, and at night it turns dark.

![The same song twice, on the light background by day and on the dark one at night](docs/screenshots/day-and-night.jpg)

> [!NOTE]
> **Encore is based on Klangbild.**<br>
> It began as a fork of [Klangbild](https://github.com/hazymorning/Klangbild) by hazymorning, a calm now playing screen for the TV. Encore keeps its care for the picture and turns it into a player, with the lyrics large on screen and the controls in reach. Thanks to hazymorning for the work it builds&nbsp;on.

## More

<a name="devices"></a>
<details>
<summary><b>Compatibility</b></summary>
<br>

Encore runs on TVs and boxes with Android TV 9 or later. Since the TV can stay on for hours, the whole picture drifts slowly by a few pixels, which helps keep an OLED screen from burning in.

**Streaming sticks with Fire OS or Vega OS**

Fire OS is built on Android, and the Android version underneath most likely decides whether Encore installs. Fire OS 7 and later are built on Android 9 or newer, which should be enough, though Encore hasn't been tried on one of those sticks yet. Vega OS isn't Android at all, so Android apps most likely won't install there.

</details>

<details>
<summary><b>Setting it up</b></summary>
<br>

You need Home Assistant and a TV or box with Android TV 9 or later.

<details>
<summary><b>1. Install Encore on your TV</b></summary>
<br>

Encore isn't on Google Play, so you'll need to install the APK from the [latest release](https://github.com/vednolacni/encore/releases/latest) yourself. The easiest way is to open the release page in the free Downloader app, select the APK and allow installs from unknown sources when Android asks.

</details>

<details>
<summary><b>2. Connect it to Home Assistant</b></summary>
<br>

The first time you open Encore, it shows a QR code. Scan it with your phone, sign in to Home Assistant and choose the speaker your music plays on. Everything happens on the phone, because no one likes typing on a remote. On the same page you can add Music Assistant, the lights behind your TV and a few buttons of your own for the quick settings, but all of that can wait.

> **Important**<br>
> If Music Assistant plays your music, your speaker most likely shows up twice in the list, once from its own integration and once from Music Assistant. Choose the one marked Music Assistant, since only that one brings the lyrics and everything else Music Assistant knows about the song.

</details>

<details>
<summary><b>3. Open it automatically (optional)</b></summary>
<br>

If your TV is connected to Home Assistant through the Android Debug Bridge integration, an automation like this one opens Encore whenever your speaker starts playing:

```yaml
triggers:
  - trigger: state
    entity_id: media_player.living_room
    to: playing
actions:
  - action: media_player.select_source
    target:
      entity_id: media_player.tv
    data:
      source: io.github.vednolacni.encore
```

The two entity IDs are only examples, so put in the ones for your own speaker and TV.

</details>
<br>

</details>

<details>
<summary><b>TV remote</b></summary>
<br>

| Button | Action |
| --- | --- |
| OK | Play or pause (hold to open the settings) |
| Left / Right | Previous or next song |
| Down | Choose the buttons under the bar, and press again for the quick settings |
| Up / Back | Leave the buttons or the quick settings |

> In the settings you can change what some of the buttons do, for example to have up and down change the volume.

</details>

<details>
<summary><b>Sendspin</b></summary>
<br>

With Music Assistant 2.10.7 or later, the TV can become one of its speakers and play in time with the others over Sendspin. Once "Play here" is on, under System in the settings or with the box on the setup page, it shows up in Music Assistant under its own name and keeps playing even while Encore is closed.

</details>

<details>
<summary><b>Contributing</b></summary>
<br>

Encore is a small project made for the joy of it. Ideas and wishes are very welcome in the [Discussions](https://github.com/vednolacni/encore/discussions), and bugs in the [issues](https://github.com/vednolacni/encore/issues).

</details>

<details>
<summary><b>Data privacy</b></summary>
<br>

Encore has no accounts and doesn't track you. Your Home Assistant login is stored encrypted on the TV and never goes anywhere but your own Home Assistant.

</details>

<details>
<summary><b>License</b></summary>
<br>

Encore is released under the MIT License, like Klangbild before it, and keeps hazymorning's copyright beside its own (see [LICENSE](LICENSE)).

</details>
