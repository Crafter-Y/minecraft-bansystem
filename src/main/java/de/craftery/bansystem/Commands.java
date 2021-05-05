package de.craftery.bansystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class Commands implements CommandExecutor {
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

    public static boolean isNumeric(String string) {
        int intValue;

        if(string == null || string.equals("")) {
            return false;
        }
        try {
            intValue = Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("bmk")) {
            if (sender.hasPermission("bansystem.bmk")) {
                if(args.length != 2) {
                    if (args.length == 0) {
                        FileConfiguration punishmentConfig = Main.getInstance().getPunishmentConfig();
                        ConfigurationSection pcon = punishmentConfig.getConfigurationSection("punishmentConfig");
                        String Banreasons = ChatColor.BLACK + "[Ban-SYS] " + ChatColor.GREEN + "Mögliche Bangründe sind: " + ChatColor.BLUE;
                        for (String key : pcon.getKeys(false))
                        {
                            Banreasons += key + " ,";
                        }
                        sender.sendMessage(Banreasons);
                        return true;
                    } else {
                        sender.sendMessage("Syntax: /bmk <player> <grund>");
                        return true;
                    }
                }

                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    return true;
                } else {
                    String punishment = "";
                    String inppunishment = args[1];
                    Long expiration = 0L;
                    String punishType = "";
                    FileConfiguration punishmentConfig = Main.getInstance().getPunishmentConfig();
                    ConfigurationSection pcon = punishmentConfig.getConfigurationSection("punishmentConfig");

                    for (String key : pcon.getKeys(false))
                    {
                        if (key.equals(inppunishment)) {
                            expiration = System.currentTimeMillis() + pcon.getLong(key + ".expiration");
                            punishType =  pcon.getString(key + ".type");
                            punishment = key;
                        }
                    }
                    if (expiration == 0L) {
                        expiration = 1000 * 60 * 5L;
                    }
                    String punishmentID = "#" + Main.getInstance().makePunishmehtID();

                    if (!(punishment == "")) {
                        if (punishType.equals("Ban")) {
                            sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.DARK_RED + target.getName() + " wurde erfolgreich bestraft.");
                            String banmessage = ChatColor.DARK_GRAY + "» " +ChatColor.DARK_AQUA + "GangCraft.eu" + ChatColor.DARK_GRAY + " «\n\n" + ChatColor.GRAY + "Du bist zur Zeit vom Spielbetrieb ausgeschlossen.\n\n" + "\uD83E\uDCA1 Grund: " + ChatColor.DARK_RED + args[1] + "\n\n" + ChatColor.GRAY + "Restdauer: " + ChatColor.RED + findDifference(new Date(expiration), new Date(System.currentTimeMillis())) + "\n\n" + ChatColor.GRAY + "Bearbeitungs-Nr: " + ChatColor.GREEN + punishmentID;
                            target.kickPlayer(banmessage);
                            PunishDealer.ban(target, sender.getName(), punishment, punishmentID, expiration);
                        } else if (punishType.equals("Kick")) {
                            sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.DARK_RED + target.getName() + " wurde erfolgreich bestraft.");
                            String banmessage = ChatColor.DARK_GRAY + "» " +ChatColor.DARK_AQUA + "GangCraft.eu" + ChatColor.DARK_GRAY + " «\n\n" + ChatColor.GRAY + "Du wurdest vom Server geworfen.\n\n" + "\uD83E\uDCA1 Grund: " + ChatColor.DARK_RED + args[1] + "\n\n" + ChatColor.GRAY + "Bearbeitungs-Nr: " + ChatColor.GREEN + punishmentID + "\n\n" + ChatColor.GRAY + "Bitte unterlasse dieses Verhalten.";
                            PlayerFile file = Main.getInstance().getPlayerFile(target.getUniqueId());
                            file.addHistory("Kick", punishment, punishmentID, expiration, sender.getName(), System.currentTimeMillis());
                            file.saveAll();
                            target.kickPlayer(banmessage);
                        } else if (punishType.equals("Mute")) {
                            sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.DARK_RED + target.getName() + " wurde erfolgreich bestraft.");
                            PunishDealer.mute(target, sender.getName(), punishment, punishmentID, expiration);
                        }

                    } else {
                        sender.sendMessage("Ban Type not found!");
                    }
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung auf diesen Befehl!");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("unban")) {
            if (sender.hasPermission("bansystem.unban")) {
                if(args.length < 2) {
                    sender.sendMessage("Syntax: /unban <player> <grund>");
                    return true;
                }

                GetOfflineUUID uuidFetcher = new GetOfflineUUID(Arrays.asList(args[0]));
                try {
                    Map<String, UUID> playerUUIDs = uuidFetcher.call();
                    UUID targetUUID = playerUUIDs.get(args[0]);
                    List<String> list = new ArrayList<String>(Arrays.asList(args));

                    list.remove(0);

                    String punishment = String.join(" ", list);
                    PunishDealer.unban(targetUUID, sender.getName(), punishment);
                    sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.RED + "Entbannung von " + ChatColor.GRAY + args[0] + ChatColor.RED +" wurde erfolgreich aufgehoben.");
                } catch (Exception ex) {
                    sender.sendMessage("Player not found");
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung auf diesen Befehl!");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("unmute")) {
            if (sender.hasPermission("bansystem.unmute")) {
                if(args.length < 2) {
                    sender.sendMessage("Syntax: /unmute <player> <grund>");
                    return true;
                }

                GetOfflineUUID uuidFetcher = new GetOfflineUUID(Arrays.asList(args[0]));
                try {
                    Map<String, UUID> playerUUIDs = uuidFetcher.call();
                    UUID targetUUID = playerUUIDs.get(args[0]);
                    List<String> list = new ArrayList<String>(Arrays.asList(args));

                    list.remove(0);

                    String punishment = String.join(" ", list);
                    PunishDealer.unmute(targetUUID, sender.getName(), punishment);
                    sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.RED + "Entbannung von " + ChatColor.GRAY + args[0] + ChatColor.RED +" wurde erfolgreich aufgehoben.");
                } catch (Exception ex) {
                    sender.sendMessage("Player not found");
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung auf diesen Befehl!");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("vz")) {
            if (sender.hasPermission("bansystem.vz")) {
                if(args.length != 1) {
                    sender.sendMessage("Syntax: /vz <player>");
                    return true;
                }
                GetOfflineUUID uuidFetcher = new GetOfflineUUID(Arrays.asList(args[0]));
                try {
                    Map<String, UUID> playerUUIDs = uuidFetcher.call();
                    UUID targetUUID = playerUUIDs.get(args[0]);
                    PlayerFile file = Main.getInstance().getPlayerFile(targetUUID);
                    sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.RED + "History von " + ChatColor.BLUE + args[0]);
                    Map<Long, Map<String, String>> history = file.getHistory();
                    sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.DARK_RED + history.values().size() + ChatColor.RED + " Einträge");
                    for (Map.Entry<Long, Map<String, String>> LoopEntry : history.entrySet()) {
                        Map<String, String> historyEntry = LoopEntry.getValue();
                        sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.DARK_RED + historyEntry.get("type") + ChatColor.GRAY + " am " + ChatColor.BLUE + historyEntry.get("givenAt"));
                        sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.RED + "Vergeben von: " + ChatColor.GREEN + historyEntry.get("givenBy"));
                        sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.RED + "Grund: " + ChatColor.GREEN + historyEntry.get("reason"));
                        sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.RED + "Bearbeitungs-Nr.: " + ChatColor.GREEN + historyEntry.get("id"));
                        if (historyEntry.get("removed").equals("true")) {
                            sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.RED + "Entfernt wegen: " + ChatColor.GREEN + historyEntry.get("removedReason"));
                            sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.RED + "Entfernt von: " + ChatColor.GREEN + historyEntry.get("removedBy"));
                        }
                        sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.GRAY + "  ");
                    }


                } catch (Exception ex) {
                    sender.sendMessage("Player not found");
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung auf diesen Befehl!");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("b")) {
            if (sender.hasPermission("bansystem.b")) {
                if(args.length < 3) {
                    sender.sendMessage("Syntax: /b <player> <zeit in min> <grund>");
                    return true;
                }
                String punishmentID = "#" + Main.getInstance().makePunishmehtID();
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    return true;
                }

                if(!isNumeric(args[1])) {
                    return true;
                }

                Long expiration = Long.parseLong(args[1]) * 60 * 1000  + System.currentTimeMillis();

                List<String> list = new ArrayList<String>(Arrays.asList(args));

                list.remove(0);
                list.remove(0);

                String punishment = String.join(" ", list);

                sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.DARK_RED + target.getName() + " wurde erfolgreich bestraft.");
                String banmessage = ChatColor.DARK_GRAY + "» " +ChatColor.DARK_AQUA + "GangCraft.eu" + ChatColor.DARK_GRAY + " «\n\n" + ChatColor.GRAY + "Du bist zur Zeit vom Spielbetrieb ausgeschlossen.\n\n" + "\uD83E\uDCA1 Grund: " + ChatColor.DARK_RED + punishment + "\n\n" + ChatColor.GRAY + "Restdauer: " + ChatColor.RED + findDifference(new Date(expiration), new Date(System.currentTimeMillis())) + "\n\n" + ChatColor.GRAY + "Bearbeitungs-Nr: " + ChatColor.GREEN + punishmentID;
                target.kickPlayer(banmessage);
                PunishDealer.ban(target, sender.getName(), punishment, punishmentID, expiration);
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung auf diesen Befehl!");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("m")) {
            if (sender.hasPermission("bansystem.m")) {
                if(args.length < 3) {
                    sender.sendMessage("Syntax: /m <player> <zeit in min> <grund>");
                    return true;
                }
                String punishmentID = "#" + Main.getInstance().makePunishmehtID();
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    return true;
                }

                if(!isNumeric(args[1])) {
                    return true;
                }

                Long expiration = Long.parseLong(args[1]) * 60 * 1000  + System.currentTimeMillis();

                List<String> list = new ArrayList<String>(Arrays.asList(args));

                list.remove(0);
                list.remove(0);

                String punishment = String.join(" ", list);

                sender.sendMessage(ChatColor.BLACK + "[Ban-SYS] " + ChatColor.DARK_RED + target.getName() + " wurde erfolgreich bestraft.");
                PunishDealer.mute(target, sender.getName(), punishment, punishmentID, expiration);
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung auf diesen Befehl!");
                return true;
            }
        }

        /*if (command.getName().equalsIgnoreCase("ouuid")) {
            UUID uuid = offlineUUID.getUUID(args[0]);
            String msg;
            if (uuid != null) {
                msg = uuid.toString();
            } else {
                msg = "UUID not found";
            }
            sender.sendMessage(msg);
            return true;
        }*/

        return false;
    }
}
