package fr.minuskube.bot.discord.util;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageUtils.class);

    public static RestAction<Message> error(MessageChannel channel, String errorTitle, String errorMsg) {
        return channel.sendMessage(new EmbedBuilder()
                .setColor(Color.RED)
                .setThumbnail("http://minuskube.fr/error_icon.png")
                .addField("`" + errorTitle + "`", errorMsg, false)
                .build());
    }

    public static RestAction<Message> error(MessageChannel channel, String errorMsg) {
        return error(channel, "ERROR!", errorMsg);
    }

    public static RestAction<Void> removeReaction(String emote, Message message) {
        message = message.getChannel().getMessageById(message.getId()).complete();

        return message.getReactions().stream()
                .filter(reaction -> {
                    LOGGER.debug(reaction.getEmote().getName() + " / " + emote);
                    return reaction.getEmote().getName().equals(emote);
                })
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Emote not found."))
                .removeReaction();
    }

    public static List<Member> getMemberMentions(Guild guild, String msg) {
        Pattern pattern = Pattern.compile("<(@|@!)(\\d+)>");
        Matcher matcher = pattern.matcher(msg);

        List<Member> result = new ArrayList<>();

        while(matcher.find()) {
            String id = matcher.group(2);
            Member member = guild.getMemberById(id);

            if(member != null)
                result.add(member);
        }

        return result;
    }

    public static String replaceMentions(Guild guild, String msg) {
        Pattern pattern = Pattern.compile("<(@|@!|@&|#)(\\d+)>");
        Matcher matcher = pattern.matcher(msg);

        StringBuffer result = new StringBuffer();

        while(matcher.find()) {
            boolean channelMention = matcher.group(1).equals("#");
            String id = matcher.group(2);

            if(!channelMention) {
                Member member = guild.getMemberById(id);

                if(member != null) {
                    matcher.appendReplacement(result, "@" + member.getEffectiveName());

                    continue;
                }

                Role role = guild.getRoleById(id);

                if(role != null)
                    matcher.appendReplacement(result, "@" + role.getName());
            }
            else {
                Channel channel = guild.getJDA().getTextChannelById(id);

                if(channel != null)
                    matcher.appendReplacement(result, "#" + channel.getName());
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }

}
