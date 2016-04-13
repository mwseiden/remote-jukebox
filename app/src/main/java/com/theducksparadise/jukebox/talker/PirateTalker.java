package com.theducksparadise.jukebox.talker;

import java.util.Random;

public class PirateTalker implements Talker {

    private static String[] verbs = new String[] {
            "abandon",
            "adventure",
            "assault",
            "attack",
            "battle",
            "behead",
            "capsize",
            "capture",
            "challenge",
            "confiscate",
            "curse",
            "escape",
            "explore",
            "fight",
            "hijack",
            "hook",
            "hold",
            "keelhaul",
            "kidnap",
            "kill",
            "lash",
            "lookout",
            "maroon",
            "menace",
            "navigate",
            "parley",
            "pillage",
            "plunder",
            "prowl",
            "quest",
            "raid",
            "ransack",
            "revolt",
            "roam",
            "rob",
            "sabotage",
            "sail",
            "ship",
            "steal",
            "torture",
            "trade",
            "vandalize",
            "vanquish",

    };

    private static String[] nouns = new String[] {
            "adventure",
            "anchor",
            "armada",
            "arms",
            "bandanna",
            "bandit",
            "bandolier",
            "barrel",
            "battle",
            "beach",
            "boatswain",
            "bos'n",
            "bounty",
            "brawl",
            "buccaneer",
            "cannon",
            "captain",
            "cargo",
            "cave",
            "challenge",
            "chest",
            "coast",
            "coastline",
            "coins",
            "compass",
            "conquest",
            "contraband",
            "corpse",
            "corsair",
            "course",
            "crew",
            "criminal",
            "crook",
            "crow's nest",
            "cutlass",
            "curse",
            "dagger",
            "danger",
            "dead reckoning",
            "deck",
            "deck hands",
            "desert island",
            "doubloon",
            "earring",
            "eye patch",
            "fear",
            "first mate",
            "flag",
            "fleet",
            "flotsam and jetsam",
            "fortune",
            "galleon",
            "gangplank",
            "gear",
            "gibbet",
            "gold",
            "greed",
            "grog",
            "gun",
            "gunner",
            "gunpowder",
            "haul",
            "heist",
            "high seas",
            "hook",
            "hold",
            "horizon",
            "hull",
            "hurricane",
            "island",
            "jetsam",
            "jewels",
            "Jolly Roger",
            "keel",
            "knife",
            "land",
            "landlubber",
            "legend",
            "limey",
            "Long John Silver",
            "loot",
            "lore",
            "lucre",
            "maggot",
            "malaria",
            "map",
            "marauder",
            "matiner",
            "mast",
            "mates",
            "mayhem",
            "menace",
            "merchant",
            "musket",
            "mutiny",
            "ocean",
            "old salt",
            "outcasts",
            "parrot",
            "pegleg",
            "pieces of eight",
            "pirate",
            "pistol",
            "plank",
            "plunder",
            "privateer",
            "quartermaster",
            "quarters",
            "quest",
            "raid",
            "rat",
            "rations",
            "realm",
            "reckoning",
            "revenge",
            "revolt",
            "riches",
            "rigging",
            "robber",
            "rope",
            "rudder",
            "ruffian",
            "rum",
            "sabotage",
            "sail",
            "sailor",
            "scalawag",
            "scar",
            "scurvy",
            "seas",
            "seaweed",
            "sextant",
            "ship",
            "shipmate",
            "shore",
            "silver",
            "skiff",
            "skull and bones",
            "spoils",
            "swagger",
            "sword",
            "thief",
            "thievery",
            "thug",
            "tides",
            "torture",
            "trade",
            "treachery",
            "treasure",
            "treasure island",
            "truce",
            "vessel",
            "villain",
            "violence",
            "weapons",
            "yellow fever"
    };

    private static String[] adjectives = new String[] {
            "asea",
            "ashore",
            "bad",
            "barbaric",
            "brutal",
            "cruel",
            "cutthroat",
            "daring",
            "dishonest",
            "evil",
            "fearsome",
            "ferocious",
            "greedy",
            "hostile",
            "illegal",
            "ill-gotten",
            "infamous",
            "lawless",
            "nautical",
            "New World",
            "notorious",
            "Old World",
            "overboard",
            "predatory",
            "ruthless",
            "sailing",
            "scurvy",
            "swashbuckling",
            "treacherous",
            "unlawful",
            "unscrupulous",
            "vicious",
            "vile",
            "violent"
    };

    private static String[] exclamations = new String[] {
            "ahoy",
            "arrrrr",
            "aye-aye",
            "land-ho",
            "matey",
            "shiver-me-timbers",
            "swab the deck",
            "walk the plank",
            "X marks the spot",
            "yar",
            "yo-ho-ho"
    };


    // %n : noun
    // %v : verb
    // %a : adjective
    // %d : descriptive
    private static String[] formats = new String[] {
            "%v the %n ye %d",
            "%v the %n and %v the %n! We be lookin' fer %d",
            "%d off the starboard bow",
            "yer %n was a %d",
            "dat be a %a, %a, %a, %n ye gots there",
            "did I ask ye to %v? Ye %d from the %a isles",
            "ye think ye are good 'nuf to %v me %n? %d",
            "all hands on deck! We gots a %d here",
            "that makes me want t' %v, %v, then %v me %a %n",
            "where did ye hear about me %d? I told nobody but me %n",
            "%a, %a, %a, %a, %n and %n",
            "wots dis %d ye keep %v about?",
            "I hears ye, no need to %v yer %n",
            "me favorite shanty is %v the %d by %a",
            "whatever, yer %n was a %n",
            "%a, %d and %v the %d",
            "'tis a fine ship. Except for the %n",
            "drunker than a %d who %v on a %n",
            "does this hat make me %n look %a?",
            "I hid me treasure behind the %d",
            "I heard a song about %d and it reminded me of ye and yer %a %n",
            "who %v the %n dis time?",
            "well mine is as %a as a %d",
            "me mom always said if ye can't %v then ye should %v instead",
            "did I be askin' yer opinion on %d?",
            "when we go ashore, watch out for the %d",
            "can ye just %v already?",
            "%a %n on a %a %n",
            "I not be drunk, just a %d",
            "%v the %n 'til it's %a",
            ""
    };

    @Override
    public String talk() {
        Random random = new Random();
        String response = replacePlaceholders(random, formats[random.nextInt(formats.length)], nouns, "%n");
        response = replacePlaceholders(random, response, adjectives, "%a");
        response = replacePlaceholders(random, response, verbs, "%v");

        while (response.contains("%d")) {
            response = response.replaceFirst("%d", getDescriptiveNoun(random));
        }

        return exclaim(random, response);
    }

    private String getDescriptiveNoun(Random random) {
        String descriptiveNoun = "";

        int adjectiveCount = random.nextInt(4);
        for (int i=0; i<adjectiveCount; i++) {
            if (i > 0) descriptiveNoun += ", ";
            descriptiveNoun += adjectives[random.nextInt(adjectives.length)];
        }

        return descriptiveNoun + " " + nouns[random.nextInt(nouns.length)];
    }

    private String exclaim(Random random, String string) {
        if (random.nextInt(3) == 0) string = exclamations[random.nextInt(exclamations.length)] + (string.length() > 0 ? ", " : "") + string;
        if (random.nextInt(3) == 0 && !string.endsWith("?")) string += (string.length() > 0 ? ", " : "") + exclamations[random.nextInt(exclamations.length)];

        return string + "!";
    }

    private String replacePlaceholders(Random random, String string, String[] values, String placeholder) {
        while (string.contains(placeholder)) {
            string = string.replaceFirst(placeholder, values[random.nextInt(values.length)]);
        }

        return string;
    }

}
