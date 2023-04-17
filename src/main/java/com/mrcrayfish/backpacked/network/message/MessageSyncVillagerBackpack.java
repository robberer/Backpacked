package com.mrcrayfish.backpacked.network.message;

import com.mrcrayfish.backpacked.network.play.ClientPlayHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageSyncVillagerBackpack implements IMessage<MessageSyncVillagerBackpack>
{
    private int entityId;

    public MessageSyncVillagerBackpack() {}

    public MessageSyncVillagerBackpack(int entityId)
    {
        this.entityId = entityId;
    }

    @Override
    public void encode(MessageSyncVillagerBackpack message, PacketBuffer buffer)
    {
        buffer.writeInt(message.entityId);
    }

    @Override
    public MessageSyncVillagerBackpack decode(PacketBuffer buffer)
    {
        return new MessageSyncVillagerBackpack(buffer.readInt());
    }

    @Override
    public void handle(MessageSyncVillagerBackpack message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() -> ClientPlayHandler.handleSyncVillagerBackpack(message));
        supplier.get().setPacketHandled(true);
    }

    public int getEntityId()
    {
        return this.entityId;
    }
}
