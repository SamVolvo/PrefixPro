package com.samvolvo.luckpermsPrefix.commands;

import com.samvolvo.luckpermsPrefix.LuckpermsPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor {
    private final LuckpermsPrefix plugin;

    public MainCommand(LuckpermsPrefix plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player){
            if (args.length != 2){
                sender.sendMessage("&cCommand not completed.");
                return false;
            }

            if (!player.hasPermission("luckpermsprefix.reload")){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou don't have the permission to use this command."));
                return true;
            }

            if (!args[0].equalsIgnoreCase("reload")){
                sender.sendMessage("No function recognized.");
                return false;
            }

            if (args[1].equalsIgnoreCase("prefix")){
                for (Player player1 : Bukkit.getOnlinePlayers()){
                    plugin.getPlayerTeamUtil().setPlayerTeam(player1);
                }
            }

            if (args[1].equalsIgnoreCase("config")){
                plugin.loadConfig();
                sender.sendMessage("Config reloaded");
            }



        }else{
            sender.sendMessage("§cLuckPermsPrefix doesn't support console commands yet!");
        }
        return true;
    }
}
