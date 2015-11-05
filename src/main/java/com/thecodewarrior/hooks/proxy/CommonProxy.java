package com.thecodewarrior.hooks.proxy;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.ExtendedVector3;
import com.thecodewarrior.hooks.util.HookProperties;
import com.thecodewarrior.hooks.util.HookUtil;
import com.thecodewarrior.hooks.util.HookWrapper;
import com.thecodewarrior.hooks.util.IResourceConfig;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class CommonProxy
{
	public static boolean isClient = false;

	public static boolean shouldPull = true;
	
	public void init(){}
	public void glAlign(Axis align, Vector3 along, Axis point, Axis to) {}
	public void glAlign(Vector3 align, Vector3 along, Vector3 point, Vector3 to) {}
//	Map<World, FakeEntityHook> eMap = new HashMap<World, FakeEntityHook>();
	public static boolean approxEqual(double a, double b){
	    return a == b ? true : Math.abs(a - b) < 0.0000001;
	}
	
	public static enum Axis {
		X(1,0,0), Y(0,1,0), Z(0,0,1);
		
		Vector3 vector;
		
		Axis(int x, int y, int z) {
			this.vector = new Vector3(x,y,z);
		}
		
		public Vector3 getVector()
		{
			return vector.copy();
		}
	}
	
//	@SubscribeEvent(priority=EventPriority.LOWEST)
//	public void onPlayerPreTick(TickEvent.PlayerTickEvent event) {
//		HookProperties props = HookWrapper.getProperties(event.player);
//		List<ActiveHook> hooks = props.getHooks();
//		for (ActiveHook hook : hooks)
//		{
//			
//		}
//	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if(event.phase != Phase.START)
			return;
		
		HookProperties props = HookWrapper.getProperties(event.player);
		Vector3 motion = props.hookMotion;
		props.cleanHooks();
		props.isSteady = false;
		EntityPlayer player = event.player;
		Vector3 playerCenter = new Vector3(
				(player.boundingBox.minX+player.boundingBox.maxX)/2,
				(player.boundingBox.minY+player.boundingBox.maxY)/2,
				(player.boundingBox.minZ+player.boundingBox.maxZ)/2
				);
		Vector3 playerEyes = new Vector3( RayTracer.getCorrectedHeadVec(player) );
		
		World w = event.player.worldObj;
		
		List<ActiveHook> hooks = props.getHooks();
		
		Vector3 cumulitave = new Vector3();
		int cumulitaveCount = 0;
		int index = 0;
		for (ActiveHook hook : hooks)
		{
			hook.update(player);
			if(isClientPlayer(player)) {
				hook.getHook().getRenderer().spawnParticles(hook, player);
			}
			if(hook.isStopped() && !hook.isRetracting())
			{
				cumulitave.add(hook.getPullLocation());
				cumulitaveCount++;
				continue;
			}
			Vector3 loc = hook.getLocation();
			Vector3 vel = hook.getVelocity();
			Vector3 movement = vel;
			if(hook.isRetracting())
			{
				movement = vel = loc.copy().sub(playerCenter).normalize().multiply(-hook.getHook().getRetractSpeed());
				if(loc.copy().sub(playerCenter).mag() < hook.getHook().getRetractSpeed()) {
					hook.destroy();
				}
			} else {
				ExtendedVector3<ForgeDirection> hit = HookUtil.collisionRayCast(w, loc, vel, event.player);
				movement = hit;
				if(!(  approxEqual(movement.x, vel.x) && approxEqual(movement.y, vel.y) && approxEqual(movement.z, vel.z)  ) && !hook.isStopped())
				{
					hook.setHitSide(hit.getData());
					hook.setStopped();
				}
			}
			hook.setLocation(loc.copy().add( movement ));
//			if( !hook.isStopped() &&
//				distance(playerEyes, loc) > Math.pow(hook.getHook().getLength(), 2) )
//				hook.setRetracting();
			
			index++;
		}
		props.isHooked = false;
		if(cumulitaveCount < 1 && props.wasHookedLastTick) {
			player.motionX = motion.x;
			player.motionY = motion.y;
			player.motionZ = motion.z;
			motion.x = motion.y = motion.z = 0;
			props.wasHookedLastTick = false;
		}
		else if(!props.wasHookedLastTick)
		{
			motion.x = player.motionX;
			motion.y = player.motionY;
			motion.z = player.motionZ;
			props.wasHookedLastTick = true;
		}
		if(cumulitaveCount < 1) {
			return;
		}
		player.moveForward = player.moveStrafing = 0;
//		motion.x += player.motionX;
//		motion.y += player.motionY;
//		motion.z += player.motionZ;
		player.motionX = player.motionY = player.motionZ = 0;
		
		props.isHooked = true;
		player.fallDistance = 0;
		
		boolean complicatedPhysicsStuffThatKindOfWorksButDoesntReally = true;
		double GRAVITY = 0.0784000015258789;
		motion.y -= GRAVITY;
		
		double accel = props.getPullStrength()*GRAVITY*1.05;
		double terminal = accel*4;
		double friction = 1.25;
		
		Vector3 movement = cumulitave.copy().multiply(1.0/cumulitaveCount);
		movement.sub(playerCenter);
		if(shouldPull) {
			double distance = movement.mag();
			if(Math.abs(player.posX - player.lastTickPosX) < 0.01 &&
			   Math.abs(player.posY - player.lastTickPosY) < 0.01 &&
			   Math.abs(player.posZ - player.lastTickPosZ) < 0.01 ||
			   distance < 3*accel) {
				props.isSteady = true;
				player.onGround = true;
			}
		
			if(complicatedPhysicsStuffThatKindOfWorksButDoesntReally) {
				movement.normalize();
				movement.multiply(accel);
				
				if(motion.y < 0 && movement.y < 0)
					movement.y = 0;
				if(distance < 3*accel) {
					movement.y = -motion.y;
					movement.x /= 2;
					movement.z /= 2;
				}
				
				
				
				motion.add(movement);
				Vector3 playerMotionNoFall = motion.copy();
				if(playerMotionNoFall.y < 0)
					playerMotionNoFall.y = 0;
				
				if(playerMotionNoFall.mag() > terminal) {
					motion.normalize().multiply(terminal);
				}
			} else {
				if(distance > accel) {
					movement.normalize();
					movement.multiply(accel);
				}
				
				motion.set(movement);
			}
		}
		
		if(complicatedPhysicsStuffThatKindOfWorksButDoesntReally) {			
			Vector3 playerLoc = new Vector3(player.posX, player.posY, player.posZ);
			Vector3 afterLoc = playerLoc.copy().add(motion); // get the location relative to the player
	
			for (ActiveHook hook : hooks)
			{
				if(!hook.isStopped() || hook.isRetracting()) {
					continue;
				}
				
				Vector3 hookLoc = hook.getLocation();
				Vector3 relLoc = playerLoc.copy().sub(hookLoc); // get the vector from the hook to the player
				
				// if the final position after moving is too far from the hook
				if(relLoc.mag() >= hook.getHook().getLength()) {
					
//					if(relLoc.mag() >= hook.getHook().getLength()-0.1 && relLoc.mag() <= hook.getHook().getLength()+0.1) {
//						double velSq = motion.magSquared();
//						double radius = hook.getHook().getLength();
//						double centripetalAccelMag = velSq/radius;
//						
//						Vector3 centripitalAccel = relLoc.copy().normalize().multiply(centripetalAccelMag);
//					} else {
//						relLoc.set(afterLoc).sub(hookLoc);
//						
//						relLoc.normalize(); // get the direction only, not the magnitude
//						relLoc.multiply(hook.getHook().getLength()); // make it only go to the max range of this hook
//						relLoc.add(hookLoc); // move it back to the global space
//						relLoc.sub(playerLoc); // get the distance between the player's position and the corrected final position - this is the movement
//		
//						motion.set(relLoc);
//						afterLoc.set(playerLoc).add(motion);
//					}
					
					
					Vector3 normal = relLoc.copy().normalize();
					double mag = motion.dotProduct(normal);
					double over = relLoc.mag() - hook.getHook().getLength();
					if(over > 0)
						mag += over;
					if(mag > 0) {
						Vector3 forceByChain = normal.copy().multiply(mag);
						motion.sub(forceByChain);
						
//						double velSq = motion.magSquared();
//						double radius = hook.getHook().getLength();
//						double centripetalAccelMag = velSq/radius;
//						if(centripetalAccelMag > 0) {
//							Vector3 centripitalAccel = relLoc.copy().normalize().multiply(-centripetalAccelMag);
//							
//							motion.add(centripitalAccel);
//						}
						
					}
					
//					afterLoc.set(playerLoc).add(motion);
//					relLoc.set(afterLoc).sub(hookLoc);
//					if(relLoc.mag() >= hook.getHook().getLength()) {
//						relLoc.normalize(); // get the direction only, not the magnitude
//						relLoc.multiply(hook.getHook().getLength()); // make it only go to the max range of this hook
//						relLoc.add(hookLoc); // move it back to the global space
//						relLoc.sub(playerLoc); // get the distance between the player's position and the corrected final position - this is the movement
//		
//						motion.set(relLoc);
//						afterLoc.set(playerLoc).add(motion);
//					}
				}
			}
	
//			player.motionX = motion.x;
//			player.motionY = motion.y;
//			player.motionZ = motion.z;
		}
		
		Vector3 oldPos = new Vector3(player.posX, player.posY, player.posZ); 
		player.motionX = player.motionY = player.motionZ = 1;
		player.moveEntity(motion.x, motion.y, motion.z);
		motion.x = player.motionX == 0 ? 0 : motion.x;
		motion.y = player.motionY == 0 ? 0 : motion.y;
		motion.z = player.motionZ == 0 ? 0 : motion.z;
		player.motionX = player.motionY = player.motionZ = 0;		
	}
	
	
	public double distance(Vector3 a, Vector3 b)
	{
		return MathHelper.sqrt_double(
				Math.pow(a.x-b.x, 2) +
				Math.pow(a.y-b.y, 2) +
				Math.pow(a.z-b.z, 2)
				);
	}

	public boolean isClientPlayer(Entity e)
	{
		return false;
	}
	
	public void registerResourceConfig(IResourceConfig obj) {}
	
}
