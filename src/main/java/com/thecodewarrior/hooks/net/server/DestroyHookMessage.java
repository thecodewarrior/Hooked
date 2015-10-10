package com.thecodewarrior.hooks.net.server;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

import com.thecodewarrior.hooks.net.NetHandler;
import com.thecodewarrior.hooks.net.client.DestroyHookClientMessage;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.HookProperties;
import com.thecodewarrior.hooks.util.HookWrapper;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class DestroyHookMessage implements IMessage
{
	
	UUID hookId;
	
	public DestroyHookMessage()
	{
		
	}
	
	public DestroyHookMessage(UUID hookId)
	{
		this.hookId = hookId;
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		hookId = NetHandler.readUUID(buf);
	}
	
	@Override
	public void toBytes(ByteBuf buf)
	{
		NetHandler.writeUUID(buf, hookId);
	}
	
    public static class Handler implements IMessageHandler<DestroyHookMessage, IMessage>
    {

		@Override
		public IMessage onMessage(DestroyHookMessage message, MessageContext ctx)
		{
			EntityPlayer player = ctx.getServerHandler().playerEntity;
            HookProperties props = HookWrapper.getProperties( player );
            
            for(ActiveHook h : props.getHooks())
            {
            	if(h.getUUID().equals(message.hookId))
            		h.destroy();
            }
            
            NetHandler.CHANNEL.sendToAllAround(new DestroyHookClientMessage(player.getEntityId(), message.hookId),
            					new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 160));
			return null;
		}
    	
    }
	
}
