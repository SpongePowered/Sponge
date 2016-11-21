/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.core.tileentity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.item.inventory.type.TileEntityInventory;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.projectile.ProjectileLauncher;
import org.spongepowered.common.interfaces.data.IMixinCustomNameable;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.fabric.DefaultInventoryFabric;

import java.util.Optional;

@NonnullByDefault
@Mixin(TileEntityDispenser.class)
@Implements({@Interface(iface = MinecraftInventoryAdapter.class, prefix = "inventory$"),
        @Interface(iface = TileEntityInventory.class, prefix = "tileentityinventory$")})
public abstract class MixinTileEntityDispenser extends MixinTileEntityLockable implements Dispenser, IMixinCustomNameable {

    private Fabric<IInventory> fabric;
    private SlotCollection slots;
    private Lens<IInventory, ItemStack> lens;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        this.fabric = new DefaultInventoryFabric(this);
        this.slots = new SlotCollection.Builder()
                .add(9)
                .build();
        this.lens = new GridInventoryLensImpl(0, 3, 3, 3, this.slots);
    }

    @Override
    public <T extends Projectile> Optional<T> launchProjectile(Class<T> projectileClass) {
        return ProjectileLauncher.launch(checkNotNull(projectileClass, "projectileClass"), this, null);
    }

    @Override
    public <T extends Projectile> Optional<T> launchProjectile(Class<T> projectileClass, Vector3d velocity) {
        return ProjectileLauncher.launch(checkNotNull(projectileClass, "projectileClass"), this, checkNotNull(velocity, "velocity"));
    }

    @Override
    public void setCustomDisplayName(String customName) {
        ((TileEntityDispenser) (Object) this).setCustomName(customName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TileEntityInventory<TileEntityCarrier> getInventory() {
        return (TileEntityInventory<TileEntityCarrier>) this;
    }

    @Intrinsic
    public void tilentityinventory$markDirty() {
        this.markDirty();
    }

    public SlotProvider<IInventory, ItemStack> inventory$getSlotProvider() {
        return this.slots;
    }

    public Lens<IInventory, ItemStack> inventory$getRootLens() {
        return this.lens;
    }

    public Fabric<IInventory> inventory$getInventory() {
        return this.fabric;
    }

    public Optional<Dispenser> tileentityinventory$getTileEntity() {
        return Optional.of(this);
    }

    public Optional<Dispenser> tileentityinventory$getCarrier() {
        return Optional.of(this);
    }
}
