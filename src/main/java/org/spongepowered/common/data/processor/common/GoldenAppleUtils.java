package org.spongepowered.common.data.processor.common;

import com.google.common.collect.Iterables;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.type.GoldenApple;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.item.SpongeGoldenApple;

public class GoldenAppleUtils {

    public static void setType(ItemStack itemStack, GoldenApple value) {
        itemStack.setItemDamage(((SpongeGoldenApple) value).type);
    }

    public static GoldenApple getType(ItemStack itemStack) {
        return Iterables.get(Sponge.getSpongeRegistry().goldenAppleMappings.values(), itemStack.getMetadata());
    }

}
