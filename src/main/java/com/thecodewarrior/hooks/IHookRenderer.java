package com.thecodewarrior.hooks;

import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.util.ActiveHook;

public interface IHookRenderer
{
	
	/**
	 * Render the hook, with the origin at the center of the hook's bounding box and the y-axis toward the player
	 * @param hook The Hook to render
	 */
	public void renderHook(ActiveHook hook, Vector3 pointing);
	
	
	/**
	 * Render the line between the player and the hook
	 * The origin is at the center of the hook's bounding box and the y-axis is toward the player
	 * @param hook The Hook to render
	 * @param length The length of line to render
	 */
	public void renderLine(ActiveHook hook, Vector3 offsetToPlayer);
}
