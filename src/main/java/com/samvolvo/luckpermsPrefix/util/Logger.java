package com.samvolvo.luckpermsPrefix.util;

import com.samvolvo.luckpermsPrefix.LuckpermsPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {

    public void error(String message){
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c[ERROR]: &r" + message));
    }

    public void debug(String message){
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[DEBUG]: &r" + message));
    }

    public void warning(String message){
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&e[WARNING]: &r" + message));
    }

    public void loading(String message){
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[LOADING]: &r" + message));
    }

    public void info(String message){
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[INFO]: &r" + message));
    }
}
