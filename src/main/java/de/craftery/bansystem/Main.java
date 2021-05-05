package de.craftery.bansystem;

import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin {

    private static Main instance;

    private File playersDir;

    private FileConfiguration punishmentConfig;

    private Map<UUID, PlayerFile> playerFiles = new HashMap<UUID, PlayerFile>();

    @Override
    public void onEnable() {
        instance = this;

        loadCPunishmentConfig();
        loadPlayerFiles();
        registerListeners();


        System.out.println("Ban system loaded!");
        this.getCommand("bmk").setExecutor((CommandExecutor) new Commands());
        this.getCommand("unban").setExecutor((CommandExecutor) new Commands());
        this.getCommand("unmute").setExecutor((CommandExecutor) new Commands());
        this.getCommand("vz").setExecutor((CommandExecutor) new Commands());
        this.getCommand("b").setExecutor((CommandExecutor) new Commands());
        this.getCommand("m").setExecutor((CommandExecutor) new Commands());
    }

    @Override
    public void onDisable() {
        System.out.println("Ban system unloaded!");
    }

    public PlayerFile getPlayerFile(UUID uuid)
    {
        if (playerFiles.containsKey(uuid))
        {
            return playerFiles.get(uuid);
        } else {
            File file = new File(playersDir, uuid.toString() + ".yml");
            try
            {
                file.createNewFile();
            } catch (IOException e) { }

            PlayerFile playerFile = new PlayerFile(uuid, file);
            playerFiles.put(uuid, playerFile);
            return playerFile;
        }
    }

    private void loadCPunishmentConfig () {
        getConfig().addDefault("punishmentConfig.TB.type", "Ban");
        getConfig().addDefault("punishmentConfig.TB.expiration", 30000L);
        getConfig().addDefault("punishmentConfig.TM.type", "Mute");
        getConfig().addDefault("punishmentConfig.TM.expiration", 30000L);
        getConfig().addDefault("punishmentConfig.TK.type", "Kick");
        getConfig().addDefault("punishmentConfig.TK.expiration", 0);
        getConfig().addDefault("punishmentIDCounter", 0);
        getConfig().options().copyDefaults(true);
        saveConfig();
        punishmentConfig = getConfig();
    }

    public Integer makePunishmehtID () {
        Integer PunishmentID = getConfig().getInt("punishmentIDCounter", 0);
        getConfig().set("punishmentIDCounter", PunishmentID + 1);
        saveConfig();

        return PunishmentID;
    }

    public FileConfiguration getPunishmentConfig () {
        return  punishmentConfig;
    }

    private void registerListeners()
    {
        new PunishmentChecker();
    }

    private void loadPlayerFiles()
    {
        playersDir = new File(getDataFolder(), "Players");
        playersDir.mkdirs();

        for (String fileName : playersDir.list())
        {
            File file = new File(playersDir, fileName);
            UUID playerUUID = UUID.fromString(file.getName().replace(".yml", ""));

            PlayerFile playerFile = new PlayerFile(playerUUID, file);

            playerFiles.put(playerUUID, playerFile);
        }

        getLogger().info("Loaded " + playerFiles.size() + " player" + (playerFiles.size() == 1 ? "" : "s") + "!");
    }

    public static Main getInstance()
    {
        return instance;
    }
}
