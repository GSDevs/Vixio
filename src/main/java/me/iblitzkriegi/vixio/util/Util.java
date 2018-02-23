package me.iblitzkriegi.vixio.util;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.variables.Variables;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import me.iblitzkriegi.vixio.Vixio;
import me.iblitzkriegi.vixio.effects.EffLogin;
import me.iblitzkriegi.vixio.util.enums.SearchSite;
import me.iblitzkriegi.vixio.util.wrapper.Bot;
import me.iblitzkriegi.vixio.util.wrapper.Emoji;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.bukkit.event.Event;
import ch.njol.skript.log.HandlerList;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.logging.Level;

public class Util {

    private static final Field VARIABLE_NAME;
    private static final Field LOG_HANDLERS;
    private static boolean variableNameGetterExists = Skript.methodExists(Variable.class, "getName");

    static {

        if (!variableNameGetterExists) {

            Field _VARIABLE_NAME = null;
            try {
                _VARIABLE_NAME = Variable.class.getDeclaredField("name");
                _VARIABLE_NAME.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                Skript.error("Skript's 'variable name' method could not be resolved.");
            }
            VARIABLE_NAME = _VARIABLE_NAME;

        } else {
            VARIABLE_NAME = null;
        }

    }

    static {

        Field _LOG_HANDLERS = null;
        try {
            _LOG_HANDLERS = SkriptLogger.class.getDeclaredField("handlers");
            _LOG_HANDLERS.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Skript.error("Skript's 'log handler' field could not be resolved.");
        }
        LOG_HANDLERS = _LOG_HANDLERS;

    }

    private static YoutubeSearchProvider youtubeSearchProvider =
            new YoutubeSearchProvider(
                    new YoutubeAudioSourceManager(false)
            );

    public static DefaultAudioPlayerManager defaultAudioPlayerManager = new DefaultAudioPlayerManager();
    private static SoundCloudAudioSourceManager soundCloudSearchProvider = new SoundCloudAudioSourceManager(true);

    // Variable name related code credit btk5h (https://github.com/btk5h)
    public static VariableString getVariableName(Variable<?> var) {
        if (variableNameGetterExists) {
            return var.getName();
        } else {
            try {
                return (VariableString) VARIABLE_NAME.get(var);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean equalsAnyIgnoreCase(String toMatch, String... potentialMatches) {
        return Arrays.asList(potentialMatches).contains(toMatch);
    }

    public static AudioTrack[] search(SearchSite site, String[] queries) {
        List<AudioTrack> results = new ArrayList<>();
        AudioItem playlist = null;

        for (String query : queries) {

            switch (site) {

                case YOUTUBE:
                    playlist = youtubeSearchProvider.loadSearchResult(query);
                    break;

                case SOUNDCLOUD:
                    playlist = soundCloudSearchProvider.loadItem(
                            defaultAudioPlayerManager,
                            new AudioReference("scsearch:" + query, null)
                    );
                    break;

            }

            if (playlist instanceof AudioPlaylist) {
                results.addAll(((AudioPlaylist) playlist).getTracks());
            }

        }

        return results.isEmpty() ? null :
                results.toArray(new AudioTrack[results.size()]);

    }

    public static void setList(String name, Event e, boolean local, Object... objects) {
        if (objects == null || name == null) return;

        int separatorLength = Variable.SEPARATOR.length() + 1;
        name = name.substring(0, (name.length() - separatorLength));
        name = name.toLowerCase(Locale.ENGLISH) + Variable.SEPARATOR;
        Variables.setVariable(name + "*", null, e, local);
        for (int i = 0; i < objects.length; i++)
            Variables.setVariable(name + (i + 1), objects[i], e, local);
    }

    public static Color getColorFromString(String str) {
        if (str == null) return null;

        Color color = null;
        try {
            color = (Color) Color.class.getField(str.toUpperCase(Locale.ENGLISH).replace(" ", "_")).get(null);
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e1) {
            Skript.exception(e1);
        }

        return color;

    }

    public static Bot botFrom(Object input){
        if (input == null) {
            return null;
        } else if (input instanceof Bot) {
            return (Bot) input;
        } else if (input instanceof String) {
            return Vixio.getInstance().botNameHashMap.get(input);
        }
        return null;
    }

    public static Message messageFrom(Object input) {
        if (input instanceof Message) {
            return (Message) input;
        } else if (input instanceof String) {
            try {
                return new MessageBuilder()
                        .setContent((String) input)
                        .build();
            } catch (IllegalStateException | IllegalArgumentException x) {
                return null;
            }
        }
        return null;
    }

    public static boolean botIsConnected(Bot bot, JDA jda){
        return bot.getJDA() == jda;
    }

    public static Guild bindGuild(Bot bot, Guild guild) {
        if (!(guild.getJDA() == bot.getJDA())) {
            return bot.getJDA().getGuildById(guild.getId());
        } else {
            return guild;
        }
    }

    public static TextChannel bindChannel(Bot bot, TextChannel textChannel) {
        if (!(textChannel.getJDA() == bot.getJDA())) {
            return bot.getJDA().getTextChannelById(textChannel.getId());
        } else {
            return textChannel;
        }
    }


    public static VoiceChannel bindVoiceChannel(Bot bot, VoiceChannel voiceChannel) {
        if (!(voiceChannel.getJDA() == bot.getJDA())) {
            return bot.getJDA().getVoiceChannelById(voiceChannel.getId());
        } else {
            return voiceChannel;
        }
    }

    public static Channel bindChannel(Bot bot, Channel channel) {
        if (!(channel.getJDA() == bot.getJDA())) {
            TextChannel textChannel = bot.getJDA().getTextChannelById(channel.getId());
            VoiceChannel voiceChannel = bot.getJDA().getVoiceChannelById(channel.getId());

            return voiceChannel == null ? textChannel : voiceChannel;
        } else {
            return channel;
        }
    }

    public static Emoji unicodeFrom(String emote, Guild guild) {
        try {
            if (EmojiManager.isEmoji(emote)) {
                return new Emoji(emote);
            }
            Collection<Emote> emotes = guild.getEmotesByName(emote, false);
            return emotes.isEmpty() ? new Emoji(EmojiParser.parseToUnicode(":" + emote + ":")) : new Emoji(emotes.iterator().next());
        }catch (UnsupportedOperationException | NoSuchElementException x) {
            return null;
        }
    }

    public static Emoji unicodeFrom(String emote) {
        if (EmojiManager.isEmoji(emote)) {
            return new Emoji(emote);
        } else {
            return new Emoji(EmojiParser.parseToUnicode(emote));
        }
    }

    public static Bot randomBot() {
        Collection<Bot> bots = Vixio.getInstance().botHashMap.values();
        return bots.isEmpty() ? null : bots.toArray(new Bot[bots.size()])[new Random().nextInt(bots.size())];
    }

    public static HandlerList getLogHandlers() {
        try {
            return (HandlerList) LOG_HANDLERS.get(null);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static void setLogHandlers(HandlerList logHandlers) {
        try {
            LOG_HANDLERS.set(null, logHandlers);
        } catch (IllegalAccessException e) {
        }
    }

    public static void forceError(String error) {
        HandlerList originalHandlers = getLogHandlers();
        setLogHandlers(new HandlerList());
        Skript.error(error);
        setLogHandlers(originalHandlers);
    }

}

