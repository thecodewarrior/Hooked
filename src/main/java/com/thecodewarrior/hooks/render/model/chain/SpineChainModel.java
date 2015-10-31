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

public class SpineChainModel implements IChainModel
{
	
	ResourceLocation tex;
	ResourceLocation perpTex;
	
	@Override
	public void constructTexturesFor(String hookName)
	{
		// TODO Auto-generated method stub
		tex = new ResourceLocation(HookMod.MODID + ":textures/hooks/" + hookName + "/chain.png");
		perpTex = new ResourceLocation(HookMod.MODID + ":textures/hooks/" + hookName + "/vertebra.png");
	}
	
	@Override
	public void processConfig(Map<String, String> data)
	{
		overlap = HookUtil.parseWithDefault(overlap, data.get("chain.overlap"));
		texHeight = HookUtil.parseWithDefault(texHeight, data.get("chain.tex.height"));
		space = HookUtil.parseWithDefault(space, data.get("chain.spacing"));
		vertStart = HookUtil.parseWithDefault(vertStart, data.get("chain.spacing.start"));
		scale = 0.25*HookUtil.parseWithDefault(1d, data.get("chain.scale"));
	}
	int texHeight = 16;
	int overlap = 0;
	int space = -1;
	int vertStart = 0;
	double sideLen = Math.sqrt(0.5);
	double scale = 0.25;

	@Override
	public void draw(ActiveHook hook, double length)
	{
		double w = 48;
		
		GL11.glPushMatrix();
//		GL11.glTranslated(0, 2, 0);
		GL11.glScaled(scale, scale, scale);
		Minecraft mc = Minecraft.getMinecraft();
		Tessellator t = Tessellator.instance;
		mc.renderEngine.bindTexture(tex);
		
		double l = 2;
		double px = l/(float)texHeight;
		GL11.glPushMatrix();
		for(double d = 0; d+l*scale < length; d += (l-overlap*px)*scale) {
			
			t.startDrawingQuads();

			HookClientUtil.renderFace(
					sideLen, 0, 0,			16/w, 0,
					0, 		 0, sideLen, 	24/w, 0,
					0, 		 l, sideLen, 	24/w, 1,
					sideLen, l, 0,			16/w, 1);
			
			HookClientUtil.renderFace(
					sideLen, 0, 0,			16/w, 0,
					0, 		 0, -sideLen,	8/w,  0,
					0, 		 l, -sideLen,	8/w,  1,
					sideLen, l, 0,			16/w, 1);
			
			HookClientUtil.renderFace(
					-sideLen, 0, 0, 		32/w, 0,
					0, 		  0, sideLen, 	24/w, 0,
					0, 		  l, sideLen, 	24/w, 1,
					-sideLen, l, 0, 		32/w, 1);
			
			HookClientUtil.renderFace(
					-sideLen, 0, 0, 		0,   0,
					0, 		  0, -sideLen,	8/w, 0,
					0, 		  l, -sideLen,	8/w, 1,
					-sideLen, l, 0, 		0,   1);
			HookClientUtil.renderFace(
					sideLen+1, 0, 0, 		32/w, 0,
					sideLen+1, l, 0, 		32/w, 1,
					sideLen-1, l, 0, 		48/w, 1,
					sideLen-1, 0, 0, 		48/w, 0
					);
			
			t.draw();
			
			GL11.glTranslated(0, l-overlap*px, 0);
		}
		GL11.glPopMatrix();
		
		if(space > 0) {
		
			mc.renderEngine.bindTexture(perpTex);
			GL11.glPushMatrix();
			if(space != -1) {
				GL11.glTranslated(0, vertStart*px, 0);
				for(double d = vertStart*px; d+space*px*scale < length; d += space*px*scale) {
					
					t.startDrawingQuads();
	
					HookClientUtil.renderFace(
							 2*sideLen, 0,  2*sideLen, 	1, 0,
							 2*sideLen, 0, -2*sideLen, 	0, 0,
							-2*sideLen, 0, -2*sideLen, 	0, 1,
							-2*sideLen, 0,  2*sideLen, 	1, 1);
					
					t.draw();
					
					GL11.glTranslated(0, space*px, 0);
				}
			}
			GL11.glPopMatrix();
		
		}
		
		GL11.glPopMatrix();
	}
	
}
