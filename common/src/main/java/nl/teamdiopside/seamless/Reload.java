package nl.teamdiopside.seamless;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class Reload {

    public static void reload(ResourceManager resourceManager) {
        apply(getJsons(resourceManager));
    }

    public record OutlineRule(Set<String> blocks, HashMap<String, Set<String>> blockstates, Set<String> directions, Set<String> connectingBlocks, HashMap<String, Set<String>> connectingBlockstates, ResourceLocation location) {}

    public record JsonFile(ResourceLocation key, JsonElement json) {}

    public static final List<OutlineRule> RULES = new ArrayList<>();

    public static void apply(Map<ResourceLocation, JsonElement> jsons) {
        Seamless.fastEnabled = Seamless.fastOption.get();

        RULES.clear();
        List<OutlineRule> temp = new ArrayList<>();

        List<JsonFile> files = new ArrayList<>();
        jsons.forEach((key, json) -> {
            files.add(new JsonFile(key, json));
        });
        files.sort(Comparator.comparing(jsonFile -> jsonFile.key.toString()));

        for (JsonFile file : files) {
            ResourceLocation key = file.key();
            JsonElement json = file.json();

            if (!Seamless.modIds.contains(key.getNamespace())) {
                continue;
            }

            try {
                Set<String> blocks = getSet(json, "blocks");
                HashMap<String, Set<String>> blockstates = getBlockStates(json, "blockstates");
                Set<String> directions = getSet(json, "directions");
                Set<String> connectingBlocks = getSet(json, "connecting_blocks");
                HashMap<String, Set<String>> connectingBlockstates = getBlockStates(json, "connecting_blockstates");

                temp.add(new OutlineRule(blocks, blockstates, directions, connectingBlocks, connectingBlockstates, key));
                Seamless.LOGGER.debug("Loaded Seamless outline rule {}", key);
            } catch (Exception e) {
                Seamless.LOGGER.error("Failed to parse JSON for Seamless outline rule " + key + ".json, Error: " + e);
            }
        }

        RULES.addAll(temp);
    }

    public static Set<String> getSet(JsonElement json, String string) {
        Set<String> set = new HashSet<>();
        json.getAsJsonObject().get(string).getAsJsonArray().forEach(element -> set.add(element.getAsString()));
        return set;
    }

    public static HashMap<String, Set<String>> getBlockStates(JsonElement json, String string) {
        HashMap<String, Set<String>> blockstates = new HashMap<>();
        try {
            json.getAsJsonObject().get(string).getAsJsonObject().asMap().forEach((k, v) -> {
                Set<String> states = new HashSet<>();
                v.getAsJsonArray().forEach(jsonElement -> states.add(jsonElement.getAsString()));
                blockstates.put(k, states);
            });
        } catch (NullPointerException ignored) {

        }
        return blockstates;
    }

    public static Map<ResourceLocation, JsonElement> getJsons(ResourceManager resourceManager) {
        String directory = "seamless_rules";
        Gson gson = new Gson();
        HashMap<ResourceLocation, JsonElement> map = Maps.newHashMap();
        int i = directory.length() + 1;
        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources(directory, resourceLocation -> resourceLocation.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation resourceLocation2 = entry.getKey();
            String string = resourceLocation2.getPath();
            ResourceLocation resourceLocation22 = new ResourceLocation(resourceLocation2.getNamespace(), string.substring(i, string.length() - ".json".length()));
            try {
                BufferedReader reader = entry.getValue().openAsReader();
                try {
                    JsonElement jsonElement = GsonHelper.fromJson(gson, reader, JsonElement.class);
                    if (jsonElement != null) {
                        JsonElement jsonElement2 = map.put(resourceLocation22, jsonElement);
                        if (jsonElement2 == null) continue;
                        throw new IllegalStateException("Duplicate data file ignored with ID " + resourceLocation22);
                    }
                    Seamless.LOGGER.error("Couldn't load data file {} from {} as it's null or empty", resourceLocation22, resourceLocation2);
                } finally {
                    ((Reader)reader).close();
                }
            } catch (JsonParseException | IOException | IllegalArgumentException exception) {
                Seamless.LOGGER.error("Couldn't parse data file {} from {}", resourceLocation22, resourceLocation2, exception);
            }
        }
        return map;
    }
}
