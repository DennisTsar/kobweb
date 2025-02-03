package playground

import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface MyPostService : RemoteService {
    suspend fun sayHello(firstName: String, lastName: String, age: Int): String
}
