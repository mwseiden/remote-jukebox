# remote-jukebox

The goal of this project is to provide a simple to use jukebox app that can run on an
Android device.

I am using it by hanging it on the wall and connecting to my stereo via bluetooth.

It also includes Twitch bot capabilities.

It requires an Android device with a settings button and a back button.

## Configuration

The configuration is accessed by hitting the menu button and selecting `Settings`. When
done, hit the back button to save and return to the main screen.

### Basic Configuration

The bare minimum to get up and running

#### Base Folder

Opens a file browser. Select the root directory with your audio files.

#### Refresh Database

Reloads the song information from the file system and saves it to the internal database.
This needs to be done whenever music is added or removed.

### Advanced Configuration

#### Allowed Genres

White lists genres that can be played. The jukebox supports multiple genres if they are
separated with a semicolon.

**Unselecting all genres means don't use a filter**

#### Disallowed Genres

Black lists genres that shouldn't be played. This is applied after the white list.

#### Clear Database

Wipes out the internal database.

### Twitch Bot Configuration

There is support for connecting to Twitch chat for requests. Every time the settings
screen is closed the bot will rejoin chat or quit chat if the bot was disabled.

#### Twitch Bot Enabled

Turns the bot on or off.

#### Twitch Account

The account name that the bot will run under.

#### Twitch Password

Your oauth password from [here](http://twitchapps.com/tmi). You can specify your password
with or without the `oauth:` header.

#### Twitch Channel

The channel for the bot to join and monitor for requests.

#### Don't Filter Requests

If checked then the white and black lists are ignored for requests. Otherwise, only the
current subset of songs will be available to your viewers.

#### Seconds Between Requests

Puts a limit on how often users can request a song.

#### Generate Playlist

Creates a list of all available bands and copies them to the clipboard. They can then be
posted on the website of your choice, such as PasteBin.

#### URL of Playlist

The URL of the list of bands. This is mentioned at intervals by the bot or via the ?help
command. If blank then this message will be skipped.

## Using The Jukebox

### Pause/Play

Hit the Pause button and the music will pause. The button will turn into the Play button.
Hitting the Play button will resume the music.

### Next

Hit the Next button to skip to the next song.

### Music

Hit the Music button to bring up a list of available songs. It will start at the Artist
level. Clicking on the name of an Artist will go to the Album level. Clicking on the Check
Box will select everything by that artist. Hit the back button to abandon your selections.

At the Album level you can click on the album name to get to the Song level. Clicking on
the Check Box will select all songs on that Album. Hitting the back button will go back to
Artist view.

At the Song level you can click on the Check Box to select individual songs. Hitting the
back button will go back to the Album view.

Songs are selected to be queued in the order they are checked. In order to queue the songs
you must click on the queue songs button at the bottom. Hitting the back button from
Artist view will cancel all selections.

### Clearing the Queue

You can clear the queue by hitting the menu button and selecting `Clear Queue`.

## Twitch Bot Commands

### Public Commands

?help
?request [SONG/BAND/GENRE]
?request [SONG] ?by [BAND]
?song
?album

### Moderator Commands

?next
?clear

## Bugs and Future Features

* The handling of added songs needs to be better. Right now you have to wipe the DB and reload everything on every change.
* Sometimes it crashes after reloading the database.


