package org.spongepowered.common.mixin.core.server.management;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.server.management.PlayerProfileCache_ProfileEntryBridge;
import org.spongepowered.common.profile.SpongeGameProfile;

@Mixin(targets = "net.minecraft.server.management.PlayerProfileCache$ProfileEntry")
public abstract class PlayerProfileCache_ProfileEntryMixin implements PlayerProfileCache_ProfileEntryBridge {

    @Shadow @Final private com.mojang.authlib.GameProfile gameProfile;

    private boolean impl$isFull;
    private boolean impl$isSigned;

    private volatile @Nullable GameProfile impl$basic;
    private volatile @Nullable GameProfile impl$fullSigned;
    private volatile @Nullable GameProfile impl$fullUnsigned;

    @Override
    public void bridge$setIsFull(final boolean full) {
        this.impl$isFull = full;
    }

    @Override
    public void bridge$setSigned(final boolean signed) {
        this.impl$isSigned = signed;
    }

    @Override
    public void bridge$set(final GameProfile profile, final boolean full, final boolean signed) {
        if (full) {
            if (signed) {
                this.impl$fullSigned = profile;
            } else {
                this.impl$fullUnsigned = profile;
            }
        } else {
            this.impl$basic = profile;
        }
        this.impl$isSigned = signed;
        this.impl$isFull = full;
    }

    @Override
    public GameProfile bridge$getBasic() {
        GameProfile basic = this.impl$basic;
        if (basic != null) {
            return basic;
        }
        basic = this.impl$basic = SpongeGameProfile.basicOf(this.gameProfile);
        return basic;
    }

    @Override
    public @Nullable GameProfile bridge$getFull(final boolean signed) {
        if (!this.impl$isFull) {
            return null;
        }
        GameProfile full = signed ? this.impl$fullSigned : this.impl$fullUnsigned;
        if (full != null) {
            return full;
        }
        if (signed) {
            if (!this.impl$isSigned) {
                return null;
            }
            full = this.impl$fullSigned = SpongeGameProfile.of(this.gameProfile);
        } else {
            final GameProfile fullSigned = this.impl$fullSigned;
            full = this.impl$fullUnsigned = SpongeGameProfile.unsignedOf(
                    fullSigned == null ? SpongeGameProfile.of(this.gameProfile) : fullSigned);
        }
        return full;
    }
}
