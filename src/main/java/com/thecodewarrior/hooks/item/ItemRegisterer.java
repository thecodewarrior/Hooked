package com.thecodewarrior.hooks.item;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.oredict.OreDictionary;

import com.opencsv.CSVReader;
import com.thecodewarrior.hooks.HookRegisterHelper;
import com.thecodewarrior.hooks.util.HookUtil;

import cpw.mods.fml.common.Optional;

public class ItemRegisterer
{
	
	HookRegisterHelper h;
	
	
	List<String[]> data;
	public void init() {
		data = new ArrayList<String[]>();
		
		h = new HookRegisterHelper();
		
		try
		{
			BufferedReader csvBufferedReader = new BufferedReader(new InputStreamReader(getClass().getClassLoader()
					.getResourceAsStream("com/thecodewarrior/hooks/data/hooks.csv"), "UTF-8"));
			
			CSVReader reader = new CSVReader(csvBufferedReader);
			data = reader.readAll();
			data.remove(0); // titles
			reader.close();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		for (String[] line : data)
		{
//			if( line[0].equals("") || OreDictionary.doesOreNameExist(line[0]) ) {
				registerHook(line);
//			}
		}
	}
	
	private void registerHook(String[] hook) {
		int col = 1;
		
		String domain  = hook[col++].trim().toLowerCase();
		String name    = hook[col++].trim().toLowerCase();
		double length  = HookUtil.parseWithDefault(5d, hook[col++].trim());
		double speed   = HookUtil.parseWithDefault(5d, hook[col++].trim());
		double retract = HookUtil.parseWithDefault(5d, hook[col++].trim());
		int count      = HookUtil.parseWithDefault(1,  hook[col++].trim());
		int durability = HookUtil.parseWithDefault(1,  hook[col++].trim());
		
		boolean multi = false;
		String multiString = hook[col++].trim().toLowerCase();
		if(multiString.equals("yes") || multiString.equals("true") || multiString.equals("1")) {
			multi = true;
		}
		
		h.reset(name);
		if(domain.length() > 0)
			h.setDomain(domain);
		h.setLength(length);
		h.setSpeed(speed);
		h.setRetractSpeed(retract);
		h.setCount(count);
		h.setDurability(durability);
		h.setMulti(multi);
		h.register();

	}
	
	public void vanilla() {}
	public void thermalFoundation() {}
	
	// Copper, Tin, Bronze, Hepatizon, Angmallen, Damascus Steel, Manganese, and Steel
	public void metallurgy_base() {}
	
	// Zinc, Brass, Silver, Electrum, and Platinum
	public void metallurgy_precious() {}
	
	// Ignatius, Shadow Iron, Lemurite, Shadow Steel, Midasium, Vyroxeres, Ceruclase, Adluorite, Inolashite, Kalendrite, Amordrine, Vulcanite, and Sanguinite
	public void metallurgy_nether() {}
	
	// Prometheum, Deep Iron, Infusolium, Black Steel, Oureclase, Aredrite, Astral Silver, Carmot, Mithril, Rubracium, Quicksilver, Haderoth, Orichalcum, Celenegil, Adamantine, Atlarus, and Tartarite
	public void metallurgy_fantasy() {}
	
	// Eximite, Meutoite, and Desichalkos
	public void metallurgy_ender() {}
	
	public static class ActualRegisterer extends ItemRegisterer {
		@Override
		public void vanilla() {
//			h.reset("iron");
//			h.setLength(10).setSpeed(5).setRetractSpeed(10).setDurability(256);
//			h.register();
//			
//			h.reset("gold");
//			h.setLength(35).setSpeed(35).setRetractSpeed(35).setDurability(64).setCount(4);
//			h.register();
//			
//			h.reset("diamond");
//			h.setLength(15).setSpeed(20).setRetractSpeed(40).setDurability(2048).setCount(3);
//			h.register();
//			
//			h.reset("emerald");
//			h.setLength(20).setSpeed(20).setRetractSpeed(40).setUnbreakable().setCount(3);
//			h.register();
//			
//			h.reset("ender");
//			h.setLength(25).setSpeed(25 * 20 / 2).setRetractSpeed(25 * 20 / 2).setUnbreakable().setCount(3);
//			h.register();
//			
//			h.reset("slime");
//			h.setLength(25).setSpeed(30).setRetractSpeed(60).setUnbreakable().setCount(3);
//			h.register();
//			
//			h.reset("wither");
//			h.setLength(30).setSpeed(30).setRetractSpeed(60).setUnbreakable().setCount(4);
//			h.register();
		}
		
		@Override
		@Optional.Method(modid = "thermalfoundation")
		public void thermalFoundation() {
//			h.reset("invar");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
//			
//			h.reset("electrum");
//			h.setLength(15).setSpeed(15).setRetractSpeed(30).setDurability(512);
//			h.register();
//			
//			h.reset("signalum");
//			h.setLength(10).setSpeed(20).setRetractSpeed(40).setDurability(512);
//			h.register();
//			
//			h.reset("enderium");
//			h.setLength(15).setSpeed(30).setRetractSpeed(60).setDurability(4096);
//			h.register();
//			
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
		}
		
		// Copper, Tin, Bronze, Hepatizon, Angmallen, Damascus Steel, Manganese, and Steel
		@Override
		public void metallurgy_base() {
//			h.reset("angmallen");
//			h.setLength(35).setSpeed(35).setRetractSpeed(70).setCount(4).setDurability(64).setMulti();
//			h.register();
//			
//			h.reset("damascus_steel");
//			h.setLength(10).setSpeed(10).setRetractSpeed(10).setDurability(500);
//			h.register();
//			
//			h.reset("hepatizon");
//			h.setLength(10).setSpeed(5).setRetractSpeed(10).setCount(2).setDurability(300).setMulti();
//			h.register();
//			
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
//			
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
			
		}
		
		// Zinc, Brass, Silver, Electrum, and Platinum
		@Override
		public void metallurgy_precious() {
//			h.reset("brass");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
//			
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
//			
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
//			
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
		}
		
		// Ignatius, Shadow Iron, Lemurite, Shadow Steel, Midasium, Vyroxeres, Ceruclase, Adluorite, Inolashite, Kalendrite, Amordrine, Vulcanite, and Sanguinite
		@Override
		public void metallurgy_nether() {
			
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
//			
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
//			
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
//			
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
		}
		
		// Prometheum, Deep Iron, Infusolium, Black Steel, Oureclase, Aredrite, Astral Silver, Carmot, Mithril, Rubracium, Quicksilver, Haderoth, Orichalcum, Celenegil, Adamantine, Atlarus, and Tartarite
		@Override
		public void metallurgy_fantasy() {
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
//			
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
//			
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
//			
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
		}
		
		// Eximite, Meutoite, and Desichalkos
		@Override
		public void metallurgy_ender() {
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
//			
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
//			
//			h.reset("shiny");
//			h.setLength(15).setSpeed(10).setRetractSpeed(20).setDurability(512);
//			h.register();
		}
	}
}
