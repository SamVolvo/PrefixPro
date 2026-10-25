package com.samvolvo.prefixPro.listeners;

import com.samvolvo.prefixPro.PrefixManager;
import com.samvolvo.prefixPro.PrefixPro;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.user.User;

public class LuckPermsListener {
    private final PrefixPro plugin;
    public LuckPermsListener(PrefixPro plugin, PrefixManager prefixManager) { this.plugin = plugin; }
    public void onNodeAdd(NodeAddEvent event) { changed(event.getTarget()); }
    public void onNodeRemove(NodeRemoveEvent event) { changed(event.getTarget()); }
    public void onDataRecalculate(UserDataRecalculateEvent event) { plugin.requestRefresh(event.getUser().getUniqueId()); }
    private void changed(Object target) {
        if (target instanceof User user) plugin.requestRefresh(user.getUniqueId());
        else plugin.requestRefreshAll(); // Group prefixes/inheritance affect multiple online users.
    }
}
