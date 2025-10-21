# 2.0.2
- Fix exploit allowing unlimited creative flight by corrupting the red hook's bounding region
- Make the red hook's bounding box harder to corrupt

# 2.0.1
- Improve jump assist reliability
- Improve jump assist game feel (the jump assist won't reduce jump height below default)
- Fix a couple of texture inconsistencies
- (Fabric) Allow swapping hooks by right-clicking the hook item
- Fix a missing damage type check causing the player to be immune to all damage when using a hook (#43)
- Fix coplanar redstone hooks sometimes leading to errors ticking the player (#44)

# 2.0.0
- No changes from beta.4

# 2.0.0-beta.4
- Flattened hook behavior item components (instead of `hooked:behavior={"type":"hooked:basic","pullStrength":1.5}` you can just do `hooked:pull_strength=1.5`)
- Expanded datapack docs in readme
- Fix duplicate hook audio for real this time

# 2.0.0-beta.3
- Increase redstone hook max range
- Make flight hooks increase break range of planted hooks

# 2.0.0-beta.2
- Fix duplicate hook hit sounds in singleplayer
- Rename Red Hook to Redstone Hook
- Change default keybind to `C`
  - This required a bit of a workaround in Fabric due to the conflict with vanilla's "Save Hotbar Activator" key

# 2.0.0-beta.1
- Reworked networking to improve client-side consistency
  - Client state is prioritized over server state, reducing desyncs which can cause hooks to fail at critical moments

# 2.0.0-alpha.7
- Created blank `hooked:custom_hook` for use in data packs
- Fixed advancements not correctly granting recipes
- Hooks trigger Sculk Sensors when they land (frequency = 2, same as projectile landing)
- Fixed hook slot background colors not matching vanilla
- (NeoForge) Fixed mining speed compensation not working
- (NeoForge) Fixed right-clicking the hook not equipping it
- (NeoForge) Fixed Curio slot not having a translation
- (Fabric) Moved Trinkets slot to the chest instead of legs
