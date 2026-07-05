import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.managers.Presence;
import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DiscordBot {

    private static JDA jda;

    public static void main(String[] args) throws InterruptedException {
        JDABuilder api = JDABuilder.createDefault(System.getenv("token"));

        api.addEventListeners(new Messenger(), new SlashCommands(), new ContextMenu(), new ModalMenu());
        api.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES);
        api.setMemberCachePolicy(MemberCachePolicy.ALL);
        api.setChunkingFilter(ChunkingFilter.ALL);
        api.setStatus(OnlineStatus.DO_NOT_DISTURB);

        jda = api.build();

        // Shutdown hook to close bot
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            jda.shutdownNow();
            System.out.println("Shutdown hook ran!");
        }));

        readActivityFromJSON("status.json");
        applicationCommands();
        jda.awaitReady();
    }

    public static void readActivityFromJSON(String fileName) {
        try {
            String contentFromFile = new String(Files.readAllBytes(Paths.get(fileName)));
            JSONObject activity = new JSONObject(contentFromFile);

            if (activity.has("Activity Type") && activity.has("Status")) {
                String type = activity.getString("Activity Type");
                String status = activity.getString("Status");
                setActivity(type, status);
            }

        } catch (IOException | JSONException e) {
            System.out.println(e.fillInStackTrace().toString());
        }
    }

    public static void writeActivityToJSON(String activity, String status) {
        JSONObject newStatus = new JSONObject();
        newStatus.put("Activity Type", activity);
        newStatus.put("Status", status);

        String fileName = "status.json";
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(newStatus.toString());
            file.flush();
        }
        catch (IOException e) {
            System.out.println(e.fillInStackTrace().toString());
        }
    }

    public static void setActivity(String activity, String status) {
        Presence presence = jda.getPresence();

        switch (activity) {
            case "playing"   -> presence.setActivity(Activity.playing(status));
            case "competing" -> presence.setActivity(Activity.competing(status));
            case "listening" -> presence.setActivity(Activity.listening(status));
            case "streaming" -> presence.setActivity(Activity.streaming(status, "https://twitch.tv/innerslothdevs"));
            case "custom"    -> presence.setActivity(Activity.customStatus(status));
        }
    }

    public static void applicationCommands() {
        String botName = jda.getSelfUser().getName();
        jda.updateCommands().addCommands( // can take upwards of an hour
            /*---------------------
                 Slash Commands
            ---------------------*/
            // say command
            Commands.slash("say", "Make " + botName + " say a message")
                    .addOption(STRING, "content", "Message for the bot to repeat", true)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND)),

            // set status command
            Commands.slash("setactivity", "Change " + botName + "'s activity status")
                    .addOptions(new OptionData(STRING,"activity", "the type of status",true)
                            .addChoices(new Command.Choice("playing","playing"))
                            .addChoices(new Command.Choice("competing in","competing"))
                            .addChoices(new Command.Choice("listening to", "listening"))
                            .addChoices(new Command.Choice("streaming", "streaming"))
                            .addChoices(new Command.Choice("custom status", "custom")))
                    .addOptions(new OptionData(STRING, "content", "Message to be displayed alongside the activity type", true)
                            .setMaxLength(128)),

            // join voice channel
            Commands.slash("joinvc", "Have " + botName + " join a voice channel")
                    .addOptions(new OptionData(CHANNEL, "vc", "Name of the voice channel", true)
                            .setChannelTypes(ChannelType.VOICE))
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VIEW_CHANNEL)),

            // leave voice channel
            Commands.slash("leavevc","Have " + botName + " leave the voice channel in this server")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VIEW_CHANNEL)),

            // time --> discord time stamp converter
                Commands.slash("timestampconvert", "Converts a time to the Discord timestamp format")
                        .addOptions(new OptionData(INTEGER, "month", "1-12", true)
                                .setMinValue(1)
                                .setMaxValue(12))
                        .addOptions(new OptionData(INTEGER, "day", "1-31", true)
                                .setMinValue(1)
                                .setMaxValue(31))
                        .addOptions(new OptionData(INTEGER, "year", "any year", true))
                        .addOptions(new OptionData(INTEGER, "hour", "0-23 (Military time). Defaults to 0", false)
                                .setMinValue(0)
                                .setMaxValue(23))
                        .addOptions(new OptionData(INTEGER, "minute", "0-59. Defaults to 0", false)
                                .setMinValue(0)
                                .setMaxValue(59))
                        .addOptions(new OptionData(INTEGER, "second", "0-59. Defaults to 0", false)
                                .setMinValue(0)
                                .setMaxValue(59))
                        .addOptions(new OptionData(NUMBER, "utc", "Time zone offset from UTC standard. Defaults to -4 for EDT", false)
                                .setMinValue(-12)
                                .setMaxValue(14)),

            // ping
            Commands.slash("ping", "Returns the bot's latency"),

            // source code
            Commands.slash("source", "View the source code for " + botName),

            // get channels
            Commands.slash("getchannels", "channels bot has access to"),

            /*---------------------
             Context Menu Commands
            ---------------------*/
            // Get User Avatar
            Commands.context(Command.Type.USER, "Get user avatar"),

            // Amogus bomb
            Commands.context(Command.Type.USER, "amogus bomb"),

            // Count Words
            Commands.message("Count words"),

            // randomize the vc the user is in... on loop (ohno)
            Commands.user("Random VC")

        ).queue();
    }
    public static JDA getJDA() {
        return jda;
    }
}