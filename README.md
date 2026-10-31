# MMPlayer

A music player for every track in vanilla Minecraft. Press M to open.

A client-side Fabric mod for Minecraft 26.2 that turns the whole vanilla soundtrack into a searchable in-game music player.

## Features

- Press **M** (rebindable) to open the music player menu anywhere in the game
- Lists every individual piece of vanilla music by its real name: game music, biome music, menu music, music discs and more - shown like the vanilla music toast, e.g. "Sweden - C418", "Pigstep - Lena Raine"
- Search bar to filter by title, artist or sound ID
- Grouped list: Music Discs, Overworld, Creative, Menu, Nether, Underwater, Boss, End, Credits
- Click a track to play it, click again to stop; there is also a Stop button
- Currently playing track is highlighted and shown in a "Now playing" bar
- Plays through the vanilla sound engine in the music category, so the music volume slider still works, and any vanilla song playing is stopped first so tracks never overlap
- Music keeps playing when you leave the menu, like a real player
- Own music volume slider in the settings, independent from the vanilla music level
- Track list is based on the game's own sounds.json data and is baked into the mod jar, so song names and tracks stay correct on every setup

## Configuration

In-game settings volume slider is available through [ModMenu](https://modrinth.com/mod/modmenu), or through the icon added by [ZabytLib](https://github.com/zabyt/ZabytLib) on the title/pause screen.

## Building

The clone is self-contained: every jar needed for compilation (ZabytLib, ModMenu) is in `libs/`.

```
git clone https://github.com/zabyt/MMPlayer
cd MMPlayer
gradlew.bat build
```

The built jar ends up in `build/libs/`.

## Dependencies

- Minecraft `~26.2`, Fabric Loader `>=0.19.3`, Java `>=25`, Fabric API
- [ZabytLib](https://github.com/zabyt/ZabytLib) at runtime
- [ModMenu](https://modrinth.com/mod/modmenu) at runtime

## License

MIT
