package com.winthier.minilink.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;

public class Configure {
    public static final String PLAYERS_SECTION = "players";
    public static final String UUID_KEY = "UUID";
    public static final String GAME_KEY = "GameKey";
    public static final String GAME_NAME = "Game";
    public static final String RESULT_KEY = "Result";
    public static final String MESSAGE_KEY = "Message";
    public static final String MAX_PLAYERS = "MaxPlayers";
    public static final String MIN_PLAYERS = "MinPlayers";
    public static final String SHOULD_ANNOUNCE = "ShouldAnnounce";

    // Players

    public static void storePlayers(ConfigurationSection config, List<PlayerInfo> players) {
        ConfigurationSection section = config.getConfigurationSection(PLAYERS_SECTION);
        if (section == null) section = config.createSection(PLAYERS_SECTION);
        for (PlayerInfo player : players) {
            section.set(player.getUniqueId().toString(), player.getName());
        }
    }

    public static List<PlayerInfo> loadPlayers(ConfigurationSection config) {
        List<PlayerInfo> result = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection(PLAYERS_SECTION);
        if (section == null) return result;
        for (String key : section.getKeys(false)) {
            final UUID uuid = UUID.fromString(key);
            final String name = section.getString(key);
            result.add(PlayerInfo.fromInfo(uuid, name));
        }
        return result;
    }

    // UUID

    public static void storeUuid(ConfigurationSection config, UUID uuid) {
        config.set(UUID_KEY, uuid.toString());
    }

    public static UUID loadUuid(ConfigurationSection config) {
        return UUID.fromString(config.getString(UUID_KEY));
    }

    // Game Config

    public static void storeGameConfig(ConfigurationSection config, ConfigurationSection gameConfig) {
        for (String key : gameConfig.getKeys(false)) {
            config.set(key, gameConfig.get(key));
        }
    }

    // Game Key

    public static void storeGameKey(ConfigurationSection config, String gameKey) {
        config.set(GAME_KEY, gameKey);
    }

    public static String loadGameKey(ConfigurationSection config) {
        return config.getString(GAME_KEY);
    }

    // Game Name

    public static void storeGameName(ConfigurationSection config, String gameName) {
        config.set(GAME_NAME, gameName);
    }

    public static String loadGameName(ConfigurationSection config) {
        return config.getString(GAME_NAME);
    }

    // Result and message

    public static void storeResultAndMessage(ConfigurationSection config, boolean result, String message) {
        config.set(RESULT_KEY, result);
        config.set(MESSAGE_KEY, message);
    }

    // Result

    public static void storeResult(ConfigurationSection config, boolean result) {
        config.set(RESULT_KEY, result);
    }

    public static boolean loadResult(ConfigurationSection config) {
        return config.getBoolean(RESULT_KEY);
    }

    // Result

    public static void storeMessage(ConfigurationSection config, String message) {
        config.set(MESSAGE_KEY, message);
    }

    public static String loadMessage(ConfigurationSection config) {
        return config.getString(MESSAGE_KEY);
    }

    // Player Count

    public static void storeMaxPlayers(ConfigurationSection config, int maxPlayers) {
        config.set(MAX_PLAYERS, maxPlayers);
    }

    public static int loadMaxPlayers(ConfigurationSection config) {
        return config.getInt(MAX_PLAYERS);
    }

    public static void storeMinPlayers(ConfigurationSection config, int minPlayers) {
        config.set(MIN_PLAYERS, minPlayers);
    }

    public static int loadMinPlayers(ConfigurationSection config) {
        return config.getInt(MIN_PLAYERS);
    }

    // Should Announce

    public static void storeShouldAnnounce(ConfigurationSection config, boolean shouldAnnounce) {
        config.set(SHOULD_ANNOUNCE, shouldAnnounce);
    }

    public static boolean loadShouldAnnounce(ConfigurationSection config) {
        return config.getBoolean(SHOULD_ANNOUNCE);
    }
}
