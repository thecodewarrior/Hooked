package com.thecodewarrior.hooks.net.server;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.net.NetHandler;
import com.thecodewarrior.hooks.net.client.RetractHookClientMessage;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.HookProperties;
import com.thecodewarrior.hooks.util.HookWrapper;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class RetractHookMessage implements IMessage
{
	
	UUID hookId;
	Vector3 location;
	
	public RetractHookMessage()
	{
		
	}
	
	public RetractHookMessage(UUID hookId, Vector3 location)
	{
		this.hookId = hookId;
		this.location = location;
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		hookId = NetHandler.readUUID(buf);
		location = NetHandler.readVector3(buf);
	}
	
	@Override
	public void toBytes(ByteBuf buf)
	{
		NetHandler.writeUUID(buf, hookId);
		NetHandler.writeVector3(buf, location);
	}
	
    public static class Handler implements IMessageHandler<RetractHookMessage, IMessage>
    {

		@Override
		public IMessage onMessage(RetractHookMessage message, MessageContext ctx)
		{
			EntityPlayer player = ctx.getServerHandler().playerEntity;
            HookProperties props = HookWrapper.getProperties( player );
            
            for(ActiveHook h : props.getHooks())
            {
            	if(h.getUUID().equals( message.hookId ))
            	{
            		h.destroy();
            		h.setLocation(message.location);
            	}
            }

            NetHandler.CHANNEL.sendToAllAround(new RetractHookClientMessage(player.getEntityId(), message.hookId, message.location),
            					new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 160));
			return null;
		}
    	
    }
	
}
