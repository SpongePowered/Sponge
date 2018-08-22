package org.spongepowered.common.mixin.core.item;

import net.minecraft.item.ItemColored;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.text.translation.SpongeTranslation;

@Mixin(ItemColored.class)
public abstract class MixinItemColored extends MixinItem {

    @Override
    public Translation getTranslation(ItemStack stack) {
        net.minecraft.item.ItemStack itemStack = ItemStackUtil.toNative(stack);
        return new SpongeTranslation(getTranslationKey(itemStack) + ".name");
    }
}
