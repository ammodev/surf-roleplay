package dev.slne.surf.roleplay.core.client.common.proxies

import dev.slne.surf.roleplay.core.client.common.ClientInstance
import dev.slne.surf.roleplay.core.common.user.rpc.UserService

val userProxy by lazy {
    ClientInstance.INSTANCE.rabbitApi.createRpcService<UserService>()
}