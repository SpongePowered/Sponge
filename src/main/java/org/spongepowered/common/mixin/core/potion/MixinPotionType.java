package org.spongepowered.common.mixin.core.potion;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.item.potion.PotionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(net.minecraft.potion.PotionType.class)
public abstract class MixinPotionType implements PotionType {

    @Shadow @Final private ImmutableList<net.minecraft.potion.PotionEffect> effects;

    @Override
    @SuppressWarnings("unchecked")
    public List<PotionEffect> getEffects() {
        return ((List) this.effects); // PotionEffect is mixed into
    }
}
