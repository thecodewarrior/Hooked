package com.thecodewarrior.hooks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.thecodewarrior.hooks.util.Hook;

public abstract class ItemHookProvider extends Item
{
	
	public abstract Hook getHook(ItemStack stack, EntityPlayer player);
	public abstract int  getHookCount(ItemStack stack, EntityPlayer player);
	public abstract boolean isBroken(ItemStack stack, EntityPlayer player);
	public void damageHook(ItemStack stack, EntityPlayer player){
		stack.setItemDamage(stack.getItemDamage()+1);
	}
	
	public boolean isMultiHook(ItemStack stack, EntityPlayer player)
	{
		return true;
	}
	
}
