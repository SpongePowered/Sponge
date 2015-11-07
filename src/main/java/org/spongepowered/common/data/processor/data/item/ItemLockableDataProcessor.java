package org.spongepowered.common.data.processor.data.item;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.world.LockCode;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableLockableData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.LockableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeLockableData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

public final class ItemLockableDataProcessor extends AbstractItemSingleDataProcessor<String, Value<String>, LockableData, ImmutableLockableData> {

    public ItemLockableDataProcessor() {
        super(stack -> {
            Item item = stack.getItem();
            if (!(item instanceof ItemBlock)) {
                return false;
            }
            Block block = ((ItemBlock) item).getBlock();
            if (!(block instanceof ITileEntityProvider)) {
                return false;
            }
            TileEntity tile = ((ITileEntityProvider) block).createNewTileEntity(null, item.getMetadata(stack.getItemDamage()));
            return tile instanceof TileEntityLockable;
        }, Keys.LOCK_TOKEN);
    }

    @Override
    public DataTransactionResult remove(DataHolder holder) {
        if (supports(holder)) {
            set((ItemStack) holder, "");
            return DataTransactionBuilder.successNoData();
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected boolean set(ItemStack stack, String value) {
        NBTTagCompound mainCompound = NbtDataUtil.getOrCreateCompound(stack);
        NBTTagCompound tileCompound = NbtDataUtil.getOrCreateSubCompound(mainCompound, NbtDataUtil.BLOCK_ENTITY_TAG);
        LockCode code = new LockCode(value);
        if (code.isEmpty()) {
            tileCompound.removeTag("Lock");
        } else {
            code.toNBT(tileCompound);
        }
        return true;
    }

    @Override
    protected Optional<String> getVal(ItemStack container) {
        if (container.getTagCompound() == null) {
            return Optional.of("");
        }
        NBTTagCompound tileCompound = container.getTagCompound().getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG);
        LockCode code = LockCode.fromNBT(tileCompound);
        if (code.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(code.getLock());
    }

    @Override
    protected ImmutableValue<String> constructImmutableValue(String value) {
        return new ImmutableSpongeValue<String>(Keys.LOCK_TOKEN, "", value);
    }

    @Override
    protected LockableData createManipulator() {
        return new SpongeLockableData();
    }

}
