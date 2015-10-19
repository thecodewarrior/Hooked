package com.thecodewarrior.hooks;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.thecodewarrior.hooks.util.Hook;

import cpw.mods.fml.common.registry.GameRegistry;

public class ItemHook extends ItemHookProvider
{
	
	boolean multi = false;
	int count = 1;
	Hook hook;
	int durability = -1;
	String hookName;
	
	
	public ItemHook(Hook hook) 						{ this(hook, 1,     false); }
	public ItemHook(Hook hook, int count) 			{ this(hook, count, false); }
	public ItemHook(Hook hook, int count, boolean multi)
	{
		this.multi = multi;
		this.count = count;
		this.hook  = hook ;
		hookName = hook.getId().substring(hook.getId().indexOf(':')+1);
		setUnlocalizedName("hook." + hookName);
		setCreativeTab(CreativeTabs.tabTransport);
	}
	
	
	
	@Override
	public void registerIcons(IIconRegister reg)
	{
		this.itemIcon = reg.registerIcon(HookMod.MODID + ":hooks/"+hookName+"/item");
		
//		super.registerIcons(p_94581_1_);
	}
	
	public ItemHook setCount(int count)
	{
		this.count = count;
		return this;
	}
	
	public ItemHook setMulti(boolean multi)
	{
		this.multi = multi;
		return this;
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return durability != -1 && stack.getItemDamage() != 0;
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		if(stack.getItemDamage() == 0) return 1;
		double d = durability - stack.getItemDamage();
		d = d/durability;
		return 1f-d;
	}
	
	public ItemHook register(String itemId) {
		GameRegistry.registerItem(this, itemId);
		HookRegistry.registerHook(hook.getId(), hook);
		return this;
	}
	
	public ItemHook setDurability(int durability) {
		this.durability = durability;
		return this;
	}
	
	@Override
	public Hook getHook(ItemStack stack, EntityPlayer player)
	{
		return hook;
	}

	@Override
	public int getHookCount(ItemStack stack, EntityPlayer player)
	{
		return count;
	}
	
	@Override
	public boolean isMultiHook(ItemStack stack, EntityPlayer player)
	{
		return multi;
	}
	
	public void damageHook(ItemStack stack, EntityPlayer player){
		if(durability > 0)
			stack.setItemDamage(stack.getItemDamage()+1);
	}
	
	@Override
	public boolean isBroken(ItemStack stack, EntityPlayer player)
	{
		return durability > 0 && stack.getItemDamage() > durability;
	}
}
