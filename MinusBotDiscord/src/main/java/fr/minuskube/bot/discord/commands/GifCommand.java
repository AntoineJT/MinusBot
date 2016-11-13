package fr.minuskube.bot.discord.commands;

import at.mukprojects.giphy4j.Giphy;
import at.mukprojects.giphy4j.entity.search.SearchRandom;
import at.mukprojects.giphy4j.exception.GiphyException;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

public class GifCommand extends Command {

    private static final String API_KEY = "dc6zaTOxFJmzC";

    public GifCommand() {
        super("gif", "Prints a random gif!");
    }

    @Override
    public void execute(Message msg, String[] args) {
        MessageChannel channel = msg.getChannel();
        Giphy giphy = new Giphy(API_KEY);

        try {
            SearchRandom giphySearch = giphy.searchRandom(args.length > 0 ? args[0] : "");

            channel.sendMessage(new MessageBuilder()
                    .appendMention(msg.getAuthor())
                    .appendString(args.length > 0 ? " (" + args[0] + ")\n" : "\n")
                    .appendString(giphySearch.getData().getImageOriginalUrl()).build())
                    .queue();
        } catch (GiphyException e) {
            channel.sendMessage(new MessageBuilder()
                    .appendMention(msg.getAuthor())
                    .appendString(args.length > 0 ? " (" + args[0] + ")\n" : "\n")
                    .appendString("No gif found.").build())
                    .queue();
        }

        if(channel.getType() == ChannelType.TEXT) {
            TextChannel tc = (TextChannel) channel;
            Guild guild = tc.getGuild();

            if(guild.getSelfMember().hasPermission(tc, Permission.MESSAGE_MANAGE))
                msg.deleteMessage().queue();
        }
    }

}
