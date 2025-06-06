# Datapacks

Example commands:
- A diamond hook with 100 block range and 20 hooks:
  `/give @p hooked:diamond_hook[hooked:hook_range=100,hooked:hook_count=20]`
- An ender hook with 999 block range: (the speed is equal to the range so it still reaches the target in one tick)  
  `/give @p hooked:ender_hook[hooked:hook_range=999,hooked:hook_speed=999]` 
- An ender hook that pulls the user 10 times faster than normal:
  `/give @p hooked:ender_hook[hooked:pull_strength=22.5]`
- A diamond hook that grants flight like the redstone hook:
  `/give @p hooked:diamond_hook[hooked:behavior="hooked:flight",hooked:wireframe_color=[0.42,0.83,0.85]]`

## Item components

- `hooked:hook_count` - `int`
  - The maximum number of hooks
- `hooked:hook_range` - `double`
  - The maximum range of the hooks
- `hooked:hook_speed` - `double`
  - The hook speed in blocks per tick
- `hooked:fire_cooldown` - `int`
  - The fire cooldown in ticks
- `hooked:behavior` - one of:
  - `"hooked:basic"` - the standard hook behavior (configured using `hooked:pull_strength`)
  - `"hooked:flight"` - the flight hook behavior (configured using `hooked:wireframe_color` and `hooked:break_range_factor`)
- `hooked:pull_strength` - (basic behavior) optional max speed to pull the player in blocks per tick (default `1`)
- `hooked:wireframe_color` - (flight behavior) optional three-component vector defining the rgb color of the wireframe guide (default `[1,0,0]`)
- `hooked:break_range_factor` - (flight behavior) optional multiplication factor for the break range of planted hooks (default `4`)
- `hooked:hook_model` - object with keys:
  - `model` - optional location for the hook .obj file (default `hooked:models/hook/base.obj`)
  - `texture` - required location for the hook texture file (reference map: `hooked:textures/hook/base/hook.png`)
  - `hookLength` - optional length of the hook model, used for positioning (default `0.5`)
- `hooked:chain_appearance` - object with keys:
  - `texture1` - texture location for the main chain axis
  - `texture2` - texture location for the cross chain axis
  - `playerGap` - gap between the player and where the chain starts rendering
  - `particleColorMin` - optional three-component vector defining the minimum rgb values for the chain particles (particles will be used if this is set)
  - `particleColorMax` - optional three-component vector defining the maximum rgb values for the chain particles
