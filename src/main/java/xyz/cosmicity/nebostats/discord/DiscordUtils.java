/*
© Copyright Nick Williams 2021.
Credit should be given to the original author where this code is used.
 */

package xyz.cosmicity.nebostats.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class DiscordUtils {

    private static JDA jda;
    private static Guild guild;
    private static Role mod_role;
    private static TextChannel dm_overflow_channel;

    public static JDA getJda() {
        return jda;
    }

    public static void setJda(JDA j) throws InterruptedException {
        jda = j;
        jda.awaitReady();
    }

    public static Guild getGuild() {
        return guild;
    }

    public static void setGuild(String id) {
        DiscordUtils.guild = jda.getGuildById(id);
    }


    public static Role getMod_role() {
        return mod_role;
    }

    public static void setMod_role(String id) {
        DiscordUtils.mod_role = guild.getRoleById(id);
    }

    public static void setDm_overflow_channel(String s) {
        dm_overflow_channel = jda.getTextChannelById(s);
    }

    public static TextChannel getDm_overflow_channel() {
        return dm_overflow_channel;
    }

    public static void sendOverflowDM(String raw) {
        dm_overflow_channel.sendMessage(raw).queue();
    }
}
