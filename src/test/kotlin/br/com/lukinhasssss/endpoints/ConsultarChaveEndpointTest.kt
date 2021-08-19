package br.com.lukinhasssss.endpoints

import br.com.lukinhasssss.ConsultarChaveRequest
import br.com.lukinhasssss.ConsultarChaveServiceGrpc
import br.com.lukinhasssss.TipoChave
import br.com.lukinhasssss.TipoConta
import br.com.lukinhasssss.clients.BCBClient
import br.com.lukinhasssss.clients.BankAccountResponse
import br.com.lukinhasssss.clients.OwnerResponse
import br.com.lukinhasssss.clients.PixKeyDetailsResponse
import br.com.lukinhasssss.entities.ChavePix
import br.com.lukinhasssss.repositories.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ConsultarChaveEndpointTest {

    @Inject
    lateinit var pixRepository: ChavePixRepository

    @Inject
    lateinit var grpcClient: ConsultarChaveServiceGrpc.ConsultarChaveServiceBlockingStub

    @Inject
    lateinit var bcbClient: BCBClient

    private lateinit var chavePix: ChavePix

    @BeforeEach
    internal fun setUp() {
        pixRepository.deleteAll()
        chavePix = pixRepository.save(ChavePix(
            idCliente = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            tipoChave = TipoChave.CELULAR,
            valorChave = "+5511987654321",
            tipoConta = TipoConta.CONTA_CORRENTE
        ))
    }

    @Test
    internal fun `deve consultar a chave pix quando a requisicao for feita pelo pixId e todos os dados forem validos`() {
        val request = ConsultarChaveRequest.newBuilder().pixIdBuilder
            .setPixId(chavePix.pixId)
            .setIdCliente(chavePix.idCliente)
            .build()

        `when`(bcbClient.buscarChave("+5511987654321")).thenReturn(HttpResponse.ok(pixKeyDetailsResponse))

        val response = grpcClient.consultarChave(ConsultarChaveRequest.newBuilder().setPixId(request).build())

        with(response) {
            assertNotNull(pixId)
            assertNotNull(idCliente)
            assertNotNull(nome)
            assertNotNull(cpf)
            assertNotNull(tipoChave)
            assertNotNull(chave)
            assertNotNull(conta)
            assertNotNull(criadoEm)
        }
    }

    @Test
    internal fun `deve consultar a chave pix quando a requisicao for feita pela chavePix e todos os dados forem validos`() {
        val request = ConsultarChaveRequest.newBuilder()
            .setChavePix(chavePix.valorChave)
            .build()

        `when`(bcbClient.buscarChave(request.chavePix)).thenReturn(HttpResponse.ok(pixKeyDetailsResponse))

        val response = grpcClient.consultarChave(request)

        with(response) {
            assertNotNull(nome)
            assertNotNull(cpf)
            assertNotNull(tipoChave)
            assertNotNull(chave)
            assertNotNull(conta)
            assertNotNull(criadoEm)
        }
    }

    @Test
    internal fun `deve retornar NOT_FOUND quando a chave nao for encontrada no Banco Central`() {
        val request = ConsultarChaveRequest.newBuilder()
            .setChavePix(chavePix.valorChave)
            .build()

        `when`(bcbClient.buscarChave(request.chavePix)).thenReturn(HttpResponse.status(HttpStatus.NOT_FOUND))

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(request)
        }

        with(exception){
            assertEquals("Chave Pix não encontrada!", status.description)
            assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando a requisicao for feita pelo pixId e o pixId for vazio`() {
        val request = ConsultarChaveRequest.newBuilder().pixIdBuilder
            .setPixId("")
            .setIdCliente(chavePix.idCliente)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(ConsultarChaveRequest.newBuilder().setPixId(request).build())
        }

        with(exception){
            assertEquals("PixId é obrigatório!", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando a requisicao for feita pelo pixId e o pixId nao for um UUID valido`() {
        val request = ConsultarChaveRequest.newBuilder().pixIdBuilder
            .setPixId("98dsf9v416fb1vea9fb")
            .setIdCliente(chavePix.idCliente)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(ConsultarChaveRequest.newBuilder().setPixId(request).build())
        }

        with(exception){
            assertEquals("PixId não é um UUID válido!", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando a requisicao for feita pelo pixId e o idCliente for vazio`() {
        val request = ConsultarChaveRequest.newBuilder().pixIdBuilder
            .setPixId(chavePix.pixId)
            .setIdCliente("")
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(ConsultarChaveRequest.newBuilder().setPixId(request).build())
        }

        with(exception){
            assertEquals("IdCliente é obrigatório!", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando a requisicao for feita pelo pixId e o idCliente nao for um UUID valido`() {
        val request = ConsultarChaveRequest.newBuilder().pixIdBuilder
            .setPixId(chavePix.pixId)
            .setIdCliente("64fsv651dsf6vsd")
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(ConsultarChaveRequest.newBuilder().setPixId(request).build())
        }

        with(exception){
            assertEquals("IdCliente não é um UUID válido!", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    internal fun `deve retornar NOT_FOUND quando a requisicao for feita pelo pixId e a chave pix nao for encontrada`() {
        val request = ConsultarChaveRequest.newBuilder().pixIdBuilder
            .setPixId(UUID.randomUUID().toString())
            .setIdCliente(chavePix.idCliente)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(ConsultarChaveRequest.newBuilder().setPixId(request).build())
        }

        with(exception){
            assertEquals("Chave Pix não encontrada!", status.description)
            assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }

    @Test
    internal fun `deve retornar PERMISSION_DENIED quando a requisicao for feita pelo pixId e a chave pix nao pertencer ao cliente`() {
        val request = ConsultarChaveRequest.newBuilder().pixIdBuilder
            .setPixId(chavePix.pixId)
            .setIdCliente(UUID.randomUUID().toString())
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(ConsultarChaveRequest.newBuilder().setPixId(request).build())
        }

        with(exception){
            assertEquals("Chave Pix não pertence ao cliente!", status.description)
            assertEquals(Status.PERMISSION_DENIED.code, status.code)
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando a requisicao for feita pela chave pix e a chavePix for vazia`() {
        val request = ConsultarChaveRequest.newBuilder()
            .setChavePix("")
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(request)
        }

        with(exception){
            assertEquals("Chave Pix é obrigatória!", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando a requisicao for feita pela chave pix tiver mais de 77 caracteres`() {
        val request = ConsultarChaveRequest.newBuilder().pixIdBuilder
            .setPixId("")
            .setIdCliente(chavePix.idCliente)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(ConsultarChaveRequest.newBuilder().setPixId(request).build())
        }

        with(exception){
            assertEquals("PixId é obrigatório!", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Factory
    class Consultar {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ConsultarChaveServiceGrpc.ConsultarChaveServiceBlockingStub {
            return ConsultarChaveServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(BCBClient::class)
    fun bcbMock(): BCBClient {
        return Mockito.mock(BCBClient::class.java)
    }

    private val pixKeyDetailsResponse = PixKeyDetailsResponse(
        keyType = "PHONE",
        key = "+5511987654321",
        bankAccount = BankAccountResponse("60701190", "0001", "987654", "CACC"),
        owner = OwnerResponse("NATURAL_PERSON", "Monkey D. Luffy", "00011122233"),
        createdAt = LocalDateTime.now()
    )

}