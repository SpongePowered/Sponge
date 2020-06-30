package org.spongepowered.common.mixin.core.world;

import net.kyori.adventure.bossbar.BossBar;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.world.BossInfoBridge;

@Mixin(BossInfo.class)
public class BossInfoMixin implements BossInfoBridge {
    @Shadow protected ITextComponent name;

    @Shadow protected float percent;
    @Shadow protected BossInfo.Color color;
    @Shadow protected BossInfo.Overlay overlay;
    @Shadow protected boolean createFog;
    @Shadow protected boolean darkenSky;
    @Shadow protected boolean playEndBossMusic;
    protected BossBar impl$adventure;

    @Override
    public void bridge$copyAndAssign(final BossBar adventure) {
        this.impl$adventure = adventure;
        this.percent = adventure.percent();
        this.darkenSky = adventure.flags().contains(BossBar.Flag.DARKEN_SCREEN);
        this.playEndBossMusic = adventure.flags().contains(BossBar.Flag.PLAY_BOSS_MUSIC);
        this.createFog = adventure.flags().contains(BossBar.Flag.CREATE_WORLD_FOG);
    }

    @Override
    public BossBar bridge$asAdventure() {
        if (this.impl$adventure == null) {
            this.bridge$setAdventure(BossBar.of(SpongeAdventure.asAdventure(this.name),
                this.percent,
                SpongeAdventure.asAdventure(this.color),
                SpongeAdventure.asAdventure(this.overlay),
                SpongeAdventure.asAdventureFlags(this.darkenSky, this.playEndBossMusic, this.createFog)));
        }
        return this.impl$adventure;
    }

    @Override
    public void bridge$setAdventure(BossBar adventure) {
        this.impl$adventure = adventure;
    }

    // Redirect setters
    @Redirect(method = "setName", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossInfo;name:Lnet/minecraft/util/text/ITextComponent;"))
    private void adventureName(final BossInfo $this, final ITextComponent name) {
        this.bridge$asAdventure().name(SpongeAdventure.asAdventure(name));
    }

    @Redirect(method = "setPercent", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossInfo;percent:F"))
    private void adventurePercent(final BossInfo $this, final float percent) {
        this.bridge$asAdventure().percent(percent);
    }

    @Redirect(method = "setColor", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossInfo;color:Lnet/minecraft/world/BossInfo$Color;"))
    private void adventureColor(final BossInfo $this, final BossInfo.Color color) {
        this.bridge$asAdventure().color(SpongeAdventure.asAdventure(color));
    }

    @Redirect(method = "setOverlay", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossInfo;overlay:Lnet/minecraft/world/BossInfo$Overlay;"))
    private void adventureOverlay(final BossInfo $this, final BossInfo.Overlay overlay) {
        this.bridge$asAdventure().overlay(SpongeAdventure.asAdventure(overlay));
    }

    @Redirect(method = "setDarkenSky", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossInfo;darkenSky:Z"))
    private void adventureDarkenSky(final BossInfo $this, final boolean playEndBossMusic) {
        if (playEndBossMusic) {
            this.bridge$asAdventure().addFlags(BossBar.Flag.DARKEN_SCREEN);
        } else {
            this.bridge$asAdventure().removeFlags(BossBar.Flag.DARKEN_SCREEN);
        }
    }

    @Redirect(method = "setPlayEndBossMusic", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossInfo;playEndBossMusic:Z"))
    private void adventurePlayEndBossMusic(final BossInfo $this, final boolean playEndBossMusic) {
        if (playEndBossMusic) {
            this.bridge$asAdventure().addFlags(BossBar.Flag.PLAY_BOSS_MUSIC);
        } else {
            this.bridge$asAdventure().removeFlags(BossBar.Flag.PLAY_BOSS_MUSIC);
        }
    }

    @Redirect(method = "setCreateFog", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossInfo;createFog:Z"))
    private void adventureCreateFog(final BossInfo $this, final boolean createFog) {
        if (createFog) {
            this.bridge$asAdventure().addFlags(BossBar.Flag.CREATE_WORLD_FOG);
        } else {
            this.bridge$asAdventure().removeFlags(BossBar.Flag.CREATE_WORLD_FOG);
        }
    }

    // Redirect getters

    @Redirect(method = "getName", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossInfo;name:Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent nameRead(final BossInfo $this) {
        return SpongeAdventure.asVanilla(this.bridge$asAdventure().name());
    }

    @Redirect(method = "getPercent", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossInfo;percent:F"))
    private float percentRead(final BossInfo $this) {
        return this.bridge$asAdventure().percent();
    }

    @Redirect(method = "getColor", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossInfo;color:Lnet/minecraft/world/BossInfo$Color;"))
    private BossInfo.Color colorRead(final BossInfo $this) {
        return SpongeAdventure.asVanilla(this.bridge$asAdventure().color());
    }

    @Redirect(method = "getOverlay", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossInfo;overlay:Lnet/minecraft/world/BossInfo$Overlay;"))
    private BossInfo.Overlay overlayRead(final BossInfo $this) {
        return SpongeAdventure.asVanilla(this.bridge$asAdventure().overlay());
    }

    @Redirect(method = "shouldDarkenSky", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossInfo;darkenSky:Z"))
    private boolean darkenSkyRead(final BossInfo $this) {
        return this.bridge$asAdventure().flags().contains(BossBar.Flag.DARKEN_SCREEN);
    }

    @Redirect(method = "shouldPlayEndBossMusic", at =@At(value = "FIELD", target = "Lnet/minecraft/world/BossInfo;playEndBossMusic:Z"))
    private boolean playEndBossMusicRead(final BossInfo $this) {
        return this.bridge$asAdventure().flags().contains(BossBar.Flag.PLAY_BOSS_MUSIC);
    }

    @Redirect(method = "shouldCreateFog", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossInfo;createFog:Z"))
    private boolean createFogRead(final BossInfo $this) {
        return this.bridge$asAdventure().flags().contains(BossBar.Flag.CREATE_WORLD_FOG);
    }
}
