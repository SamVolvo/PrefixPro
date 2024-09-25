package com.samvolvo.prefixPro.commands;

import com.samvolvo.prefixPro.PrefixPro;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor {
    private final PrefixPro plugin;

    public MainCommand(PrefixPro plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player){
            if (args.length != 2){
                sender.sendMessage("&cCommand not completed.");
                return false;
            }

            if (!player.hasPermission("prefixpro.reload")){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou don't have the permission to use this command."));
                return true;
            }

            if (args[0].equalsIgnoreCase("prefix") && args[1].equalsIgnoreCase("reload")){
                for (Player player1 : Bukkit.getOnlinePlayers()){
                    plugin.getPlayerTeamUtil().setPlayerTeam(player1);
                }
            }

            if (args[0].equalsIgnoreCase("config") && args[1].equalsIgnoreCase("reload")){
                plugin.loadConfig();
                sender.sendMessage("§eReloaded config.");
            }



        }else{
            sender.sendMessage("§cPrefixPro doesn't support console commands yet!");
        }
        return true;
    }
}
