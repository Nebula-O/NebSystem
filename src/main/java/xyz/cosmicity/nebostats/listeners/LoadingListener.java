/*
© Copyright Nick Williams 2021.
Credit should be given to the original author where this code is used.
 */

package xyz.cosmicity.nebostats.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.cosmicity.nebostats.NeboStats;
import xyz.cosmicity.nebostats.storage.Profile;

public class LoadingListener implements Listener {

    private final NeboStats pl;

    public LoadingListener(final NeboStats plugin) {pl = plugin;}

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        Profile profile = pl.sql().wrap(e.getPlayer().getUniqueId());
        pl.sql().validate(profile);
        pl.getLogger().info(e.getPlayer().getName()+" first joined at: " + profile.getFirstJoined().toString());
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        Profile profile = pl.sql().wrapIfLoaded(e.getPlayer().getUniqueId());
        pl.sql().invalidate(profile);
        pl.getLogger().info(e.getPlayer().getName()+" first joined at: " + profile.getFirstJoined().toString());
    }

}
