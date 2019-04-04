Welcome to the Hooked resources folder. Let me show you around and explain its layout.

## Quickstart

Add the folowing item at the end of the hook type list in `hooked.types.json`, replacing `<id>` with some custom id (allowed characters: lowercase + `_`).
```json
{
    "type": "basic",
    "id": "<id>",

    "count": 8,
    "range": 24.0,
    "speed": 1.2,
    "pullStrength": 0.8,

    "model": "hook_<id>",
    "hookModel": "hooked:hook/<id>",
    "verticalRopeTexture": "hooked:textures/hooks/<id>/chain1.png",
    "horizontalRopeTexture": "hooked:textures/hooks/<id>/chain2.png"
}
```
A collapsed version of the JSON should look like this. I use `{ ... }` as a condensed version of each item, and everything after the `//` is an explanation I've added:
```
{
    "entries": [ // the `[` starts a list
        {        // the start of an item
            ...
        },       // the end of an item. Notice the commas separating each item
        { 
            ...
        },
        { 
            ...
        },
        { 
            ...
        }        // the last item has no trailing comma
    ]
}
```

Then duplicate these files and replace every occurance of `example` in the copies' names and contents with your custom id:
- `hooked.resources/models/hook/example.json`
- `hooked.resources/models/item/hook_example.json`
- `hooked.resources/textures/items/example.png`
- `hooked.resources/textures/hooks/example/chain1.png`
- `hooked.resources/textures/hooks/example/chain2.png`
- `hooked.resources/textures/hooks/example/hook.png`

The base textures can be found [here.](https://github.com/thecodewarrior/Hooked/tree/1.12/src/main/resources/assets/hooked/textures/items)

The hook texture (`.../hooks/example/hook.png`) is laid out like so. The long parts are the sides of the cuboids and the squares are the end caps
![](./hook_model_texture.png)


## Details

The resources directory already contains the files necessary for an example hook. Simply add this to `hooked.types.json` and restart the game to get started.
```json
{
    "type": "basic",
    "id": "example",

    "count": 8,
    "range": 24.0,
    "speed": 1.2,
    "pullStrength": 0.8,

    "model": "hook_example",
    "hookModel": "hooked:hook/example",
    "verticalRopeTexture": "hooked:textures/hooks/example/chain1.png",
    "horizontalRopeTexture": "hooked:textures/hooks/example/chain2.png"
}
```

- `"type"` - The [hook type](#hook_types). 
- `"id"` - The hook ID. This must be unique and lowercase, and if it is changed or removed any hooks with the old ID will not function.
- `"count"` - The hook count.
- `"range"` - The range in blocks.
- `"speed"` - The speed of hooks being fired in blocks per tick.
- `"pullStrength"` - The speed the player should be pulled toward the target location. 

- `"model"` - The [item varient](#items), must be lowercase.
- `"hookModel"` - The location of the [hook model](#hook_models).
- `"verticalRopeTexture"` - The location of the vertical [rope texture](#textures).
- `"horizontalRopeTexture"` - The location of the horizontal [rope texture](#textures).

## <a id="hook_types"></a>Hook Types

Currently only `"basic"` and `"flight"` types are defined, and any other value will cause a crash complaining about an unknown descriminator.

### Basic

Just the standard hook that suspends you at the average of the hook positions.

### Flight

The standard flight/tether hook. When using the flight hook `"pullStrength"` only applies to the tether mechanic. Movement speed modifiers for the free flight mode have not been created yet.

When only one hook is attached it acts as a tether, keeping the player within the range when they fired it (i.e. when the hook attaches to a block its current distance to the player is recorded and the player is kept within that range). When acting as a tether double-jumping will cause the user to snap back to the hook location as if it was a standard hook, and double-jumping again will release it.

When multiple hooks are attached the player can freely move using the normal movement controls and jump/sneak buttons. If the hooks are roughly in a line the player will be limited to moving along that line, if they are roughly in a plane they will be limited to that plane, otherwise they are limited to within the convex hull of the hooks. (i.e. if you took an infinitely stretchy baloon and wrapped it around the points, that's the volume the player can move in)

## <a id="items"></a>Items

### Translation

Given the type id the item has three keys:

- `item.hooked:hook.<id>.name` - the item name
- `item.hooked:hook.<id>.tip.normal` - the item tooltip when shift isn't pressed
- `item.hooked:hook.<id>.tip.detail` - the item tooltip when shift is pressed

## <a id="textures"></a>Textures

The two rope textures (`"verticalRopeTexture"` and `"horizontalRopeTexture"`) are rendered at 90° to each other and repeated from the back of the hook to the player's waist. The chain texture is rendered 1 block wide.

## <a id="hook_models"></a>Hook Models

To create a custom model, simply create a .json block model with the back of the hook (the point where the chain attaches to the hook) at (0.5, 0, 0.5) with the hook pointing up. Then add a `"hookLength"` item to the type JSON with the length of your hook (base to tip). This is used to offset the hook position back from the impact position.

## <a id="recipes"></a>Recipes


