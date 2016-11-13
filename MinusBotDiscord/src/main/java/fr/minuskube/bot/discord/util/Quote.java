package fr.minuskube.bot.discord.util;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.FontMetrics;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Quote {

    private static final Logger LOGGER = LoggerFactory.getLogger(Quote.class);

    private TextChannel channel;
    private Member author;
    private String msg;
    private Date date;

    public Quote(TextChannel channel, Member author, String msg, Date date) {
        this.channel = channel;
        this.author = author;
        this.msg = msg;
        this.date = date;
    }

    public void send() {
        Webhook webhook = Webhook.getBotHook(channel);

        if(webhook == null) {
            channel.sendMessage("Error while creating quote!");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Color color = author.getColor();
        int rgb =   ((color.getRed() & 0xFF) << 16) |
                    ((color.getGreen() & 0xFF) << 8)  |
                    ((color.getBlue() & 0xFF));

        JSONObject embed = new JSONObject(new HashMap<String, Object>() {
            {
                put("color", rgb);
                put("description", msg);
                put("timestamp", dateFormat.format(date));
            }
        });

        boolean sent = webhook.execute(new JSONObject(new HashMap<String, Object>() {
            {
                put("username", author.getEffectiveName());
                put("avatar_url", author.getUser().getAvatarUrl());
                put("embeds", new JSONArray(Collections.singleton(embed)));
            }
        }));

        if(!sent)
            channel.sendMessage("Error while sending quote!");
    }

    private List<String> splitString(FontMetrics metrics, String txt) {
        int maxWidth = 275;

        List<String> result = new ArrayList<>();

        String[] lines = txt.split("\n");

        for(String line : lines) {
            String[] words = line.split(" ");
            String currentLine = words[0];

            for(int i = 1; i < words.length; i++) {
                if(words[i].contains("\n") || words[i].contains("\r")) {
                    result.add(currentLine);
                    currentLine = words[i];

                    continue;
                }

                if(metrics.stringWidth(currentLine + words[i]) < maxWidth)
                    currentLine += " " + words[i];
                else {
                    result.add(currentLine);
                    currentLine = words[i];
                }
            }

            if(currentLine.trim().length() > 0)
                result.add(currentLine);
        }

        return result;
    }

}
