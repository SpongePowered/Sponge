package org.spongepowered.common.mixin.core.item;

import net.minecraft.item.ItemTippedArrow;
import net.minecraft.potion.PotionUtils;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.text.translation.SpongeTranslation;

@Mixin(ItemTippedArrow.class)
public abstract class MixinItemTippedArrow extends MixinItem {

    @Override
    public Translation getTranslation(ItemStack stack) {
        return new SpongeTranslation(PotionUtils.getPotionFromItem(ItemStackUtil.toNative(stack)).getNamePrefixed("tipped_arrow.effect."));
    }
}
