package su.nexmedia.engine.config;

import su.nexmedia.engine.api.config.JOption;

public class EngineConfig {

    public static final JOption<Boolean> RESPECT_PLAYER_DISPLAYNAME = JOption.create("Engine.Respect_Player_DisplayName",
        true,
        "Sets whether or not 'Player#getDisplayName' can be used to find & get players in addition to regular 'Player#getName'.");
}
