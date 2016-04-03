package com.theducksparadise.jukebox;

import android.content.Context;
import android.os.Handler;

import com.theducksparadise.jukebox.domain.Artist;
import com.theducksparadise.jukebox.domain.Song;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwitchBot extends ListenerAdapter {

    private static final int CYCLES_UNTIL_TALKING = 120;

    private static final String HELP_MESSAGE = "Use commands ?request [BAND] to make a request or ?song to see what's playing.";

    private static volatile TwitchBot instance;

    private Context context;

    private PircBotX bot;

    private String accountName;

    private String channel;

    private List<String> messages = new ArrayList<>();

    private Handler handler;

    private Runnable runnable;

    private Map<String, Date> userRequests;

    private int cyclesWithoutTalking;

    private boolean allowAllRequests;

    private int secondsBetweenRequests;

    private String requestLimitMessage;

    public static TwitchBot getInstance(Context context) {
        if (instance == null) {
            synchronized (TwitchBot.class) {
                if (instance == null) {
                    instance = new TwitchBot(context);
                }
            }
        }

        return instance;
    }

    private TwitchBot(Context context) {
        if (instance != null && instance.bot != null) {
            if (instance.handler != null) instance.handler.removeCallbacks(instance.runnable);
            instance.bot.stopBotReconnect();
            instance.bot.close();
        }

        userRequests = new HashMap<>();

        cyclesWithoutTalking = 0;

        allowAllRequests = context.getSharedPreferences(SettingsActivity.PREFERENCE_FILE, Context.MODE_PRIVATE).getBoolean(SettingsActivity.PREFERENCE_KEY_TWITCH_REQUEST_ALL, false);

        secondsBetweenRequests = context.getSharedPreferences(SettingsActivity.PREFERENCE_FILE, Context.MODE_PRIVATE).getInt(SettingsActivity.PREFERENCE_KEY_TWITCH_REQUEST_LIMIT, 300);
        Integer minutes = secondsBetweenRequests / 60;
        Integer seconds = secondsBetweenRequests % 60;
        requestLimitMessage = "Only one request every ";
        if (minutes > 0) {
            requestLimitMessage += minutes.toString() + " minute" + (minutes > 1 ? "s " : " ") + (seconds > 0 ? "and " : "");
        }
        if (seconds > 0) {
            requestLimitMessage += seconds.toString() + " second" + (seconds > 1 ? "s " : " ");
        }
        requestLimitMessage += "per user!";

        if (context.getSharedPreferences(SettingsActivity.PREFERENCE_FILE, Context.MODE_PRIVATE).getBoolean(SettingsActivity.PREFERENCE_KEY_TWITCH_ENABLED, false)) {

            accountName = context.getSharedPreferences(SettingsActivity.PREFERENCE_FILE, Context.MODE_PRIVATE).getString(SettingsActivity.PREFERENCE_KEY_TWITCH_ACCOUNT, "");
            String account = accountName.toLowerCase();
            String password = context.getSharedPreferences(SettingsActivity.PREFERENCE_FILE, Context.MODE_PRIVATE).getString(SettingsActivity.PREFERENCE_KEY_TWITCH_PASSWORD, "");
            channel = context.getSharedPreferences(SettingsActivity.PREFERENCE_FILE, Context.MODE_PRIVATE).getString(SettingsActivity.PREFERENCE_KEY_TWITCH_CHANNEL, "").toLowerCase();

            // https://github.com/TheLQ/pircbotx/wiki/Twitch.tv-support
            Configuration config = new Configuration.Builder()
                // Twitch doesn't support multiple users
                .setAutoNickChange(false)
                // Twitch doesn't support WHO command
                .setOnJoinWhoEnabled(false)
                .setCapEnabled(true)
                .setAutoReconnect(true)
                // Twitch by default doesn't send JOIN, PART, and NAMES unless you request it,
                // see https://github.com/justintv/Twitch-API/blob/master/IRC.md#membership
                .addCapHandler(new EnableCapHandler("twitch.tv/membership"))
                .addServer("irc.twitch.tv")
                // Your twitch.tv username
                .setName(account)
                // Your oauth password from http://twitchapps.com/tmi
                .setServerPassword("oauth:" + password)
                // Some twitch channel
                .addAutoJoinChannel("#" + channel)
                .addListener(this)
                .buildConfiguration();

            bot = new PircBotX(config);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        bot.startBot();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            this.context = context;

            runnable = new Runnable() {
                @Override
                public void run() {
                    int delay = 5000;

                    synchronized (TwitchBot.class) {
                        if (bot != null) {
                            if (messages.size() > 0) {
                                String message = messages.get(0);
                                bot.send().message("#" + channel, message);
                                messages.remove(0);
                            } else {
                                cyclesWithoutTalking++;
                                if (cyclesWithoutTalking > CYCLES_UNTIL_TALKING) {
                                    addMessage(HELP_MESSAGE);
                                    cyclesWithoutTalking = 0;
                                }
                            }

                            if (messages.size() > 0) delay = 2000;
                        }
                    }

                    handler.postDelayed(this, delay);
                }
            };
        } else {
            bot = null;
        }
    }

    public static void reconfigure(Context context) {
        synchronized (TwitchBot.class) {
            if (instance != null && instance.bot != null) {
                instance.handler.removeCallbacks(instance.runnable);
                instance.bot.stopBotReconnect();
                instance.bot.close();
            }
            instance = new TwitchBot(context);
        }
    }

    public void setHandler(Handler handler) {
        this.handler = handler;

        this.handler.postDelayed(runnable, 5000);
    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        super.onMessage(event);

        if (event.getUser() == null) return;

        String message = event.getMessage();
        if (message.toLowerCase().startsWith("?request ")) {
            try {
                Date lastRequest = userRequests.get(event.getUser().getNick());

                if (lastRequest == null || lastRequest.getTime() < new Date().getTime() - (1000 * secondsBetweenRequests)) {
                    String band = message.substring(9).trim();
                    Artist artist = MusicDatabase.getInstance(context).getArtistCaseInsensitive(band, !allowAllRequests);
                    if (artist != null) {
                        ArrayList<Song> songs = (ArrayList<Song>) artist.getSongsForQueue();
                        List<Song> requestedSong = new ArrayList<>();
                        requestedSong.add(songs.get((int) (Math.random() * songs.size())));
                        JukeboxMedia.getInstance().addToQueue(requestedSong);
                        addMessage("Queued " + requestedSong.get(0).getName() + " by " + requestedSong.get(0).getAlbum().getArtist().getName() + " for " + event.getUser().getNick());
                        userRequests.put(event.getUser().getNick(), new Date());
                    } else {
                        addMessage("We don't have anything by " + band);
                    }
                } else {
                    addMessage(requestLimitMessage);
                }
            } catch (Exception e) {
                // dat is bad
            }
        } else if (message.equalsIgnoreCase("?song")) {
            Song song = JukeboxMedia.getInstance().getCurrentSong();

            if (song != null) {
                addMessage("We are now listening to " + song.getName() + " by " + song.getAlbum().getArtist().getName() + ", matey!");
            } else {
                addMessage("Nothing is playing, ye scurvy landlubber!");
            }
        } else if (message.equalsIgnoreCase("?album")) {
            Song song = JukeboxMedia.getInstance().getCurrentSong();

            if (song != null) {
                addMessage("This song by " + song.getAlbum().getArtist().getName() + " is on the " + song.getAlbum().getName() + " album, matey!");
            } else {
                addMessage("Nothing is playing, ye scurvy landlubber!");
            }
        }
    }

    @Override
    public void onConnect(ConnectEvent event) throws Exception {
        addMessage("Ahoy Matey! " + accountName + " reporting for sea shanty duty!");
        addMessage(HELP_MESSAGE);
    }

    private void addMessage(String message) {
        synchronized (TwitchBot.class) {
            if (!messages.contains(message)) messages.add(message);
        }
    }
}
