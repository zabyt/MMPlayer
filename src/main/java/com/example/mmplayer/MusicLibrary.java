package com.example.mmplayer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;


public final class MusicLibrary {

    public record Track(Identifier fileId, String title, String artist, String category, boolean stream) {
    }

    private static final Map<String, String> DISC_ARTISTS = Map.ofEntries(
            Map.entry("11", "C418"), Map.entry("13", "C418"), Map.entry("5", "Samuel Åberg"),
            Map.entry("blocks", "C418"), Map.entry("bounce", "Lena Raine"), Map.entry("cat", "C418"),
            Map.entry("chirp", "C418"), Map.entry("creator", "Aaron Cherof"),
            Map.entry("creator_music_box", "Aaron Cherof"), Map.entry("far", "C418"),
            Map.entry("lava_chicken", "Hyper Potions"), Map.entry("mall", "C418"), Map.entry("mellohi", "C418"),
            Map.entry("otherside", "Lena Raine"), Map.entry("pigstep", "Lena Raine"),
            Map.entry("precipice", "Lena Raine"), Map.entry("relic", "Aaron Cherof"), Map.entry("stal", "C418"),
            Map.entry("strad", "C418"), Map.entry("tears", "Amos Roddy"), Map.entry("wait", "C418"),
            Map.entry("ward", "C418"));

    private static List<Track> ALL;

    private MusicLibrary() {
    }

    public static List<Track> all() {
        if (ALL == null) {
            ALL = build(Minecraft.getInstance());
        }
        return ALL;
    }

    private static List<Track> build(Minecraft minecraft) {
        Map<Identifier, Track> byFile = new LinkedHashMap<>();
        byFile.putAll(bakedTracks());
        if (byFile.isEmpty()) {
            MMPlayerClient.LOGGER.warn("Baked track list missing, falling back to vanilla sounds.json");
            byFile.putAll(vanillaTracks(minecraft));
        }
        List<Track> tracks = new ArrayList<>(byFile.values());
        tracks.sort((a, b) -> {
            int ca = categoryId(a.category());
            int cb = categoryId(b.category());
            return ca != cb ? Integer.compare(ca, cb)
                    : a.title().compareToIgnoreCase(b.title());
        });
        return List.copyOf(tracks);
    }

    private static Map<Identifier, Track> bakedTracks() {
        try (InputStream in = MusicLibrary.class.getResourceAsStream("/assets/mmplayer/music_tracks.json")) {
            if (in == null) {
                return Map.of();
            }
            JsonArray root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonArray();
            Map<Identifier, Track> byFile = new LinkedHashMap<>();
            for (JsonElement element : root) {
                JsonObject obj = element.getAsJsonObject();
                Identifier fileId = Identifier.withDefaultNamespace(obj.get("file").getAsString());
                boolean stream = obj.has("stream") && obj.get("stream").getAsBoolean();
                String name = obj.has("name") && obj.get("name").isJsonPrimitive()
                        ? obj.get("name").getAsString().trim()
                        : "";
                if (byFile.containsKey(fileId)) {
                    continue;
                }
                byFile.put(fileId, makeTrack(fileId, stream, name));
            }
            return byFile;
        } catch (Exception e) {
            MMPlayerClient.LOGGER.error("Could not read the baked music list", e);
            return Map.of();
        }
    }

    private static Map<Identifier, Track> vanillaTracks(Minecraft minecraft) {
        Map<Identifier, Track> byFile = new LinkedHashMap<>();
        try {
            Resource resource = minecraft.getResourceManager().getResourceOrThrow(
                    Identifier.withDefaultNamespace("sounds.json"));
            try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                for (Map.Entry<String, JsonElement> eventEntry : root.entrySet()) {
                    if (!eventEntry.getKey().startsWith("music")) {
                        continue;
                    }
                    JsonObject definition = eventEntry.getValue().getAsJsonObject();
                    if (!definition.has("sounds") || !definition.get("sounds").isJsonArray()) {
                        continue;
                    }
                    JsonArray sounds = definition.getAsJsonArray("sounds");
                    for (JsonElement soundElement : sounds) {
                        if (soundElement.isJsonArray()) {
                            continue;
                        }
                        String name;
                        boolean stream;
                        String type;
                        if (soundElement.isJsonObject()) {
                            JsonObject soundObj = soundElement.getAsJsonObject();
                            if (!soundObj.has("name")) {
                                continue;
                            }
                            name = soundObj.get("name").getAsString();
                            stream = soundObj.has("stream") && soundObj.get("stream").getAsBoolean();
                            type = soundObj.has("type") ? soundObj.get("type").getAsString() : "file";
                        } else {
                            name = soundElement.getAsString();
                            stream = false;
                            type = "file";
                        }
                        if (!type.equals("file")) {
                            continue;
                        }
                        if (name.endsWith(".ogg")) {
                            name = name.substring(0, name.length() - 4);
                        }
                        Identifier fileId = Identifier.fromNamespaceAndPath(Identifier.DEFAULT_NAMESPACE, name);
                        if (byFile.containsKey(fileId)) {
                            continue;
                        }
                        byFile.put(fileId, makeTrack(fileId, stream, ""));
                    }
                }
            }
        } catch (Exception e) {
            MMPlayerClient.LOGGER.error("Could not read vanilla music list", e);
        }
        return byFile;
    }

    private static Track makeTrack(Identifier fileId, boolean stream, String bakedName) {
        String path = fileId.getPath();
        if (path.startsWith("records/")) {
            String raw = path.substring("records/".length());
            String title = raw.equals("11") || raw.equals("13") || raw.equals("5") ? raw
                    : titleCase(raw.replace('_', ' '));
            String artist = DISC_ARTISTS.getOrDefault(raw, "Unknown Artist");
            return new Track(fileId, title, artist, "Music Disc", stream);
        }
        if (bakedName != null && !bakedName.isEmpty()) {
            int split = bakedName.indexOf(" - ");
            if (split > 0) {
                return new Track(fileId, bakedName.substring(split + 3), bakedName.substring(0, split),
                        categoryOf(path), stream);
            }
            return new Track(fileId, bakedName, "", categoryOf(path), stream);
        }
        String key = fileId.toShortLanguageKey();
        String translated = Component.translatable(key).getString();
        if (!translated.equals(key) && translated.contains(" - ")) {
            int split = translated.indexOf(" - ");
            return new Track(fileId, translated.substring(split + 3), translated.substring(0, split),
                    categoryOf(path), stream);
        }
        return new Track(fileId, titleCase(path.substring(path.lastIndexOf('/') + 1).replace('_', ' ')),
                "", categoryOf(path), stream);
    }

    private static int categoryId(String category) {
        String[] order = {
                "Music Disc", "Overworld", "Creative", "Menu", "Nether", "Underwater", "Boss", "End", "Credits"
        };
        for (int i = 0; i < order.length; i++) {
            if (order[i].equals(category)) {
                return i;
            }
        }
        return 99;
    }

    private static int categoryId(Track track) {
        return categoryId(track.category());
    }

    private static String categoryOf(String path) {
        if (path.startsWith("music/menu/")) {
            return "Menu";
        }
        if (path.startsWith("music/game/creative/")) {
            return "Creative";
        }
        if (path.startsWith("music/game/end/boss")) {
            return "Boss";
        }
        if (path.startsWith("music/game/end/alpha")) {
            return "Credits";
        }
        if (path.startsWith("music/game/end/")) {
            return "End";
        }
        if (path.startsWith("music/game/nether/")) {
            return "Nether";
        }
        if (path.startsWith("music/game/water/")) {
            return "Underwater";
        }
        return "Overworld";
    }

    private static String titleCase(String text) {
        StringBuilder out = new StringBuilder();
        for (String word : text.split(" ")) {
            if (word.isEmpty()) {
                continue;
            }
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase(Locale.ROOT));
        }
        return !out.isEmpty() ? out.toString() : text;
    }

    public static List<Track> filter(String query) {
        String q = query.toLowerCase(Locale.ROOT).trim();
        if (q.isEmpty()) {
            return all();
        }
        List<Track> matches = new ArrayList<>();
        for (Track track : all()) {
            String combined = (track.title() + " " + track.artist() + " " + track.fileId().getPath()).toLowerCase(Locale.ROOT);
            if (combined.contains(q)) {
                matches.add(track);
            }
        }
        return matches;
    }
}
