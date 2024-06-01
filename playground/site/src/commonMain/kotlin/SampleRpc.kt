import kotlinx.coroutines.flow.StateFlow
import kotlinx.rpc.RPC
import kotlinx.rpc.RPCEagerField

interface MyService : RPC {
    @RPCEagerField
    val myFlow: StateFlow<String>
    suspend fun sayHello(firstName: String, lastName: String, age: Int): String
}
