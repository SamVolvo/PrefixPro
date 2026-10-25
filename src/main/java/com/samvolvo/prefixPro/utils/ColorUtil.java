package com.samvolvo.prefixPro.utils;

import org.bukkit.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {
    private static final Pattern HEX = Pattern.compile("(?i)(?<!\\\\)(?:&)?#([0-9a-f]{6})(?![0-9a-f])");
    private static final Pattern CODES = Pattern.compile("(?i)§x(?:§[0-9a-f]){6}|§[0-9a-fk-or]");
    private ColorUtil() {}

    public static String colorize(String input) {
        if (input == null || input.isEmpty()) return "";
        Matcher matcher = HEX.matcher(input);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            StringBuilder replacement = new StringBuilder("§x");
            for (char digit : matcher.group(1).toLowerCase(java.util.Locale.ROOT).toCharArray()) {
                replacement.append('§').append(digit);
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(out);
        return ChatColor.translateAlternateColorCodes('&', out.toString());
    }

    /** Keep the active color AND formatting; a color/reset clears earlier formats. */
    public static String getLastColors(String text) {
        if (text == null) return "";
        Matcher matcher = CODES.matcher(text);
        String color = "";
        StringBuilder formats = new StringBuilder();
        while (matcher.find()) {
            String code = matcher.group().toLowerCase(java.util.Locale.ROOT);
            char kind = code.charAt(1);
            if (kind == 'x' || kind == 'r' || "0123456789abcdef".indexOf(kind) >= 0) {
                color = code;
                formats.setLength(0);
            } else if (formats.indexOf(code) < 0) {
                formats.append(code);
            }
        }
        return color + formats;
    }

    /** Paper can return uppercase hex digits after its component round-trip. */
    public static String normalizeCodes(String text) {
        if (text == null) return "";
        return CODES.matcher(text).replaceAll(match -> match.group().toLowerCase(java.util.Locale.ROOT));
    }

    /** Do not split a hex sequence, a legacy code or a surrogate pair at a server limit. */
    public static String truncate(String text, int limit) {
        int end = 0;
        while (end < text.length()) {
            int width = Character.charCount(text.codePointAt(end));
            if (text.charAt(end) == '§') {
                if (end + 1 >= text.length()) break;
                width = Character.toLowerCase(text.charAt(end + 1)) == 'x' ? 14 : 2;
                if (end + width > text.length()) break;
            }
            if (end + width > limit) break;
            end += width;
        }
        return text.substring(0, end);
    }
}
