package com.mrcrayfish.backpacked.network.message;

import com.mrcrayfish.backpacked.network.play.ServerPlayHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessagePlayerBackpack implements IMessage<MessagePlayerBackpack>
{
    private int entityId;

    public MessagePlayerBackpack() {}

    public MessagePlayerBackpack(int entityId)
    {
        this.entityId = entityId;
    }

    @Override
    public void encode(MessagePlayerBackpack message, PacketBuffer buffer)
    {
        buffer.writeInt(message.entityId);
    }

    @Override
    public MessagePlayerBackpack decode(PacketBuffer buffer)
    {
        return new MessagePlayerBackpack(buffer.readInt());
    }

    @Override
    public void handle(MessagePlayerBackpack message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() -> ServerPlayHandler.handlePlayerBackpack(message, supplier.get().getSender()));
        supplier.get().setPacketHandled(true);
    }

    public int getEntityId()
    {
        return this.entityId;
    }
}
