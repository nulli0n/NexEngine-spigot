package su.nexmedia.engine.hooks.external.citizens;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public interface CitizensListener {

    void onLeftClick(NPCLeftClickEvent e);

    void onRightClick(NPCRightClickEvent e);
}
