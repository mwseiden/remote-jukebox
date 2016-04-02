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

    private static volatile TwitchBot instance;

    private Context context;

    private PircBotX bot;

    private String account;

    private String password;

    private String channel;

    private List<String> messages = new ArrayList<>();

    private Handler handler;

    private Runnable runnable;

    private Map<String, Date> userRequests;

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
            instance.bot.close();
        }

        userRequests = new HashMap<>();

        if (context.getSharedPreferences(SettingsActivity.PREFERENCE_FILE, Context.MODE_PRIVATE).getBoolean(SettingsActivity.PREFERENCE_KEY_TWITCH_ENABLED, false)) {

            account = context.getSharedPreferences(SettingsActivity.PREFERENCE_FILE, Context.MODE_PRIVATE).getString(SettingsActivity.PREFERENCE_KEY_TWITCH_ACCOUNT, "").toLowerCase();
            password = context.getSharedPreferences(SettingsActivity.PREFERENCE_FILE, Context.MODE_PRIVATE).getString(SettingsActivity.PREFERENCE_KEY_TWITCH_PASSWORD, "");
            channel = context.getSharedPreferences(SettingsActivity.PREFERENCE_FILE, Context.MODE_PRIVATE).getString(SettingsActivity.PREFERENCE_KEY_TWITCH_CHANNEL, "").toLowerCase();

            // https://github.com/TheLQ/pircbotx/wiki/Twitch.tv-support
            Configuration config = new Configuration.Builder()
                // Twitch doesn't support multiple users
                .setAutoNickChange(false)
                // Twitch doesn't support WHO command
                .setOnJoinWhoEnabled(false)
                .setCapEnabled(true)
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
                    synchronized (TwitchBot.class) {
                        if (bot != null) {
                            if (messages.size() > 0) {
                                String chat = "";
                                for (String message : messages) chat += message + " ";
                                try {
                                    bot.send().message("#" + channel, chat);
                                } catch (Exception e) {
                                    // not good
                                }
                            }
                            messages.clear();
                        }
                    }

                    handler.postDelayed(this, 5000);
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

                if (lastRequest == null || lastRequest.getTime() < new Date().getTime() - (1000 * 60 * 5)) {
                    String band = message.substring(9).trim();
                    Artist artist = MusicDatabase.getInstance(context).getArtistCaseInsensitive(band);
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
                    addMessage("Only one request every 5 minutes per user!");
                }
            } catch (Exception e) {
                // dat is bad
            }
        } else if (message.equalsIgnoreCase("?song")) {
            Song song = JukeboxMedia.getInstance().getCurrentSong();

            if (song != null) {
                addMessage("We are now listening to " + song.getName() + " by " + song.getAlbum().getArtist().getName() + " matey!");
            } else {
                addMessage("Nothing is playing, ye scurvy landlubber!");
            }
        }
    }

    @Override
    public void onConnect(ConnectEvent event) throws Exception {
        addMessage("Ahoy Matey! " + account + " reporting for sea shanty duty! Use commands ?request [BAND] to make a request or ?song to see what's playing.");
    }

    private void addMessage(String message) {
        synchronized (TwitchBot.class) {
            messages.add(message);
        }
    }
}
