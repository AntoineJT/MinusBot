package fr.minuskube.bot.discord.trello;

import fr.minuskube.bot.discord.DiscordBot;
import org.json.JSONObject;

import java.io.Serializable;

public class Board extends Component implements Serializable {

    private String name;
    private String desc;
    private String shortLink;
    private boolean closed;

    public String getName() { return name; }
    public String getDescription() { return desc; }
    public String getShortLink() { return shortLink; }
    public boolean isClosed() { return closed; }

    public static Board from(JSONObject obj) {
        return DiscordBot.instance().getGson().fromJson(obj.toString(), Board.class);
    }

}
