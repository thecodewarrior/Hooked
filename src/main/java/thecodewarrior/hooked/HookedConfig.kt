package thecodewarrior.hooked

import com.teamwizardry.librarianlib.features.config.ConfigIntRange
import com.teamwizardry.librarianlib.features.config.ConfigProperty

object HookedConfig {

    @ConfigProperty(category = "hooked", comment = """A bitmask of the locations that should be searched for hooks.
To get the value, add together the codes for the locations you want to be searched. The numbers in parentheses indicate the order the locations are searched.
1 = Baubles (1)
2 = Hands (2)
4 = Hotbar (3)
8 = Main Inventory (4)
e.g. for baubles only use '1', for baubles + hands use '3' (1+2), for inventory + hands use '10' (8+2)""")
    @ConfigIntRange(1, 15)
    var searchLocations: Int = 1
    const val SEARCH_BAUBLES = 1
    const val SEARCH_HANDS = 2
    const val SEARCH_HOTBAR = 4
    const val SEARCH_INVENTORY = 8

}