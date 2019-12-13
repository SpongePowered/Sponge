package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public class LivingEntityPotionEffectsProvider extends GenericMutableDataProvider<LivingEntity, List<PotionEffect>> {

    public LivingEntityPotionEffectsProvider() {
        super(Keys.POTION_EFFECTS);
    }

    @Override
    protected Optional<List<PotionEffect>> getFrom(LivingEntity dataHolder) {
        final Collection<EffectInstance> effects = dataHolder.getActivePotionEffects();
        if (effects.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(effects.stream()
                .map(effect -> (PotionEffect) new EffectInstance(effect.getPotion(), effect.getDuration(),
                        effect.getAmplifier(), effect.isAmbient(), effect.doesShowParticles()))
                .collect(Collectors.toList()));
    }

    @Override
    protected boolean set(LivingEntity dataHolder, List<PotionEffect> value) {
        dataHolder.clearActivePotions();
        for (PotionEffect effect : value) {
            final EffectInstance mcEffect = new EffectInstance(((EffectInstance) effect).getPotion(), effect.getDuration(),
                    effect.getAmplifier(), effect.isAmbient(), effect.getShowParticles());
            dataHolder.addPotionEffect(mcEffect);
        }
        return true;
    }

    @Override
    protected boolean removeFrom(LivingEntity dataHolder) {
        dataHolder.clearActivePotions();
        return true;
    }
}
