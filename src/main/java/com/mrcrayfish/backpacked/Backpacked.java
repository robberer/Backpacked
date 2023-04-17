package com.mrcrayfish.backpacked;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.backpacked.client.ClientHandler;
import com.mrcrayfish.backpacked.common.UnlockTracker;
import com.mrcrayfish.backpacked.core.ModCommands;
import com.mrcrayfish.backpacked.core.ModContainers;
import com.mrcrayfish.backpacked.core.ModItems;
import com.mrcrayfish.backpacked.integration.Curios;
import com.mrcrayfish.backpacked.inventory.ExtendedPlayerInventory;
import com.mrcrayfish.backpacked.item.BackpackItem;
import com.mrcrayfish.backpacked.network.Network;
import com.mrcrayfish.backpacked.network.message.MessageUpdateBackpack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
@Mod(Reference.MOD_ID)
public class Backpacked
{
    public static final ResourceLocation EMPTY_BACKPACK_SLOT = new ResourceLocation(Reference.MOD_ID, "item/empty_backpack_slot");

    private static boolean controllableLoaded = false;
    private static boolean curiosLoaded = false;
    private static List<ResourceLocation> bannedItemsList;

    public Backpacked()
    {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ModCommands());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onTextureStitch));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onEnqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReload);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModContainers.REGISTER.register(bus);
        ModItems.REGISTER.register(bus);
        controllableLoaded = ModList.get().isLoaded("controllable");
        curiosLoaded = ModList.get().isLoaded("curios");
    }

    private void onCommonSetup(FMLCommonSetupEvent event)
    {
        UnlockTracker.registerCapability();
        Network.init();
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        ClientHandler.setup();
    }

    private void onEnqueueIMC(InterModEnqueueEvent event)
    {
        if(!curiosLoaded)
            return;

        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BACK.getMessageBuilder().build());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onPlayerRenderScreen(GuiContainerEvent.DrawBackground event)
    {
        if(curiosLoaded)
            return;

        ContainerScreen<?> screen = event.getGuiContainer();
        if(screen instanceof InventoryScreen)
        {
            InventoryScreen inventoryScreen = (InventoryScreen) screen;
            int left = inventoryScreen.getGuiLeft();
            int top = inventoryScreen.getGuiTop();
            inventoryScreen.getMinecraft().getTextureManager().bind(ContainerScreen.INVENTORY_LOCATION);
            Screen.blit(event.getMatrixStack(), left + 76, top + 43, 7, 7, 18, 18, 256, 256);
        }
        else if(screen instanceof CreativeScreen)
        {
            CreativeScreen creativeScreen = (CreativeScreen) screen;
            if(creativeScreen.getSelectedTab() == ItemGroup.TAB_INVENTORY.getId())
            {
                int left = creativeScreen.getGuiLeft();
                int top = creativeScreen.getGuiTop();
                creativeScreen.getMinecraft().getTextureManager().bind(ContainerScreen.INVENTORY_LOCATION);
                Screen.blit(event.getMatrixStack(), left + 126, top + 19, 7, 7, 18, 18, 256, 256);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void onTextureStitch(TextureStitchEvent.Pre event)
    {
        if(event.getMap().location().equals(PlayerContainer.BLOCK_ATLAS))
        {
            event.addSprite(EMPTY_BACKPACK_SLOT);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event)
    {
        if(curiosLoaded)
            return;

        PlayerEntity oldPlayer = event.getOriginal();
        if(oldPlayer.inventory instanceof ExtendedPlayerInventory && event.getPlayer().inventory instanceof ExtendedPlayerInventory)
        {
            ((ExtendedPlayerInventory) event.getPlayer().inventory).copyBackpack((ExtendedPlayerInventory) oldPlayer.inventory);
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event)
    {
        if(curiosLoaded)
            return;

        PlayerEntity player = event.getPlayer();
        if(player.inventory instanceof ExtendedPlayerInventory)
        {
            ItemStack backpack = ((ExtendedPlayerInventory) player.inventory).getBackpackItems().get(0);
            if(!backpack.isEmpty() && backpack.getItem() instanceof BackpackItem)
            {
                Network.getPlayChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new MessageUpdateBackpack(player.getId(), backpack));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(curiosLoaded)
            return;

        if(event.phase != TickEvent.Phase.START)
            return;

        PlayerEntity player = event.player;
        if(!player.level.isClientSide && player.inventory instanceof ExtendedPlayerInventory)
        {
            ExtendedPlayerInventory inventory = (ExtendedPlayerInventory) player.inventory;
            if(!inventory.backpackArray.get(0).equals(inventory.backpackInventory.get(0)))
            {
                Network.getPlayChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new MessageUpdateBackpack(player.getId(), inventory.backpackInventory.get(0)));
                inventory.backpackArray.set(0, inventory.backpackInventory.get(0));
            }
        }
    }

    public static boolean isControllableLoaded()
    {
        return controllableLoaded;
    }

    public static boolean isCuriosLoaded()
    {
        return curiosLoaded;
    }

    public static ItemStack getBackpackStack(PlayerEntity player)
    {
        AtomicReference<ItemStack> backpack = new AtomicReference<>(ItemStack.EMPTY);
        if(Backpacked.isCuriosLoaded())
        {
            backpack.set(Curios.getBackpackStack(player));
        }
        if(player.inventory instanceof ExtendedPlayerInventory)
        {
            ExtendedPlayerInventory inventory = (ExtendedPlayerInventory) player.inventory;
            ItemStack stack = inventory.getBackpackItems().get(0);
            if(stack.getItem() instanceof BackpackItem)
            {
                backpack.set(stack);
            }
        }
        return backpack.get();
    }

    public static boolean setBackpackStack(PlayerEntity player, ItemStack stack)
    {
        if(!(stack.getItem() instanceof BackpackItem) && !stack.isEmpty())
            return false;

        if(Backpacked.isCuriosLoaded())
        {
            Curios.setBackpackStack(player, stack);
            return true;
        }
        else if(player.inventory instanceof ExtendedPlayerInventory)
        {
            ((ExtendedPlayerInventory) player.inventory).getBackpackItems().set(0, stack.copy());
            return true;
        }
        return false;
    }

    private void onConfigLoad(ModConfig.Loading event)
    {
        ModConfig config = event.getConfig();
        if(config.getType() == ModConfig.Type.SERVER && config.getModId().equals(Reference.MOD_ID))
        {
            this.updateBannedItemsList();
        }
    }

    private void onConfigReload(ModConfig.Reloading event)
    {
        ModConfig config = event.getConfig();
        if(config.getType() == ModConfig.Type.SERVER && config.getModId().equals(Reference.MOD_ID))
        {
            this.updateBannedItemsList();
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onPlayerLogin(ClientPlayerNetworkEvent.LoggedInEvent event)
    {
        this.updateBannedItemsList();
    }

    private void updateBannedItemsList()
    {
        bannedItemsList = ImmutableList.copyOf(Config.SERVER.bannedItems.get().stream().map(ResourceLocation::new).collect(Collectors.toList()));
    }

    public static List<ResourceLocation> getBannedItemsList()
    {
        return bannedItemsList;
    }

}
