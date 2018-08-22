package org.spongepowered.common.mixin.core.item;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBanner;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.text.translation.SpongeTranslation;

import static net.minecraft.item.ItemBanner.getBaseColor;

@Mixin(ItemBanner.class)
public abstract class MixinItemBanner extends MixinItem {

    @Override
    public Translation getTranslation(ItemStack stack) {
        String s = "item.banner.";
        EnumDyeColor enumdyecolor = getBaseColor(ItemStackUtil.toNative(stack));
        s = s + enumdyecolor.getTranslationKey() + ".name";
        return new SpongeTranslation(s);
    }
}
