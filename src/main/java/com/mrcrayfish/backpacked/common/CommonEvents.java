package com.mrcrayfish.backpacked.common;

import com.mrcrayfish.backpacked.Backpacked;
import com.mrcrayfish.backpacked.Config;
import com.mrcrayfish.backpacked.Reference;
import com.mrcrayfish.backpacked.common.tracker.BiomeExploreProgressTracker;
import com.mrcrayfish.backpacked.common.tracker.CraftingProgressTracker;
import com.mrcrayfish.backpacked.item.BackpackItem;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SCollectItemPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class CommonEvents
{
    @SubscribeEvent
    public static void onPickupItem(EntityItemPickupEvent event)
    {
        if(Config.SERVER.autoEquipBackpackOnPickup.get() && event.getEntityLiving() instanceof ServerPlayerEntity)
        {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
            ItemEntity entity = event.getItem();
            ItemStack stack = entity.getItem();
            if(!(stack.getItem() instanceof BackpackItem))
                return;

            if(Backpacked.getBackpackStack(player).isEmpty())
            {
                event.setCanceled(true);
                if(Backpacked.setBackpackStack(player, stack))
                {
                    ((ServerWorld) entity.level).getChunkSource().broadcast(entity, new SCollectItemPacket(entity.getId(), player.getId(), stack.getCount()));
                    event.setCanceled(true);
                    event.getItem().kill();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCraftedItem(PlayerEvent.ItemCraftedEvent event)
    {
        if(!(event.getPlayer() instanceof ServerPlayerEntity))
            return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        ItemStack craftedItem = event.getCrafting();
        UnlockTracker.get(event.getPlayer()).ifPresent(unlockTracker ->
        {
            unlockTracker.getProgressTrackerMap().forEach((location, progressTracker) ->
            {
                if(progressTracker instanceof CraftingProgressTracker && !progressTracker.isComplete())
                {
                    ((CraftingProgressTracker) progressTracker).processCrafted(craftedItem, player);
                }
            });
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        if(event.player.level.isClientSide())
            return;

        if(event.player.tickCount % 20 != 0)
            return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.player;
        ServerWorld world = player.getLevel();
        BlockPos playerPosition = player.blockPosition();
        world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getResourceKey(world.getBiome(playerPosition)).ifPresent(key ->
        {
            UnlockTracker.get(player).ifPresent(unlockTracker ->
            {
                unlockTracker.getProgressTrackerMap().forEach((location, progressTracker) ->
                {
                    if(progressTracker instanceof BiomeExploreProgressTracker && !progressTracker.isComplete())
                    {
                        ((BiomeExploreProgressTracker) progressTracker).explore(key, player);
                    }
                });
            });
        });
    }
}
