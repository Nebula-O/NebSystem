/*
© Copyright Nick Williams 2021.
Credit should be given to the original author where this code is used.
 */

package xyz.cosmicity.nebostats.storage;

import co.aikar.idb.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class SQLService {

    private final SQLTable profileTable;

    @NonNull
    private final LoadingCache<@NotNull UUID, @NotNull Profile> profileCache;

    public SQLService(String host, String database, String username, String password) {

        DatabaseOptions options = DatabaseOptions.builder()
                .mysql(username,
                        password,
                        database,
                        host).build();

        SQLUtils.setDb(PooledDatabaseOptions.builder().options(options).createHikariDatabase());

        profileTable = new SQLTable("profiles", "uuid","VARCHAR(36)",new String[][]{{"joined","TEXT"},{"discordid","VARCHAR(32)"}});

        profileCache = CacheBuilder.newBuilder()
                .removalListener(this::saveProfile)
                .build(CacheLoader.from(this::loadProfile));
    }

    public void onDisable() {
        profileCache.invalidateAll();
        profileCache.cleanUp();
    }

    @NonNull
    private Profile loadProfile(@NotNull final UUID uuid) {
        Profile profile = new Profile(uuid);
        if(! SQLUtils.holdsKey(profileTable, "\""+uuid.toString()+"\"")) return profile;
        return profile.loadAttributes(profileTable);
    }

    private void saveProfile(@NotNull final RemovalNotification<@NotNull UUID, @NotNull Profile> notification) {
        setRow( profileTable, notification.getValue().getUuid().toString(), Long.toString(notification.getValue().getFirstJoined().getTime()), notification.getValue().getDiscord());
    }

    /*
     * load or unload a profile from cache
     */
    public void validate(@NotNull final Profile profile) {
        this.profileCache.put(profile.getUuid(), profile);
    }
    public void invalidate(@NotNull final Profile profile) {
        this.profileCache.invalidate(profile.getUuid());
    }

    public Profile wrap(@NotNull final UUID uuid) {
        try {
            return profileCache.get(uuid);
        }
        catch(final ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Profile wrapIfLoaded(@NotNull final UUID uuid) {
        return profileCache.getIfPresent(uuid);
    }


    /**
     * @param values - the values only. (just values not colLabel=value etc)
     */
    public void setRow(final SQLTable table, final String key, final Object... values) {
        List<String> columnLabels = table.getColLabels(),
                equivalents = new ArrayList<>();
        for(String lbl : columnLabels) {
            equivalents.add(lbl + " = ?");
        }
        List<Object> objs = new ArrayList<>();
        objs.add(key);
        objs.addAll(Arrays.asList(values));
        objs.addAll(Arrays.asList(values));
        Bukkit.getServer().getLogger().info("INSERT INTO "+table.getName()+" ("+table.getPkLabel()+","+String.join(",",columnLabels) + ") VALUES (?" + ",?".repeat(columnLabels.size())+")" +
                " ON DUPLICATE KEY UPDATE " + String.join(", ",equivalents));
        for(Object o : objs) {
            Bukkit.getServer().getLogger().info(o.toString());
        }
        SQLUtils.getDb().createTransaction(stm -> {
            stm.executeUpdateQuery("INSERT INTO "+table.getName()+" ("+table.getPkLabel()+","+String.join(",",columnLabels) + ") VALUES (?" + ",?".repeat(columnLabels.size())+")" +
                    " ON DUPLICATE KEY UPDATE " + String.join(", ",equivalents) + ";", objs.toArray(Object[]::new));
            return true;
        });
    }

}
