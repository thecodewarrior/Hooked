package com.thecodewarrior.hooks.net.client;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.net.NetHandler;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.HookProperties;
import com.thecodewarrior.hooks.util.HookWrapper;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class RetractHookClientMessage implements IMessage
{
	
	UUID hookId;
	Vector3 location;
	
	int eID;
	
	public RetractHookClientMessage()
	{
		
	}
	
	public RetractHookClientMessage(int entityId, UUID hookId, Vector3 location)
	{
		eID = entityId;
		this.hookId = hookId;
		this.location = location;
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		eID = buf.readInt();
		hookId = NetHandler.readUUID(buf);
		location = NetHandler.readVector3(buf);
	}
	
	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(eID);
		NetHandler.writeUUID(buf, hookId);
		NetHandler.writeVector3(buf, location);
	}
	
    public static class Handler implements IMessageHandler<RetractHookClientMessage, IMessage>
    {

		@Override
		public IMessage onMessage(RetractHookClientMessage message, MessageContext ctx)
		{
			
			if(Minecraft.getMinecraft().thePlayer.getEntityId() == message.eID)
				return null;
			
            HookProperties props = HookWrapper.getProperties( (EntityPlayer)Minecraft.getMinecraft().theWorld.getEntityByID(message.eID) );
            
            for(ActiveHook h : props.getHooks())
            {
            	if(h.getUUID().equals(message.hookId))
            	{
            		h.setRetracting();
            		h.setLocation(message.location);
            	}
            }
			return null;
		}
    	
    }
	
}
