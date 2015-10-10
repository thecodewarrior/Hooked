package com.thecodewarrior.hooks.net.client;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.HookRegistry;
import com.thecodewarrior.hooks.net.NetHandler;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.HookProperties;
import com.thecodewarrior.hooks.util.HookWrapper;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class FireHookClientMessage implements IMessage
{
    private String  hookId;
    private UUID    uuid;
    private Vector3 start;
    private Vector3 heading;
    
	int eID;

    public FireHookClientMessage() { }

    public FireHookClientMessage(int entityId, String hookId, UUID id, Vector3 start, Vector3 heading) 
    {
		eID = entityId;
        this.hookId  = hookId;
        this.uuid    = id;
        this.start   = start;
        this.heading = heading;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
		eID = buf.readInt();
    	hookId = ByteBufUtils.readUTF8String(buf);
    	uuid = NetHandler.readUUID(buf);
    	
    	start = NetHandler.readVector3(buf);
    	heading = NetHandler.readVector3(buf);
    	
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
		buf.writeInt(eID);
        ByteBufUtils.writeUTF8String(buf, hookId);
        NetHandler.writeUUID(buf, uuid);
        
        NetHandler.writeVector3(buf, start);
        NetHandler.writeVector3(buf, heading);

    }

    public static class Handler implements IMessageHandler<FireHookClientMessage, IMessage>
    {
        
        @Override
        public IMessage onMessage(FireHookClientMessage message, MessageContext ctx)
        {
            if(Minecraft.getMinecraft().thePlayer.getEntityId() == message.eID)
				return null;
            HookProperties props = HookWrapper.getProperties( (EntityPlayer)Minecraft.getMinecraft().theWorld.getEntityByID(message.eID) );
            
            ActiveHook ahook = new ActiveHook(HookRegistry.getHook(message.hookId));
            ahook.setLocation(message.start);
    		ahook.setHeading(message.heading.normalize());
    		ahook.setUUID(message.uuid);
    		props.addHook(ahook);
    		
            return null; // no response in this case
        }
    }
}
