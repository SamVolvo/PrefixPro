package com.samvolvo.prefixPro.commands.tabcompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class MainCommandCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("prefixpro")){
            if (args.length == 1){
                completions.add("prefix");
                completions.add("config");
            }else if (args.length == 2){
                completions.add("reload");
            }
        }

        return List.of();
    }
}
