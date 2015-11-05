package com.thecodewarrior.hooks.render.model.particle;

import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.render.model.IParticleSpawner;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.HookUtil;

public class BasicParticleSpawner implements IParticleSpawner
{
	@Override
	public void constructTexturesFor(String hookName) {
		
	}
	
	@Override
	public void processConfig(Map<String, String> data) {
		if(data.get("particle.name") != null)
			name = data.get("particle.name");
		frequency = HookUtil.parseWithDefault(frequency, data.get("particle.frequency"));
		radius = HookUtil.parseWithDefault(radius, data.get("particle.distance"));
		double offsetX = HookUtil.parseWithDefault(0d, data.get("particle.offset.x"));
		double offsetY = HookUtil.parseWithDefault(0d, data.get("particle.offset.y"));
		double offsetZ = HookUtil.parseWithDefault(0d, data.get("particle.offset.z"));
		offset = new Vector3(offsetX, offsetY, offsetZ);
	}
	Vector3 offset = Vector3.zero;
	double frequency = 1;
	double radius = 0.1;
	String name = "smoke";
	
	@Override
	public void spawnParticles(EntityPlayer player, ActiveHook hook, Vector3 adjustedChainEnd)
	{
		Vector3 p = HookUtil.getPlayerHookPoint(player);
		Vector3 vecToPlayer = adjustedChainEnd.copy().sub(p);
		
		double distance = vecToPlayer.mag();
//		double radius = 0.1;
//		String name = "smoke";
		Rotation rot = HookUtil.getAlignYRotation(vecToPlayer.copy().normalize().multiply(-1));
		
//		int count = (int)(  frequency*(Math.random()+0.75) *distance);//2+(int)( (Math.random()-0.5) * 1 );
		double div = frequency < 1 ? 2 : 2/frequency;
		for(int i = 0; i < distance/div; i++) {
			
			if( ( frequency < 1  && Math.random() < frequency ) ||
				( frequency >= 1 && Math.random()*frequency > frequency/2 )
			) {} else { continue; }
			
			Vector3 position = new Vector3();
			position.y = Math.random()*distance;
			position.x = (Math.random()-0.5)*radius;
			position.z = (Math.random()-0.5)*radius;
			if(rot != null)
				rot.apply(position);
			position.add(offset);
//			position.y -= 0.5;
			position.add(adjustedChainEnd);
			player.worldObj.spawnParticle(name, position.x, position.y, position.z, 0, 0, 0);
		}
	}
	
}
