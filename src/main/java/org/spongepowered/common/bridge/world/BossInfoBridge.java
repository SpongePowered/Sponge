package org.spongepowered.common.bridge.world;

import net.kyori.adventure.bossbar.BossBar;

public interface BossInfoBridge {
    /**
     * Update this bar's info from its adventure equivalent.
     *
     * <p>The adventure bits</p>
     *
     * @param adventure adventure boss bar
     */
    void bridge$copyAndAssign(BossBar adventure);

    BossBar bridge$asAdventure();

    void bridge$setAdventure(BossBar adventure);
}
