package de.craftery.bansystem;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerFile
{
    private UUID id;
    private String ip;

    private File file;
    private YamlConfiguration config;

    private static final String HISTORY_FIELD = "history";
    private static final String NUM_OF_PREVIOUS_BANS = "offenses.bans";

    private long banExpiration;
    private String banReason;
    private String banID;
    private long muteExpiration;
    private String muteReason;
    private String muteID;
    private Map<Long, Map<String, String>> history = new HashMap<Long, Map<String, String>>();

    public PlayerFile(UUID id, File file)
    {
        this.id = id;
        this.config = YamlConfiguration.loadConfiguration(file);
        this.file = file;

        banExpiration = config.getLong("expiration.ban", 0L);
        banReason = config.getString("banReason", "Grundlos ^^");
        banID = config.getString("banID", "Ban ID nicht angegeben");
        muteExpiration = config.getLong("expiration.mute", 0L);
        muteReason = config.getString("muteReason", "Grundlos ^^");
        muteID = config.getString("muteID", "Mute ID nicht angegeben");

        history = getInitialHistory();

    }

    public void setBanExpiration(long expiration)
    {
        banExpiration = expiration;
    }

    public Long getBanExpiration()
    {
        return banExpiration;
    }

    public void setBanReason(String reason)
    {
        banReason = reason;
    }

    public String getBanReason()
    {
        return banReason;
    }

    public void setBanID(String id)
    {
        banID = id;
    }

    public String getBanID()
    {
        return banID;
    }



    public void setMuteExpiration(long expiration)
    {
        muteExpiration = expiration;
    }

    public Long getMuteExpiration()
    {
        return muteExpiration;
    }

    public void setMuteReason(String reason)
    {
        muteReason = reason;
    }

    public String getMuteReason()
    {
        return muteReason;
    }

    public void setMuteID(String id)
    {
        muteID = id;
    }

    public String getMuteID()
    {
        return muteID;
    }

    public void addHistory(String type, String reason, String ID, Long expiration, String givenBy, Long givenAt) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(givenAt);
        final String timeString =
                new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(cal.getTime());
        Map<String, String> HistoryEntry = new HashMap<String, String>();
        HistoryEntry.put("givenAt", timeString);
        HistoryEntry.put("type", type);
        HistoryEntry.put("reason", reason);
        HistoryEntry.put("id", ID);
        HistoryEntry.put("expiration", expiration + "");
        HistoryEntry.put("givenBy", givenBy);
        HistoryEntry.put("removed", "false");
        HistoryEntry.put("removedBy", "");
        HistoryEntry.put("removedReason", "");
        history.put(expiration, HistoryEntry);
    }

    private Map<Long, Map<String, String>> getInitialHistory () {
        Map<Long, Map<String, String>> histoerie = new HashMap<Long, Map<String, String>>();

        if (config.contains("history")) {
            ConfigurationSection historySection = config.getConfigurationSection("history");

            for (String key : historySection.getKeys(false))
            {
                Map<String, String> HistoryEntry = new HashMap<String, String>();
                HistoryEntry.put("givenAt", config.getString("history." + key + ".givenAt"));
                HistoryEntry.put("type", config.getString("history." + key + ".type"));
                HistoryEntry.put("reason", config.getString("history." + key + ".reason", "No reason given"));
                HistoryEntry.put("id", config.getString("history." + key + ".id", "No id given"));
                HistoryEntry.put("expiration", config.getString("history." + key + ".expiration"));
                HistoryEntry.put("givenBy", config.getString("history." + key + ".givenBy", "No sender"));
                HistoryEntry.put("removed", config.getBoolean("history." + key + ".removed", false) ? "true" : "false");
                HistoryEntry.put("removedBy", config.getString("history." + key + ".removedBy", ""));
                HistoryEntry.put("removedReason", config.getString("history." + key + ".removedReason", "No sender"));
                histoerie.put(Long.parseLong(key), HistoryEntry);
            }
        }

        return histoerie;
    }

    public Boolean removeHistory(Long expiration, String removedBy, String removedReason) {
        if (history.containsKey(expiration)) {
            Map<String, String> HistoryEntry = history.get(expiration);
            HistoryEntry.replace("removed", "true");
            HistoryEntry.replace("removedBy", removedBy);
            HistoryEntry.replace("removedReason", removedReason);
            history.replace(expiration, HistoryEntry);
            return true;
        } else {
            return false;
        }
    }

    public Map<Long, Map<String, String>> getHistory () {
        return history;
    }

    public void saveAll()
    {
        config.set("expiration.ban", banExpiration);
        config.set("banReason", banReason);
        config.set("banID", banID);
        config.set("expiration.mute", muteExpiration);
        config.set("muteReason", muteReason);
        config.set("muteID", muteID);

        saveHistory();

        save();
    }

    private void saveHistory() {
        for (Map.Entry<Long, Map<String, String>> LoopEntry : history.entrySet()) {
            Map<String, String> historyEntry = LoopEntry.getValue();
            config.set("history." + LoopEntry.getKey() + ".givenAt", historyEntry.get("givenAt"));
            config.set("history." + LoopEntry.getKey() + ".type", historyEntry.get("type"));
            config.set("history." + LoopEntry.getKey() + ".reason", historyEntry.get("reason"));
            config.set("history." + LoopEntry.getKey() + ".id", historyEntry.get("id"));
            config.set("history." + LoopEntry.getKey() + ".expiration", historyEntry.get("expiration"));
            config.set("history." + LoopEntry.getKey() + ".givenBy", historyEntry.get("givenBy"));
            if (historyEntry.get("removed").equals("false")) {
                config.set("history." + LoopEntry.getKey() + ".removed", false);
            } else {
                config.set("history." + LoopEntry.getKey() + ".removed", true);
            }
            config.set("history." + LoopEntry.getKey() + ".removedBy", historyEntry.get("removedBy"));
            config.set("history." + LoopEntry.getKey() + ".removedReason", historyEntry.get("removedReason"));
        }
    }

    public void save()
    {
        try
        {
            config.save(file);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
