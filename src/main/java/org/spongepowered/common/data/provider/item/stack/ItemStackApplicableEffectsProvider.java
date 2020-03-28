package org.spongepowered.common.data.provider.item.stack;

import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ItemStackApplicableEffectsProvider extends ItemStackDataProvider<Set<PotionEffect>> {

    public ItemStackApplicableEffectsProvider() {
        super(Keys.APPLICABLE_EFFECTS);
    }

    @Override
    protected Optional<Set<PotionEffect>> getFrom(ItemStack dataHolder) {
        final List<EffectInstance> effectsFromStack = PotionUtils.getEffectsFromStack(dataHolder);
        if (effectsFromStack.isEmpty()) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        ImmutableSet<PotionEffect> effects = ImmutableSet.copyOf((List) effectsFromStack);
        return Optional.of(effects);
    }
}
