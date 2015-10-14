package com.thecodewarrior.hooks.net.server;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.HookRegistry;
import com.thecodewarrior.hooks.net.NetHandler;
import com.thecodewarrior.hooks.net.client.FireHookClientMessage;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.HookProperties;
import com.thecodewarrior.hooks.util.HookWrapper;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class FireHookMessage implements IMessage
{
    private String  hookId;
    private UUID    uuid;
    private Vector3 start;
    private Vector3 heading;
    
    public FireHookMessage() { }

    public FireHookMessage(String hookId, UUID id, Vector3 start, Vector3 heading) 
    {
        this.hookId  = hookId;
        this.uuid    = id;
        this.start   = start;
        this.heading = heading;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
    	hookId = ByteBufUtils.readUTF8String(buf);
    	uuid = NetHandler.readUUID(buf);
    	
    	start = NetHandler.readVector3(buf);
    	heading = NetHandler.readVector3(buf);
    	
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, hookId);
        NetHandler.writeUUID(buf, uuid);
        
        NetHandler.writeVector3(buf, start);
        NetHandler.writeVector3(buf, heading);

    }

    public static class Handler implements IMessageHandler<FireHookMessage, IMessage>
    {
        
        @Override
        public IMessage onMessage(FireHookMessage message, MessageContext ctx)
        {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            HookProperties props = HookWrapper.getProperties( player );
            
            ActiveHook ahook = new ActiveHook(HookRegistry.getHook(message.hookId));
            ahook.setUUID(message.uuid);
            ahook.setLocation(message.start);
    		ahook.setHeading(message.heading.normalize());
    		props.addHook(ahook);

            NetHandler.CHANNEL.sendToAllAround(new FireHookClientMessage(player.getEntityId(), message.hookId, message.uuid, message.start, message.heading),
            					new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 160));
            return null; // no response in this case
        }
    }
}
