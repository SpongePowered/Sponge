package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

public class ItemStackGameProfileProvider extends ItemStackDataProvider<GameProfile> {

    public ItemStackGameProfileProvider() {
        super(Keys.GAME_PROFILE);
    }

    @Override
    protected Optional<GameProfile> getFrom(ItemStack dataHolder) {
        @Nullable CompoundNBT nbt = dataHolder.getChildTag(Constants.Item.Skull.ITEM_SKULL_OWNER);
        return Optional.ofNullable(nbt == null ? null : (GameProfile) NBTUtil.readGameProfile(nbt));
    }

    @Override
    protected boolean set(ItemStack dataHolder, @Nullable GameProfile value) {
        if (value == null) {
            dataHolder.getTag().remove(Constants.Item.Skull.ITEM_SKULL_OWNER);
        } else {
            final CompoundNBT nbt = NBTUtil.writeGameProfile(new CompoundNBT(), (com.mojang.authlib.GameProfile) resolveProfileIfNecessary(value));
            dataHolder.setTagInfo(Constants.Item.Skull.ITEM_SKULL_OWNER, nbt);
        }
        return true;
    }

    @Override
    protected boolean supports(Item item) {
        return item instanceof SkullItem;
    }

    public static @Nullable GameProfile resolveProfileIfNecessary(@Nullable final GameProfile profile) {
        if (profile == null) {
            return null;
        }
        if (profile.getPropertyMap().containsKey("textures")) {
            return profile;
        }
        // Skulls need a name in order to properly display -> resolve if no name is contained in the given profile
        final CompletableFuture<GameProfile> future = Sponge.getGame().getServer().getGameProfileManager().fill(profile);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            SpongeImpl.getLogger().debug("Exception while trying to fill skull GameProfile for '" + profile + "'", e);
            return profile;
        }
    }
}
