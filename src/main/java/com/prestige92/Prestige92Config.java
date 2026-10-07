package com.prestige92;

import java.awt.Color;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("prestige92")
public interface Prestige92Config extends Config
{
    /*
     * LEVEL-UP SOUND
     *
     * Allows the player to completely enable
     * or disable the Prestige level-up sound.
     */
    @ConfigItem(
            keyName = "soundEnabled",
            name = "Level-up sound",
            description = "Play a sound when you reach a new Prestige level",
            position = 0
    )
    default boolean soundEnabled()
    {
        return true;
    }

    /*
     * SOUND VOLUME
     *
     * 0 = silent
     * 100 = full volume
     */
    @Range(
            min = 0,
            max = 100
    )
    @ConfigItem(
            keyName = "soundVolume",
            name = "Sound volume",
            description = "Volume of the Prestige level-up sound",
            position = 1
    )
    default int soundVolume()
    {
        return 70;
    }

    /*
     * LEVEL-UP FIREWORKS
     *
     * Controls the custom Prestige fireworks
     * shown when a Prestige level is reached.
     */
    @ConfigItem(
            keyName = "fireworksEnabled",
            name = "Level-up fireworks",
            description = "Show fireworks when you reach a new Prestige level",
            position = 2
    )
    default boolean fireworksEnabled()
    {
        return true;
    }

    /*
     * CHAT MESSAGE
     *
     * Controls the Prestige congratulations
     * message in the RuneScape chat box.
     */
    @ConfigItem(
            keyName = "chatMessageEnabled",
            name = "Chat message",
            description = "Show a chat message when you reach a new Prestige level",
            position = 3
    )
    default boolean chatMessageEnabled()
    {
        return true;
    }

    /*
     * PANEL HEADING
     *
     * TRUE:
     * Shows the Prestige 92 shield/logo.
     *
     * FALSE:
     * Shows the gold Prestige 92 text.
     */
    @ConfigItem(
            keyName = "useIconHeading",
            name = "Use logo heading",
            description = "Use the Prestige 92 logo instead of the text heading",
            position = 4
    )
    default boolean useIconHeading()
    {
        return true;
    }

    /*
     * DEFAULT PROGRESS BAR COLOUR
     *
     * Individual skills can still have their
     * own custom colours by right-clicking them.
     */
    @ConfigItem(
            keyName = "defaultProgressColour",
            name = "Default bar colour",
            description = "Default colour used for Prestige progress bars",
            position = 5
    )
    default Color defaultProgressColour()
    {
        return new Color(
                255,
                170,
                0
        );
    }
}