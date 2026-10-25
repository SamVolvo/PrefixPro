package com.samvolvo.prefixPro.utils;

/** java.util.logging is safe for network callbacks; Bukkit console APIs are not. */
public class Logger {
    private final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("PrefixPro");
    public void error(String message) { logger.severe(message); }
    public void debug(String message) { logger.fine(message); }
    public void warning(String message) { logger.warning(message); }
    public void loading(String message) { logger.info(message); }
    public void info(String message) { logger.info(message); }
}
