package games.thecodewarrior.hooked.util

import net.minecraft.util.Identifier

fun ident(str: String) = Identifier(str)
fun ident(namespace: String, path: String) = Identifier(namespace, path)
