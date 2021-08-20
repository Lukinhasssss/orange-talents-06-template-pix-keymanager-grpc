package br.com.lukinhasssss.endpoints

import br.com.lukinhasssss.*
import br.com.lukinhasssss.clients.*
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
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RegistrarChaveEndpointTest {

    @Inject
    lateinit var pixRepository: ChavePixRepository

    @Inject
    lateinit var grpcClient: RegistrarChaveServiceGrpc.RegistrarChaveServiceBlockingStub

    @field:Inject
    lateinit var itauClient: ItauClient

    @field:Inject
    lateinit var bcbClient: BCBClient

    @BeforeEach
    internal fun setUp() {
        pixRepository.deleteAll()
    }

    @Test
    internal fun `deve registrar uma nova chave pix quando todos os dados forem validos`() {
        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("a61e53c7-c99f-4d85-9974-6be73681b5a9")
            .setTipoChave(TipoChave.CPF)
            .setValorChave("12345678901")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauClient.consultarConta(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.ok(dadosDaContaResponse))
        `when`(bcbClient.registrarChave(CreatePixKeyRequest(dadosDaContaResponse, request))).thenReturn(HttpResponse.created(CreatePixKeyResponse("12345678901")))

        val response = grpcClient.registrarChave(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(pixRepository.findById(pixId).isPresent)
        }
    }

    @Test
    internal fun `deve registrar uma nova chave pix quando a chave for ALEATORIA e o valor for vazio`() {
        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("a61e53c7-c99f-4d85-9974-6be73681b5a9")
            .setTipoChave(TipoChave.ALEATORIA)
            .setValorChave("")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauClient.consultarConta(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.ok(dadosDaContaResponse))
        `when`(bcbClient.registrarChave(CreatePixKeyRequest(dadosDaContaResponse, request))).thenReturn(HttpResponse.created(CreatePixKeyResponse("")))

        val response = grpcClient.registrarChave(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(pixRepository.findById(pixId).isPresent)
        }
    }

    @Test
    internal fun `deve registrar uma nova chave pix quando a chave for CELULAR e o valor for valido`() {
        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("a61e53c7-c99f-4d85-9974-6be73681b5a9")
            .setTipoChave(TipoChave.CELULAR)
            .setValorChave("+5511987654321")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauClient.consultarConta(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.ok(dadosDaContaResponse))
        `when`(bcbClient.registrarChave(CreatePixKeyRequest(dadosDaContaResponse, request))).thenReturn(HttpResponse.created(CreatePixKeyResponse("+551198765432")))

        val response = grpcClient.registrarChave(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(pixRepository.findById(pixId).isPresent)
        }
    }

    @Test
    internal fun `deve registrar uma nova chave pix quando a chave for CPF e o valor for valido`() {
        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("a61e53c7-c99f-4d85-9974-6be73681b5a9")
            .setTipoChave(TipoChave.CPF)
            .setValorChave("92678134568")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauClient.consultarConta(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.ok(dadosDaContaResponse))
        `when`(bcbClient.registrarChave(CreatePixKeyRequest(dadosDaContaResponse, request))).thenReturn(HttpResponse.created(CreatePixKeyResponse("92678134568")))

        val response = grpcClient.registrarChave(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(pixRepository.findById(pixId).isPresent)
        }
    }

    @Test
    internal fun `deve registrar uma nova chave pix quando a chave for EMAIL e o valor for valido`() {
        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("a61e53c7-c99f-4d85-9974-6be73681b5a9")
            .setTipoChave(TipoChave.EMAIL)
            .setValorChave("zoro@gmail.com")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauClient.consultarConta(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.ok(dadosDaContaResponse))
        `when`(bcbClient.registrarChave(CreatePixKeyRequest(dadosDaContaResponse, request))).thenReturn(HttpResponse.created(CreatePixKeyResponse("zoro@gmail.com")))

        val response = grpcClient.registrarChave(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(pixRepository.findById(pixId).isPresent)
        }
    }

    @Test
    internal fun `deve retornar NOT_FOUND quando a conta do cliente nao for encontrada e nao deve registrar uma nova chave`() {

        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("a61e53c7-c99f-4d85-9974-6be73681b5a9")
            .setTipoChave(TipoChave.CPF)
            .setValorChave("12345678901")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauClient.consultarConta(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Conta não encontrada!", status.description)
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertTrue(pixRepository.findAll().isEmpty())
        }
    }

    @Test
    internal fun `deve retornar ALREADY_EXISTS quando a chave ja estiver registrada e nao deve registrar uma nova chave`() {
        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CPF)
            .setValorChave("12345678901")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauClient.consultarConta(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.status(HttpStatus.UNPROCESSABLE_ENTITY))

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Chave já registrada!", status.description)
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertTrue(pixRepository.findAll().isEmpty())
        }
    }

    @Test
    internal fun `deve retornar UNKNOWN quando ocorrer um erro ao fazer uma requisicao para o cliente externo do Itau`() {
        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CPF)
            .setValorChave("12345678901")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauClient.consultarConta(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR))

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)!", status.description)
            assertEquals(Status.UNKNOWN.code, status.code)
            assertTrue(pixRepository.findAll().isEmpty())
        }
    }

    @Test
    internal fun `deve retornar UNKNOWN quando ocorrer um erro ao fazer uma requisicao para o cliente externo do Banco Central`() {
        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CPF)
            .setValorChave("12345678901")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauClient.consultarConta(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.ok(dadosDaContaResponse))
        `when`(bcbClient.registrarChave(CreatePixKeyRequest(dadosDaContaResponse, request))).thenReturn(HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR))

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)!", status.description)
            assertEquals(Status.UNKNOWN.code, status.code)
            assertTrue(pixRepository.findAll().isEmpty())
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando o cliente tentar cadastrar mais de cinco chaves e nao deve registrar uma nova chave`() {
        pixRepository.save(ChavePix(
            idCliente = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            tipoChave = TipoChave.CPF,
            valorChave = "12345678901",
            tipoConta = TipoConta.CONTA_CORRENTE
        ))

        pixRepository.save(ChavePix(
            idCliente = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            tipoChave = TipoChave.CELULAR,
            valorChave = "+5511987654321",
            tipoConta = TipoConta.CONTA_CORRENTE
        ))

        pixRepository.save(ChavePix(
            idCliente = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            tipoChave = TipoChave.EMAIL,
            valorChave = "zoro@gmail.com",
            tipoConta = TipoConta.CONTA_CORRENTE
        ))

        pixRepository.save(ChavePix(
            idCliente = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            tipoChave = TipoChave.ALEATORIA,
            valorChave = "",
            tipoConta = TipoConta.CONTA_CORRENTE
        ))

        pixRepository.save(ChavePix(
            idCliente = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            tipoChave = TipoChave.ALEATORIA,
            valorChave = "",
            tipoConta = TipoConta.CONTA_CORRENTE
        ))

        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.ALEATORIA)
            .setValorChave("")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Número máximo de chaves cadastradas!\n" + "Para cadastrar mais chaves remova uma chave existente.", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(pixRepository.findByIdCliente(request.idCliente).size == 5)
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando a chave tiver mais de 77 caracteres e nao deve registrar uma nova chave`() {
        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CPF)
            .setValorChave("a".repeat(78))
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Chave deve ter no máximo 77 caracteres!", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(pixRepository.findAll().isEmpty())
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando o tipo de chave for CHAVE_INVALIDA e nao deve registrar uma nova chave`() {
        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CHAVE_INVALIDA)
            .setValorChave("")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Tipo de chave é inválido!", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(pixRepository.findAll().isEmpty())
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando o tipo de conta for invalido e nao deve registrar uma nova chave`() {
        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.ALEATORIA)
            .setValorChave("")
            .setTipoConta(TipoConta.CONTA_INVALIDA)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Tipo de conta é inválido!", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(pixRepository.findAll().isEmpty())
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando o id do cliente for nulo ou nao for um UUID valido e nao deve registrar uma nova chave`() {
        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2")
            .setTipoChave(TipoChave.ALEATORIA)
            .setValorChave("")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Id do cliente deve ser um UUID válido!", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(pixRepository.findAll().isEmpty())
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando o tipo de chave for diferente de ALEATORIA e o valor da chave for vazio e nao deve registrar uma nova chave`() {
        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CPF)
            .setValorChave("")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Chave deve ser informada!", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(pixRepository.findAll().isEmpty())
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando o tipo de chave for ALEATORIA e o valor for preenchido e nao deve registrar uma nova chave`() {
        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.ALEATORIA)
            .setValorChave("HODOR")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Quando o tipo de chave for aleatória o valor não deve ser preenchido!", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(pixRepository.findAll().isEmpty())
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando o tipo de chave for CELULAR e o celular nao tiver formato valido e nao deve registrar uma nova chave`() {
        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CELULAR)
            .setValorChave("11987654321")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Formato inválido para a chave CELULAR!\n" + "Formato esperado: +5585988714077", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(pixRepository.findAll().isEmpty())
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando o tipo de chave for CPF e o cpf nao tiver formato valido e nao deve registrar uma nova chave`() {
        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CPF)
            .setValorChave("123.456.789-01")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Formato inválido para a chave CPF!\n" + "Formato esperado: 12345678912", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(pixRepository.findAll().isEmpty())
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando o tipo de chave for EMAIL e o email nao tiver formato valido e nao deve registrar uma nova chave`() {
        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.EMAIL)
            .setValorChave("uchiha.madara@konoha")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Formato inválido para a chave EMAIL!\n" + "Exemplo de email válido: email_teste@email.com", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(pixRepository.findAll().isEmpty())
        }
    }

    @Factory
    class Registrar {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RegistrarChaveServiceGrpc.RegistrarChaveServiceBlockingStub {
            return RegistrarChaveServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ItauClient::class)
    fun itauMock(): ItauClient {
        return Mockito.mock(ItauClient::class.java)
    }

    @MockBean(BCBClient::class)
    fun bcbMock(): BCBClient {
        return Mockito.mock(BCBClient::class.java)
    }

    private val dadosDaContaResponse = DadosDaContaResponse(
        "CONTA_CORRENTE",
        InstituicaoResponse("Banco", "651161"),
        "0001",
        "1234567",
        TitularResponse(UUID.randomUUID().toString(), "Monkey D. Luffy", "98745612319")
    )

}