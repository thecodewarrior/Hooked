I am currently taking a hiatus from modding, however if you are willing I would gladly accept a PR that completes this rewrite of the engine. 
The engine is _significantly_ more organized than the old one so it shouldn't be too difficult to build upon, and really the "only" thing left before feature parity is implementing the Red Hook.
If you would like to go even further, the original intent of the rewrite was to allow configurable hooks, both changing existing stats and adding entirely new hook varieties.

The red hook is relatively simple. The original system hard-coded behavior for the bounds checks for 1-4 hooks, however I can see the usefulness of having even more (e.g. 8 hooks would allow movement inside a cuboid area) and it would be consistent for the configs to allow more.
To that end how the red hook motion works is it moves the target position like creative flight (though a simplistic implementation with no momentum makes more sense and is more useful when building) but snaps it position to the closest point within a [convex hull](https://en.wikipedia.org/wiki/Convex_hull). 
The most complicated bit is calculating the hull and finding the closest point inside it, after that it's relatively easy to grab the player's forward and strafing motion and apply them to the target point. 
One configurable option that would be good to have is whether 1 hook locks you in place or allows you to drop down in a straight line, as the latter may be undesirable from a pack author's perspective in a skyblock setting.

If you decide you might want to take this up don't hesitate to contact me on CurseForge or Twitter @thecodewarrior1 and I'd be happy to answer any questions or clarify any of the code. 
If you do contact me don't feel any sense of obligation that "you accepted, now you must complete it or else!" 
If you decide against it that's fine, and if you make it halfway and become disinterested then I can merge what you have done so far and you'll give anyone else (or myself, eventually) a head start.
