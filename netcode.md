# Netcode

I'll attempt to describe how the netcode works here, mostly for my own reference later. In server communication I will 
use the term "the client" to refer to the player whose hooks we're processing, and "other clients" to refer to other 
players in the vicinity. I will also use the term "canonical" to refer to the state on the server, since the server is
king.

## Syncing

On the client, so long as the syncing is up to date, the player's own hooks are always in the canonical order. However, 
other players' hooks may not be in canonical order. For this reason controllers are only ticked on the client for the
client's player. Other players' hooks will still move and hit blocks on their own, but the controller will not be 
invoked for them.

## Firing a hook

To fire a hook:

- **[Client]** Adds a hook to the client-side `HookedPlayerData` with a random UUID [1]
- **[Client]** Sends a `FireHookPacket` that contains the firing parameters (start position + direction) to the server
- **[Server]** Receives the `FireHookPacket` and performs some validation on the inputs
- **[Server]** If the packet fails validation,
  - **[Server]** The full `HookedPlayerData` is sent to the client in a `SyncHookedDataPacket` to correct the discrepancy
- **[Server]** If the packet succeeds validation, 
  - **[Server]** The server adds a hook to the server-side `HookedPlayerData` with a random UUID
  - **[Server]** The full `HookedPlayerData` is sent back to the player in a `SyncHookedDataPacket`, ensuring that 
    everything on the client is exactly up to date.
  - **[Server]** A `SyncHookPacket` is sent to other clients

[1]: The client-side hook with this temporary UUID will be seamlessly replaced by the server-side hook with the 
canonical UUID
