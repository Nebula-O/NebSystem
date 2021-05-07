package xyz.cosmicity.nebostats.discord;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.cosmicity.nebostats.NeboStats;
import xyz.cosmicity.nebostats.storage.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DiscordListeners extends ListenerAdapter {

    private final NeboStats plugin;

    public DiscordListeners(NeboStats pl) {plugin = pl;}

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent e) {
        super.onPrivateMessageReceived(e);

        if(e.getAuthor().isBot()) return;

        String text = e.getMessage().getContentRaw();

        boolean validName = false;

        Player player = plugin.getServer().getPlayer(text);

        if(player != null) {

            validName = true;

            Profile profile = plugin.sql().wrapIfLoaded(player.getUniqueId());

            profile.setDiscord(e.getAuthor().getId());

            plugin.sql().validate(profile);

        }

        else {

            e.getMessage().reply(
                    "Sorry, I couldn't figure out what that means.\n" +
                    "If you are trying to verify your Minecraft account, make sure you're online and then type the username here without any other words (I'm CaSe SeNsItIvE).\n" +
                    "Type something else for assistance from server staff.").queue();

        }

        // forward to staff
        DiscordUtils.sendOverflowDM(
                "**`From: `" + e.getAuthor().getAsTag()
                        +" / "+ e.getAuthor().getAsMention()
                        + (validName ? " (Valid MC username)" :"")
                        + "**\n"
                        + text);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent e) {

        super.onGuildMessageReceived(e);

        if(e.getAuthor().isBot() ||
                ! e.getMessage().getContentRaw().startsWith("."))
            return;

        String text = e.getMessage().getContentRaw();
        String lText = text.toLowerCase();

        // if they are a moderator
        if(
                Objects.requireNonNull(
                        e.getMember()
                ).getRoles().contains(DiscordUtils.getMod_role())) {

            if(lText.startsWith(".reply ")) {
                try {
                    User target = e.getMessage().getMentionedUsers().get(0);
                    String raw = text.substring(".reply ".length() + target.getAsMention().length());
                    target.openPrivateChannel()
                            .flatMap(channel -> channel.sendMessage(raw))
                            .queue();
                } catch (IndexOutOfBoundsException ex) {
                    e.getMessage().reply("Invalid. `.reply @usermention Put message here.``").queue();
                }
            } else if (lText.startsWith(".onlineplayers")) {
                String dcId;
                List<String> raw = new ArrayList<>();
                User dcUser;
                int c = 0;
                for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                    c++;
                    dcId = plugin.sql().wrapIfLoaded(p.getUniqueId()).getDiscord();
                    if(!dcId.isEmpty()) {
                        dcUser = DiscordUtils.getJda().getUserById(dcId);
                        assert dcUser != null;
                        if(DiscordUtils.getGuild().isMember(dcUser)) {
                            raw.add(p.getName() + " `" + dcUser.getAsTag() + "`\n");
                        }
                    } else {
                        raw.add(p.getName());
                    }
                }
                e.getMessage().reply(c + " online:\n" + String.join("\n", raw)).queue();
            }
        }
    }
}
