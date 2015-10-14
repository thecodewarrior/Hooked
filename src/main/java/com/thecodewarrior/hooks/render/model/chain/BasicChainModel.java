package com.thecodewarrior.hooks.render.model.chain;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.thecodewarrior.hooks.HookMod;
import com.thecodewarrior.hooks.render.model.IChainModel;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.HookClientUtil;
import com.thecodewarrior.hooks.util.HookUtil;

public class BasicChainModel implements IChainModel
{
	private ResourceLocation chain;
	
	private double chainTextureSize = 16;
	private double chainLinkOffset = 16;
	private double chainLineOffset = 0;
	
	private double[] randomTwists = new double[1000];
	
	public BasicChainModel()
	{
		for (int i = 0; i < randomTwists.length; i++)
		{
			randomTwists[i] = (Math.random()-0.5)*10;
		}
	}
	
	@Override
	public void constructTexturesFor(String hookName)
	{
		chain = new ResourceLocation(HookMod.MODID, "textures/hooks/" + hookName + "/chain.png");
	}
	
	@Override
	public void processConfig(Map<String, String> data)
	{
		chainTextureSize = HookUtil.parseWithDefault( chainTextureSize, data.get("chain.link.textureSize") );
		chainLinkOffset  = HookUtil.parseWithDefault( chainLinkOffset,  data.get("chain.link.offset" ) );
		chainLineOffset  = HookUtil.parseWithDefault( chainLineOffset,  data.get("chain.offset" ) );
	}
	
	@Override
	public void draw(ActiveHook hook, double length)
	{
		Minecraft mc = Minecraft.getMinecraft();
		Tessellator t = Tessellator.instance;
		
		GL11.glPushMatrix();
		
		double size = 0.5;
		double offset = size*(chainLinkOffset/chainTextureSize);
		mc.renderEngine.bindTexture(chain);
		int i = 0;
		for(double d = -(size*chainLineOffset/chainTextureSize); d < (length-offset); d += offset) {
			t.startDrawingQuads();
			HookClientUtil.renderFace(
				 size/2, d     , 0,  1, 0,
				-size/2, d     , 0,  0, 0,
				-size/2, d+size, 0,  0, 1,
				 size/2, d+size, 0,  1, 1
			);
			t.draw();
			if(i%2 == 0)
				GL11.glRotated(   90 + randomTwists[i%randomTwists.length]  , 0, 1, 0);
			else
				GL11.glRotated(-( 90 + randomTwists[i%randomTwists.length] ), 0, 1, 0);
			i++;
		}
		
		GL11.glPopMatrix();
	}
	
}
