package org.spongepowered.common.mixin.core.item;

import net.minecraft.item.ItemPotion;
import net.minecraft.potion.PotionUtils;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.text.translation.SpongeTranslation;

@Mixin(ItemPotion.class)
public abstract class MixinItemPotion extends MixinItem {

    @Override
    public Translation getTranslation(ItemStack stack) {
        return new SpongeTranslation(PotionUtils.getPotionFromItem(ItemStackUtil.toNative(stack)).getNamePrefixed("potion.effect."));
    }
}
