package org.spongepowered.common.entity.player;

import org.spongepowered.api.entity.living.Living;

public interface IUserAdapter extends Living {

    void setInvulnerable(boolean value);

    boolean getIsInvulnerable();

    boolean isVanished();

    void setVanished(boolean vanished);
}
