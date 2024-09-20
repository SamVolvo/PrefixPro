package com.samvolvo.luckpermsPrefix.commands;

import com.samvolvo.luckpermsPrefix.LuckpermsPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {
    private final LuckpermsPrefix plugin;

    public ReloadCommand(LuckpermsPrefix plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player){
            if (!player.hasPermission("luckpermsprefix.reload")){
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou don't have the permission to use this command."));
                return true;
            }

            for (Player player1 : Bukkit.getOnlinePlayers()){
                plugin.getPlayerTeamUtil().setPlayerTeam(player1);
            }

        }else{
            sender.sendMessage("§cYou can't run commands from console for now!");
        }
        return true;
    }
}
