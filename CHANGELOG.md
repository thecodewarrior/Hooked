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
