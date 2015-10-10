package com.thecodewarrior.hooks.net.server;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.HookMod;
import com.thecodewarrior.hooks.ItemHookProvider;
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
            
            ItemStack handStack = player.inventory.getCurrentItem();
            ItemStack toDelete = null;
            boolean didDamage = false;
            if(handStack != null && handStack.getItem() instanceof ItemHookProvider) {
				ItemHookProvider prov = (ItemHookProvider) handStack.getItem();
				if(prov.getHook(handStack, player) == hook) {
					prov.damageHook(handStack, player);
					if(prov.isBroken(handStack, player)) {
						toDelete = handStack;
					}
					player.inventoryContainer.detectAndSendChanges();
					didDamage = true;
				}
			}
            if(!didDamage) {
	            for (ItemStack stack : player.inventory.mainInventory)
				{
					if(stack != null && stack.getItem() instanceof ItemHookProvider) {
						ItemHookProvider prov = (ItemHookProvider) stack.getItem();
						if(prov.getHook(stack, player) == hook) {
							prov.damageHook(stack, player);
							if(prov.isBroken(stack, player)) {
								toDelete = handStack;
							}
							player.inventoryContainer.detectAndSendChanges();
							break;
						}
					}
				}
            }
            
            if(toDelete != null) {
            	for (int i = 0; i < player.inventory.mainInventory.length; i++)
				{
					if(player.inventory.mainInventory[i] == toDelete) {
						player.inventory.setInventorySlotContents(i, null);
						break;
					}
				}
            }

            NetHandler.CHANNEL.sendToAllAround(new LatchHookClientMessage(player.getEntityId(), message.hookId, message.location),
            					new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 160));
            return null;
		}
    	
    }
	
}
