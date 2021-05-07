package xyz.cosmicity.nebostats;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.cosmicity.nebostats.listeners.LoadingListener;
import xyz.cosmicity.nebostats.storage.SQLService;
import xyz.cosmicity.nebostats.storage.SQLUtils;

public final class NeboStats extends JavaPlugin {
    private SQLService sql;

    @Override
    public void onEnable() {
        getLogger().info("Enabling NeboStats...");
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        ConfigurationSection sqlConfig = getConfig().getConfigurationSection("mysql");
        if(sqlConfig == null) {
            getLogger().severe("MySQL settings are missing!");
        }
        assert sqlConfig != null;
        sql = new SQLService(sqlConfig.getString("host"),
                sqlConfig.getString("database"),
                sqlConfig.getString("user"), sqlConfig.getString("pass"));
        Bukkit.getPluginManager().registerEvents(new LoadingListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling NeboStats...");

        sql.onDisable();
        SQLUtils.close();
    }

    public SQLService sql() {
        return sql;
    }
}
