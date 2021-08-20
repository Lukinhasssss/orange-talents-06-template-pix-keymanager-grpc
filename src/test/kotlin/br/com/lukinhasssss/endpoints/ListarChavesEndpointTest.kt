package br.com.lukinhasssss.endpoints

import br.com.lukinhasssss.ListarChavesRequest
import br.com.lukinhasssss.ListarChavesServiceGrpc
import br.com.lukinhasssss.TipoChave
import br.com.lukinhasssss.TipoConta
import br.com.lukinhasssss.clients.BCBClient
import br.com.lukinhasssss.entities.ChavePix
import br.com.lukinhasssss.repositories.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ListarChavesEndpointTest {

    @Inject
    lateinit var pixRepository: ChavePixRepository

    @Inject
    lateinit var grpcClient: ListarChavesServiceGrpc.ListarChavesServiceBlockingStub

    @BeforeEach
    internal fun setUp() {
        pixRepository.deleteAll()
        pixRepository.save(ChavePix(
            idCliente = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            tipoChave = TipoChave.CELULAR,
            valorChave = "+5511987654321",
            tipoConta = TipoConta.CONTA_CORRENTE
        ))
        pixRepository.save(ChavePix(
            idCliente = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            tipoChave = TipoChave.EMAIL,
            valorChave = "luffy@gmail.com",
            tipoConta = TipoConta.CONTA_CORRENTE
        ))
    }

    @Test
    internal fun `deve listar todas as chaves pix de um cliente`() {
        val request = ListarChavesRequest.newBuilder().setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890").build()

        val response = grpcClient.listarChaves(request)

        with(response) {
            assertTrue(response.chavesList[0].idCliente.isNotEmpty())
            assertTrue(response.chavesList[0].pixId.isNotEmpty())
            assertTrue(response.chavesList[0].tipoChave.name.isNotEmpty())
            assertTrue(response.chavesList[0].valorChave.isNotEmpty())
            assertTrue(response.chavesList[0].tipoConta.name.isNotEmpty())
            assertTrue(response.chavesList[0].criadoEm != null)
            assertTrue(response.chavesList[1].idCliente.isNotEmpty())
            assertTrue(response.chavesList[1].pixId.isNotEmpty())
            assertTrue(response.chavesList[1].tipoChave.name.isNotEmpty())
            assertTrue(response.chavesList[1].valorChave.isNotEmpty())
            assertTrue(response.chavesList[1].tipoConta.name.isNotEmpty())
            assertTrue(response.chavesList[1].criadoEm != null)
            assertTrue(response.chavesList.size == 2)
        }
    }

    @Test
    internal fun `deve retornar uma lista vazia quando o cliente nao for encontrado`() {
        val request = ListarChavesRequest.newBuilder().setIdCliente(UUID.randomUUID().toString()).build()

        val response = grpcClient.listarChaves(request)

        with(response) {
            assertTrue(response.chavesList.isEmpty())
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando o idCliente nao for um UUID valido`() {
        val request = ListarChavesRequest.newBuilder().setIdCliente("65s1fb65sd4fb91").build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.listarChaves(request)
        }

        with(exception) {
            assertEquals("IdCliente não é um UUID válido!", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Factory
    class Listar {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ListarChavesServiceGrpc.ListarChavesServiceBlockingStub {
            return ListarChavesServiceGrpc.newBlockingStub(channel)
        }
    }

}