import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface MyService : RemoteService {
    //    fun keyFlow(): Flow<String>
    suspend fun sayHello(firstName: String, lastName: String, age: Int): String
}
