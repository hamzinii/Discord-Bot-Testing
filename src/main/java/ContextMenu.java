import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;

public class ContextMenu extends ListenerAdapter {

    public void onUserContextInteraction(UserContextInteractionEvent event) {
        switch (event.getName()) {
            case "Get user avatar":
                event.reply(event.getTarget().getEffectiveAvatarUrl()   ).setEphemeral(true).queue();
                break;

            case "amogus bomb":
                String[] lettersToSend = new String[] {"a", "m", "o", "n", "g", "** **", "u", "s"};

                if (event.getTarget().getIdLong() != DiscordBot.getJDA().getSelfUser().getIdLong()) {
                    for (String letter : lettersToSend) {
                        DirectMessenger.sendMessageToUser(event.getTarget().getIdLong(), letter);
                    }
                    DirectMessenger.sendMessageToUser(event.getTarget().getIdLong(), "^ Courtesies of <@" + event.getUser().getIdLong() + ">");

                    event.reply("thumbs up").setEphemeral(true).queue();
                }
                else {
                    for (String letter : lettersToSend) {
                        DirectMessenger.sendMessageToUser(event.getUser().getIdLong(), letter);
                    }

                    event.reply("nope. right back at ya").setEphemeral(true).queue();
                }
                break;

            case "Random VC":
                if (moveRandomly(event.getGuild(), event.getTargetMember())) {
                    event.reply("yup probly").setEphemeral(true).queue();
                } else {
                    event.reply("failed for some reason -- hopefully it's because the user is not in a vc").setEphemeral(true).queue();
                }
        }
    }

    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        if (event.getName().equals("Count words")) {
            event.reply("> " + event.getTarget().getJumpUrl() + "\nWords: " + event.getTarget().getContentRaw().split("\\s+").length).setEphemeral(true).queue();
        }
    }

    public boolean moveRandomly(Guild guild, Member member) {
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null && voiceState.getChannel() != null) {
            for (int i = 0; i < 10; i++) {
                try {
                    guild.moveVoiceMember(member, guild.getVoiceChannels().get((int) (Math.random() * guild.getVoiceChannels().size()))).queueAfter(i * 5, TimeUnit.SECONDS);
                } catch (ErrorResponseException ignored) {}
            }
            return true;
        }

        return false;
    }
}