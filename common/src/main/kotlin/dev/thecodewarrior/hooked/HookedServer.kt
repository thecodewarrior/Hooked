package dev.thecodewarrior.hooked

object HookedServer {
    fun init() {
        HookServerNetworking.registerNetworking()
    }
}