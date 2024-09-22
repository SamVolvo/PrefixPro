package com.samvolvo.luckpermsPrefix.managers;

import com.samvolvo.luckpermsPrefix.LuckpermsPrefix;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Optional;

public class PrefixManager {
    private LuckpermsPrefix plugin;

    public PrefixManager(LuckpermsPrefix plugin){
        this.plugin = plugin;
    }

    public String getPlayerPrefix(Player player){
        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user == null){
            plugin.getAPILogger().warning("Luckperms did not found a user for " + player.getName());
            return null;
        }

        Optional<Group> highestWeightGroup = user.getNodes(NodeType.INHERITANCE).stream()
                .map(node -> (InheritanceNode) node)
                .map(inheritanceNode -> plugin.getLuckPerms().getGroupManager().getGroup(inheritanceNode.getGroupName()))
                .filter(group -> group != null)
                .max(Comparator.comparingInt(group -> group.getWeight().orElse(0)));

        if (highestWeightGroup.isEmpty()){
            plugin.getAPILogger().warning("No group found for player " + player.getName());
            return null;
        }

        Group group = highestWeightGroup.get();
        CachedMetaData metaData = group.getCachedData().getMetaData();
        String prefix = metaData.getPrefix();

        if (prefix == null || prefix.isEmpty()){
            plugin.getAPILogger().warning("No prefix found for group (" + group.getName() + ") for player " + player.getName());
            prefix = "";
        }

        return prefix;

    }

}
