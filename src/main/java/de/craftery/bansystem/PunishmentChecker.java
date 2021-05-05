package de.craftery.bansystem;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PunishmentChecker implements Listener
{
    private List<UUID> kickedUUIDs = new ArrayList<UUID>();

    public PunishmentChecker()
    {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    String findDifference(Date start_date, Date end_date)
    {
        long difference_In_Time = start_date.getTime() - end_date.getTime();
        long difference_In_Seconds
                = (difference_In_Time
                / 1000)
                % 60;

        long difference_In_Minutes
                = (difference_In_Time
                / (1000 * 60))
                % 60;

        long difference_In_Hours
                = (difference_In_Time
                / (1000 * 60 * 60))
                % 24;

        long difference_In_Years
                = (difference_In_Time
                / (1000l * 60 * 60 * 24 * 365));

        long difference_In_Days
                = (difference_In_Time
                / (1000 * 60 * 60 * 24))
                % 365;

        String returner = "";
        if (difference_In_Years != 0) {
            returner += difference_In_Years + " Jahr/e, ";
        }
        if (difference_In_Days != 0) {
            returner += difference_In_Days + " Tag/e, ";
        }
        if (difference_In_Hours != 0) {
            returner += difference_In_Hours + " Stunde/n, ";
        }
        if (difference_In_Minutes != 0) {
            returner += difference_In_Minutes + " Minute/n, ";
        }
        if (difference_In_Seconds != 0) {
            returner += difference_In_Seconds + " Sekunden";
        }

        return returner;
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        PlayerFile file = Main.getInstance().getPlayerFile(player.getUniqueId());

        if (file.getBanExpiration() > System.currentTimeMillis()) {
            kickedUUIDs.add(event.getPlayer().getUniqueId());

            String expires = findDifference(new Date(file.getBanExpiration()), new Date(System.currentTimeMillis()));

            String message = ChatColor.DARK_GRAY + "» " +ChatColor.DARK_AQUA + "GangCraft.eu" + ChatColor.DARK_GRAY + " «\n\n" + ChatColor.GRAY + "Du bist zur Zeit vom Spielbetrieb ausgeschlossen.\n\n" + "\uD83E\uDCA1 Grund: " + ChatColor.DARK_RED + file.getBanReason() + "\n\n" + ChatColor.GRAY + "Restdauer: " + ChatColor.RED + expires + "\n\n" + ChatColor.GRAY + "Bearbeitungs-Nr: " + ChatColor.GREEN + file.getBanID();

            event.setJoinMessage(null);
            event.getPlayer().kickPlayer(message);

            new BukkitRunnable()
            {
                public void run()
                {
                    Main.getInstance().getLogger().info(event.getPlayer().getName() + " has been kicked by the Bansystem");
                }
            }.runTaskLater(Main.getInstance(), 5);

        } else {
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLeave(PlayerQuitEvent event)
    {
        UUID uuid = event.getPlayer().getUniqueId();
        if (kickedUUIDs.contains(uuid))
        {
            event.setQuitMessage(null);
            kickedUUIDs.remove(uuid);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e)
    {
        PlayerFile file = Main.getInstance().getPlayerFile(e.getPlayer().getUniqueId());

        if (file.getMuteExpiration() > System.currentTimeMillis()) {
            e.setCancelled(true);

            String expires = findDifference(new Date(file.getMuteExpiration()), new Date(System.currentTimeMillis()));
            e.getPlayer().sendMessage(ChatColor.DARK_RED + "Du wurdest aus dem Chat wegen unangemessenem Verhalten ausgeschlossen!");
            e.getPlayer().sendMessage(ChatColor.RED + "Grund: " + ChatColor.GREEN + file.getMuteReason());
            e.getPlayer().sendMessage(ChatColor.RED + "Dauer: " + ChatColor.GREEN + expires);
            e.getPlayer().sendMessage(ChatColor.RED + "Bearbeitungs-Nr.: " + ChatColor.GREEN + file.getMuteID());


        }
    }
}
