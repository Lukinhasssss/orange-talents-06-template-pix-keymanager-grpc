package br.com.lukinhasssss.services


import br.com.lukinhasssss.*
import br.com.lukinhasssss.clients.ItauClient
import br.com.lukinhasssss.entities.ChavePix
import br.com.lukinhasssss.repositories.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemoverChaveServiceTest {

    @Inject
    lateinit var pixRepository: ChavePixRepository
    @Inject
    lateinit var grpcClient: RemoverChaveServiceGrpc.RemoverChaveServiceBlockingStub

    private lateinit var chavePix: ChavePix

    @BeforeEach
    internal fun setUp() {
        pixRepository.deleteAll()
        chavePix = pixRepository.save(ChavePix(
            idCliente = "b19f2cfd-6fb4-45f3-8f24-a2b0722b4e24",
            tipoChave = TipoChave.CELULAR,
            valorChave = "+5511987654321",
            tipoConta = TipoConta.CONTA_CORRENTE
        ))
    }

    @Test
    internal fun `deve remover uma chave pix quando todos os dados forem validos`() {
        grpcClient.removerChave(
            RemoverChaveRequest.newBuilder()
            .setPixId(chavePix.pixId)
            .setIdCliente(chavePix.idCliente)
            .build())

        assertTrue(pixRepository.findAll().isEmpty())
    }

    @Test
    internal fun `nao deve remover uma chave pix e deve retornar PERMISSION_DENIED quando o cliente tentar excluir uma chave que nao e dele`() {
        val request = RemoverChaveRequest.newBuilder()
            .setPixId(chavePix.pixId)
            .setIdCliente(UUID.randomUUID().toString())
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(request)
        }

        with(exception) {
            assertEquals("Chave só pode ser removida pelo seu próprio dono!", status.description)
            assertEquals(7, status.code.value())
            assertTrue(pixRepository.findAll().isNotEmpty())
        }
    }

    @Test
    internal fun `nao deve remover uma chave pix e deve retornar NOT_FOUND quando a chave nao for encontrada`() {
        val request = RemoverChaveRequest.newBuilder()
            .setPixId(UUID.randomUUID().toString())
            .setIdCliente(chavePix.idCliente)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(request)
        }

        with(exception) {
            assertEquals("Chave não encontrada!", status.description)
            assertEquals(5, status.code.value())
            assertTrue(pixRepository.findAll().isNotEmpty())
        }
    }

    @Test
    internal fun `nao deve remover uma chave pix e deve retornar INVALID_ARGUMENT quando o id do cliente for vazio`() {
        val request = RemoverChaveRequest.newBuilder()
            .setPixId(chavePix.pixId)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(request)
        }

        with(exception) {
            assertEquals("Campo obrigatório!", status.description)
            assertEquals(3, status.code.value())
            assertTrue(pixRepository.findAll().isNotEmpty())
        }
    }

    @Test
    internal fun `nao deve remover uma chave pix e deve retornar INVALID_ARGUMENT quando o pixId for vazio`() {
        val request = RemoverChaveRequest.newBuilder()
            .setIdCliente(chavePix.idCliente)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(request)
        }

        with(exception) {
            assertEquals("Campo obrigatório!", status.description)
            assertEquals(3, status.code.value())
            assertTrue(pixRepository.findAll().isNotEmpty())
        }
    }

    @Factory
    class Remove {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RemoverChaveServiceGrpc.RemoverChaveServiceBlockingStub {
            return RemoverChaveServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ItauClient::class)
    fun itauMock(): ItauClient {
        return Mockito.mock(ItauClient::class.java)
    }

}