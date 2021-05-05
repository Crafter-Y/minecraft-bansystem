package de.craftery.bansystem;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class PunishDealer
{
    public static void ban(OfflinePlayer target, String sender, String punishment, String punishmentID, Long expiration) // public static void punish(PunishType type, OfflinePlayer player, String punisherName, String reason, String expiration)
    {
        PlayerFile file = Main.getInstance().getPlayerFile(target.getUniqueId());

        file.setBanExpiration(expiration);
        file.setBanReason(punishment);
        file.setBanID(punishmentID);
        file.addHistory("Ban", punishment, punishmentID, expiration, sender, System.currentTimeMillis());
        file.saveAll();
    }

    public static void mute(OfflinePlayer target, String sender, String punishment, String punishmentID, Long expiration) {
        PlayerFile file = Main.getInstance().getPlayerFile(target.getUniqueId());

        file.setMuteExpiration(expiration);
        file.setMuteReason(punishment);
        file.setMuteID(punishmentID);
        file.addHistory("Mute", punishment, punishmentID, expiration, sender, System.currentTimeMillis());
        file.saveAll();
    }

    public static void unban(UUID targetUUID, String remover, String reason)
    {
        PlayerFile file = Main.getInstance().getPlayerFile(targetUUID);
        file.removeHistory(file.getBanExpiration(), remover, reason);
        file.setBanExpiration(0);
        file.saveAll();
    }
    public static void unmute(UUID targetUUID, String remover, String reason)
    {
        PlayerFile file = Main.getInstance().getPlayerFile(targetUUID);
        file.removeHistory(file.getMuteExpiration(), remover, reason);
        file.setMuteExpiration(0);
        file.saveAll();
    }
}
