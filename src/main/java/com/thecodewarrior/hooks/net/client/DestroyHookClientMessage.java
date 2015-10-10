package com.thecodewarrior.hooks.net.client;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import com.thecodewarrior.hooks.net.NetHandler;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.HookProperties;
import com.thecodewarrior.hooks.util.HookWrapper;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class DestroyHookClientMessage implements IMessage
{
	
	UUID hookId;
	
	int eID;

	public DestroyHookClientMessage()
	{
		
	}
	
	public DestroyHookClientMessage(int entityId, UUID hookId)
	{
		eID = entityId;
		this.hookId = hookId;
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		eID = buf.readInt();
		hookId = NetHandler.readUUID(buf);
	}
	
	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(eID);
		NetHandler.writeUUID(buf, hookId);
	}
	
    public static class Handler implements IMessageHandler<DestroyHookClientMessage, IMessage>
    {

		@Override
		public IMessage onMessage(DestroyHookClientMessage message, MessageContext ctx)
		{
			if(Minecraft.getMinecraft().thePlayer.getEntityId() == message.eID)
				return null;
            HookProperties props = HookWrapper.getProperties( (EntityPlayer)Minecraft.getMinecraft().theWorld.getEntityByID(message.eID) );
            
            for(ActiveHook h : props.getHooks())
            {
            	if(h.getUUID().equals(message.hookId))
            		h.destroyRaw();
            }
			return null;
		}
    	
    }
	
}
