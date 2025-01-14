package com.mrcrayfish.backpacked.item;

import com.mrcrayfish.backpacked.Backpacked;
import com.mrcrayfish.backpacked.Config;
import com.mrcrayfish.backpacked.client.ModelInstances;
import com.mrcrayfish.backpacked.client.model.BackpackModel;
import com.mrcrayfish.backpacked.integration.Curios;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import com.mrcrayfish.backpacked.inventory.BackpackedInventoryAccess;
import com.mrcrayfish.backpacked.inventory.ExtendedPlayerInventory;
import com.mrcrayfish.backpacked.inventory.container.BackpackContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class BackpackItem extends Item
{
    public static final TranslationTextComponent BACKPACK_TRANSLATION = new TranslationTextComponent("container.backpack");

    public BackpackItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        ItemStack heldItem = playerIn.getItemInHand(handIn);
        if(playerIn.inventory instanceof ExtendedPlayerInventory)
        {
            ExtendedPlayerInventory inventory = (ExtendedPlayerInventory) playerIn.inventory;
            if(inventory.getBackpackItems().get(0).isEmpty())
            {
                playerIn.inventory.setItem(41, heldItem.copy());
                heldItem.setCount(0);
                playerIn.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
                return new ActionResult<>(ActionResultType.SUCCESS, heldItem);
            }
        }
        return new ActionResult<>(ActionResultType.FAIL, heldItem);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt)
    {
        if(!Backpacked.isCuriosLoaded())
        {
            return null;
        }
        return Curios.createBackpackProvider(stack);
    }

    public void showInventory(ServerPlayerEntity player)
    {
        ItemStack backpack = Backpacked.getBackpackStack(player);
        if(!backpack.isEmpty())
        {
            BackpackInventory backpackInventory = ((BackpackedInventoryAccess) player).getBackpackedInventory();
            if(backpackInventory == null)
                return;
            ITextComponent title = backpack.hasCustomHoverName() ? backpack.getHoverName() : BACKPACK_TRANSLATION;
            int rows = this.getRowCount();
            NetworkHooks.openGui(player, new SimpleNamedContainerProvider((id, playerInventory, entity) -> {
                return new BackpackContainer(id, player.inventory, backpackInventory, rows);
            }, title), buffer -> buffer.writeVarInt(rows));
        }
    }

    public int getRowCount()
    {
        return Config.COMMON.backpackInventorySize.get();
    }

    @OnlyIn(Dist.CLIENT)
    public BackpackModel getDefaultModel()
    {
        return ModelInstances.STANDARD;
    }
}
