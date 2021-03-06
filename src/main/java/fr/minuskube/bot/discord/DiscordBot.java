package fr.minuskube.bot.discord;

import com.google.gson.Gson;
import fr.minuskube.bot.discord.comics.CommitStrip;
import fr.minuskube.bot.discord.commands.AddCommand;
import fr.minuskube.bot.discord.commands.ClearCommand;
import fr.minuskube.bot.discord.commands.ComicsCommand;
import fr.minuskube.bot.discord.commands.DrawCommand;
import fr.minuskube.bot.discord.commands.FakeQuoteCommand;
import fr.minuskube.bot.discord.commands.GamesCommand;
import fr.minuskube.bot.discord.commands.GifCommand;
import fr.minuskube.bot.discord.commands.HelpCommand;
import fr.minuskube.bot.discord.commands.InfosCommand;
import fr.minuskube.bot.discord.commands.MuteCommand;
import fr.minuskube.bot.discord.commands.PollCommand;
import fr.minuskube.bot.discord.commands.PresetCommand;
import fr.minuskube.bot.discord.commands.QuoteCommand;
import fr.minuskube.bot.discord.commands.StopCommand;
import fr.minuskube.bot.discord.commands.SuggestCommand;
import fr.minuskube.bot.discord.commands.TestCommand;
import fr.minuskube.bot.discord.commands.TextCommand;
import fr.minuskube.bot.discord.games.BoxesGame;
import fr.minuskube.bot.discord.games.ConnectFourGame;
import fr.minuskube.bot.discord.games.NumberGame;
import fr.minuskube.bot.discord.games.RPSGame;
import fr.minuskube.bot.discord.games.TicTacToeGame;
import fr.minuskube.bot.discord.listeners.CommandListener;
import fr.minuskube.bot.discord.listeners.GameListener;
import fr.minuskube.bot.discord.listeners.MuteListener;
import fr.minuskube.bot.discord.listeners.PollListener;
import fr.minuskube.bot.discord.listeners.QuoteListener;
import fr.minuskube.bot.discord.trello.TCPServer;
import fr.minuskube.bot.discord.util.Webhook;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public class DiscordBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);
    public static final String PRIVATE_NOT_ALLOWED = "You can't do this in a private channel.";

    private static DiscordBot instance;

    private JDA client;
    private LocalDateTime launchTime;

    private Config config = new Config();
    private Gson gson = new Gson();
    private CommitStrip commitStrip;

    private void loadConfig() {
        LOGGER.info("Loading config...");
        File configFile = new File("config.txt");

        if(!configFile.exists()) {
            LOGGER.warn("Config file doesn't not exist, creating a new one.");

            try {
                config.saveDefault(configFile);
            } catch(IOException e) {
                LOGGER.error("Error while creating the default file: ", e);
            }
        }
        else
            config.load(configFile);
    }

    public void ready(JDA client) {
        this.client = client;

        LOGGER.info("Connected on " + client.getGuilds().size() + " guilds with "
                + client.getUsers().size() + " users!");

        for(Guild guild : client.getGuilds())
            LOGGER.info("  - " + guild.getName() + " (" + guild.getMembers().size() + " users)");

        LOGGER.info("Starting server...");
        new TCPServer().start();

        LOGGER.info("Starting CommitStrip timer...");
        commitStrip = new CommitStrip();
        commitStrip.load();
        commitStrip.start();

        LOGGER.info("Registering commands...");
        DiscordBotAPI.registerCommands(
                new HelpCommand(),
                new InfosCommand(),
                new AddCommand(),
                new SuggestCommand(),
                new GifCommand(),
                new QuoteCommand(),
                new FakeQuoteCommand(),
                new GamesCommand(),
                new TestCommand(),
                new StopCommand(),
                new DrawCommand(),
                new MuteCommand(),
                new PollCommand(),
                new ClearCommand(),
                new ComicsCommand(),
                new TextCommand(),
                new PresetCommand()
        );

        LOGGER.info("Registering games...");
        DiscordBotAPI.registerGames(
                new NumberGame(),
                new TicTacToeGame(),
                new RPSGame(),
                new ConnectFourGame(),
                new BoxesGame()
        );

        LOGGER.info("Registering listeners...");
        client.addEventListener(
                new CommandListener(this),
                new GameListener(this),
                new MuteListener(this),
                new PollListener(this),
                new QuoteListener(this)
        );

        LOGGER.info("Initializing webhooks...");
        Webhook.initBotHooks();
        LOGGER.info("Initialized " + Webhook.getBotHooks().size() + " webhooks.");

        if(!config.isSelf()) {
            LOGGER.info("Setting status...");
            client.getPresence().setGame(Game.of(DiscordBotAPI.prefix() + "help - v1.8.0"));
        }

        launchTime = LocalDateTime.now();
        LOGGER.info("MinusBot (Discord) is ready!");
    }

    public void stop() {
        commitStrip.cancel();

        DiscordBotAPI.logout();
        client = null;

        System.exit(0);
    }

    public JDA getClient() { return client; }
    public User getOwner() { return client.getUserById("87941393766420480"); }
    public LocalDateTime getLaunchTime() { return launchTime; }

    public Config getConfig() { return config; }
    public Gson getGson() { return gson; }
    public CommitStrip getCommitStrip() { return commitStrip; }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        try {
            instance = new DiscordBot();
            instance.loadConfig();

            String token = instance.getConfig().getToken();

            if(token != null)
                DiscordBotAPI.login(instance.getConfig().isSelf(), token);
            else
                LOGGER.error("The 'token' is not set in the config file, can't start.");

            Runtime.getRuntime().addShutdownHook(new Thread(instance::stop));
        } catch(LoginException | InterruptedException | RateLimitedException e) {
            LOGGER.error("Error while login: ", e);
        }
    }

    public static DiscordBot instance() { return instance; }

}
