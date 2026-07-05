import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ModalMenu extends ListenerAdapter {
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().equals("channelaccess")) {
            long channelID = Long.parseLong(event.getValue("channelID").getAsString());
            for (GuildChannel channel : event.getGuild().getChannels()) {

            }
        }
    }
}
