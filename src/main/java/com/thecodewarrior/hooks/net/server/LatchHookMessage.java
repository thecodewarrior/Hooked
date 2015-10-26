package com.thecodewarrior.hooks.net.server;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import thecodewarrior.equipment.api.EquipmentApi;
import thecodewarrior.equipment.common.container.InventoryEquipment;
import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.HookMod;
import com.thecodewarrior.hooks.item.ItemHookProvider;
import com.thecodewarrior.hooks.net.NetHandler;
import com.thecodewarrior.hooks.net.client.LatchHookClientMessage;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.Hook;
import com.thecodewarrior.hooks.util.HookProperties;
import com.thecodewarrior.hooks.util.HookWrapper;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class LatchHookMessage implements IMessage
{
	
	UUID hookId;
	Vector3 location;
	
	
	public LatchHookMessage()
	{
		
	}
	
	public LatchHookMessage(UUID hookId, Vector3 location)
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
	
    public static class Handler implements IMessageHandler<LatchHookMessage, IMessage>
    {

		@Override
		public IMessage onMessage(LatchHookMessage message, MessageContext ctx)
		{
			EntityPlayer player = ctx.getServerHandler().playerEntity;
            HookProperties props = HookWrapper.getProperties(player);
            Hook hook = null;
            for(ActiveHook h : props.getHooks())
            {
            	if(h.getUUID().equals(message.hookId))
            	{
            		hook = h.getHook();
            		h.setStopped();
            		h.setLocation(message.location);
            		break;
            	}
            }
            
            ItemStack slotStack = EquipmentApi.getEquipment(player, HookMod.equipmentSlotId);
            ItemStack toDelete = null;
            if(slotStack != null && slotStack.getItem() instanceof ItemHookProvider) {
				ItemHookProvider prov = (ItemHookProvider) slotStack.getItem();
				if(prov.getHook(slotStack, player) == hook) {
					prov.damageHook(slotStack, player);
					if(prov.isBroken(slotStack, player)) {
						( (InventoryEquipment)EquipmentApi.getEquipment(player) ).setStack(HookMod.equipmentSlotId, null);
					}
				}
			}

            NetHandler.CHANNEL.sendToAllAround(new LatchHookClientMessage(player.getEntityId(), message.hookId, message.location),
            					new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 160));
            return null;
		}
    	
    }
	
}
