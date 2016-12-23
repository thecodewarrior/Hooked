package com.thecodewarrior.hooks.item;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.oredict.OreDictionary;

import com.opencsv.CSVReader;
import com.thecodewarrior.hooks.HookMod;
import com.thecodewarrior.hooks.HookRegisterHelper;
import com.thecodewarrior.hooks.util.HookUtil;

public class ItemRegisterer
{
	
	HookRegisterHelper h;
	
	
	List<String[]> data;
	public void init() {
		data = new ArrayList<String[]>();
		
		h = new HookRegisterHelper();
		
		try
		{
			BufferedReader csvBufferedReader = new BufferedReader(new InputStreamReader(HookMod.class.getResourceAsStream("/data/hooks.csv"), "UTF-8"));
			
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
			if( line[0].equals("") || OreDictionary.doesOreNameExist(line[0]) ) {
				registerHook(line);
			}
		}
	}
	
	private void registerHook(String[] hook) {
		int col = 1;
		
		String domain   = hook[col++].trim().toLowerCase();
		String name     = hook[col++].trim().toLowerCase();
		double length   = HookUtil.parseWithDefault(5d, hook[col++].trim());
		double speed    = HookUtil.parseWithDefault(5d, hook[col++].trim());
		double retract  = HookUtil.parseWithDefault(5d, hook[col++].trim());
		int count       = HookUtil.parseWithDefault(1,  hook[col++].trim());
		int durability  = HookUtil.parseWithDefault(1,  hook[col++].trim());

		
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
}
