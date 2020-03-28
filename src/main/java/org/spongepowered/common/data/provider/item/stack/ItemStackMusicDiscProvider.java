package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;

import java.util.Optional;

public class ItemStackMusicDiscProvider extends ItemStackDataProvider<MusicDisc> {

    public ItemStackMusicDiscProvider() {
        super(Keys.MUSIC_DISK);
    }

    @Override
    protected Optional<MusicDisc> getFrom(ItemStack dataHolder) {
        if (dataHolder.getItem() instanceof MusicDisc) {
            return Optional.of((MusicDisc) dataHolder.getItem());
        }
        return Optional.empty();
    }
}
