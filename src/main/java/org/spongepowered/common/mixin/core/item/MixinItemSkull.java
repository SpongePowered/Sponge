package org.spongepowered.common.mixin.core.item;

import net.minecraft.item.ItemSkull;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.text.translation.SpongeTranslation;

@Mixin(ItemSkull.class)
public abstract class MixinItemSkull extends MixinItem {

    @Override
    public Translation getTranslation(ItemStack stack) {
        return new SpongeTranslation(getTranslationKey(ItemStackUtil.toNative(stack)) + ".name");
    }
}
