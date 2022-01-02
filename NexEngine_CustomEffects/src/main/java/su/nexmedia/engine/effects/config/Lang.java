package su.nexmedia.engine.effects.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.effects.NexCustomEffects;
import su.nexmedia.engine.api.config.LangMessage;
import su.nexmedia.engine.core.config.CoreLang;

public class Lang extends CoreLang {

    public Lang(@NotNull NexCustomEffects plugin) {
        super(plugin);
    }

    public LangMessage Effects_Root_Apply      = new LangMessage(this,
        "{message: ~prefix: false; ~type: TITLES; ~fadeIn: 10; ~stay: 30; ~fadeOut: 10;}"
            + "&c&lRooted!\n"
            + "&7Movement is restricted."
    );
    public LangMessage Effects_Blindness_Apply = new LangMessage(this,
        "{message: ~prefix: false; ~type: TITLES; ~fadeIn: 10; ~stay: 30; ~fadeOut: 10;}"
            + "&c&lBlinded!\n"
            + "&7Your attacks are useless."
    );
    public LangMessage Effects_Disarm_Apply    = new LangMessage(this,
        "{message: ~prefix: false; ~type: TITLES; ~fadeIn: 10; ~stay: 30; ~fadeOut: 10;}"
            + "&c&lDisarmed!\n"
            + "&7Your weapon is on ground!"
    );
    public LangMessage Effects_Stun_Apply      = new LangMessage(this,
        "{message: ~prefix: false; ~type: TITLES; ~fadeIn: 10; ~stay: 30; ~fadeOut: 10;}"
            + "&c&lStunned!\n"
            + "&7You can not move and attack!");
}
