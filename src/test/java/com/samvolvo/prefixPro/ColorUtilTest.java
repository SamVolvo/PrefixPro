package com.samvolvo.prefixPro;

import com.samvolvo.prefixPro.utils.ColorUtil;
import com.samvolvo.prefixPro.utils.UpdateChecker;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ColorUtilTest {
    @Test void hexAndLegacyColors() {
        assertEquals("§x§1§2§a§b§e§fRank §l", ColorUtil.colorize("&#12ABefRank &l"));
        assertEquals("§x§a§b§c§d§e§f", ColorUtil.colorize("#abcdef"));
        assertEquals("", ColorUtil.colorize(null));
        assertEquals("\\#abcdef", ColorUtil.colorize("\\#abcdef"));
        assertEquals("#abcdef1", ColorUtil.colorize("#abcdef1"));
    }
    @Test void inheritsColorAndFormatsAndHonorsReset() {
        assertEquals("§x§1§2§a§b§e§f§l§o", ColorUtil.getLastColors(ColorUtil.colorize("&#12ABefRank &l&o")));
        assertEquals("§r§l", ColorUtil.getLastColors("§a§lRank§r§l"));
        assertEquals("§c", ColorUtil.getLastColors("§a§lRank§c"));
        assertEquals("", ColorUtil.getLastColors(null));
    }
    @Test void truncationDoesNotBreakHexOrUnicode() {
        assertEquals("test", ColorUtil.truncate(ColorUtil.colorize("test&#aabbccName"), 12));
        assertEquals("test§x§a§a§b§b§c§c", ColorUtil.truncate(ColorUtil.colorize("test&#aabbccName"), 18));
        assertEquals("", ColorUtil.truncate("😀", 1));
        assertEquals("x", ColorUtil.truncate("x§", 20));
    }
    @Test void updateComparisonDoesNotRecommendOlderRelease() {
        assertTrue(UpdateChecker.isNewerVersion("2.10.0", "2.2.0"));
        assertFalse(UpdateChecker.isNewerVersion("2.1.0", "2.2.0"));
        assertFalse(UpdateChecker.isNewerVersion("2.2.0", "2.2.0"));
        assertFalse(UpdateChecker.isNewerVersion("broken", "2.2.0"));
    }
}
