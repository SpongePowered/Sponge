package org.spongepowered.common.bridge.server.management;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.profile.GameProfile;

public interface PlayerProfileCache_ProfileEntryBridge {

    void bridge$setIsFull(boolean full);

    void bridge$setSigned(boolean signed);

    void bridge$set(GameProfile profile, boolean full, boolean signed);

    GameProfile bridge$getBasic();

    @Nullable GameProfile bridge$getFull(boolean signed);
}
