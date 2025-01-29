import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface MyService : RemoteService {
    suspend fun keyFlow(): Flow<String>
    suspend fun sayHello(firstName: String, lastName: String, age: Int): String
}

@Rpc
interface MyPostService : RemoteService {
    suspend fun sayHello(firstName: String, lastName: String, age: Int): String
}
