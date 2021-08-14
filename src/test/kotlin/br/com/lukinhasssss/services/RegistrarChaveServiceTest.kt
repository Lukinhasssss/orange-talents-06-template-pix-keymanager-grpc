package br.com.lukinhasssss.services

import br.com.lukinhasssss.*
import br.com.lukinhasssss.clients.ItauClient
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
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RegistrarChaveServiceTest {

    @Inject
    lateinit var pixRepository: ChavePixRepository
    @Inject
    lateinit var grpcClient: RegistrarChaveServiceGrpc.RegistrarChaveServiceBlockingStub

    @field:Inject
    lateinit var itauClient: ItauClient

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

        Mockito.`when`(itauClient.buscarContaPorTipo(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.ok())

        val response = grpcClient.registrarChave(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(pixRepository.findById(pixId).isPresent)
        }
    }

    @Test
    internal fun `deve registrar uma nova chave pix quando a chave for CHAVE_ALEATORIA e o valor for vazio`() {
        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("a61e53c7-c99f-4d85-9974-6be73681b5a9")
            .setTipoChave(TipoChave.CHAVE_ALEATORIA)
            .setValorChave("")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(itauClient.buscarContaPorTipo(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.ok())

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

        Mockito.`when`(itauClient.buscarContaPorTipo(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.ok())

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

        Mockito.`when`(itauClient.buscarContaPorTipo(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.ok())

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

        Mockito.`when`(itauClient.buscarContaPorTipo(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.ok())

        val response = grpcClient.registrarChave(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(pixRepository.findById(pixId).isPresent)
        }
    }

    @Test
    internal fun `deve retornar NOT_FOUND quando o id do cliente nao for encontrado`() {

        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("a61e53c7-c99f-4d85-9974-6be73681b5a9")
            .setTipoChave(TipoChave.CPF)
            .setValorChave("12345678901")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(itauClient.buscarContaPorTipo(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Não foi possível encontrar o cliente com o id informado!", status.description)
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertTrue(pixRepository.findAll().isEmpty())
        }
    }

    @Test
    internal fun `deve retornar INTERNAL quando ocorrer um erro ao fazer a requisicao para o cliente externo`() {
        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CPF)
            .setValorChave("12345678901")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(itauClient.buscarContaPorTipo(request.idCliente, request.tipoConta.name)).thenReturn(HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR))

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Não foi possível fazer a requisição para o cliente externo!", status.description)
            assertEquals(Status.INTERNAL.code, status.code)
            assertTrue(pixRepository.findAll().isEmpty())
        }
    }

    @Test
    internal fun `deve retornar ALREADY_EXISTS quando a chave ja existir`() {
        pixRepository.save(ChavePix(
            idCliente = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            tipoChave = TipoChave.CPF,
            valorChave = "12345678901",
            tipoConta = TipoConta.CONTA_CORRENTE
        ))

        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CPF)
            .setValorChave("12345678901")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(request)
        }

        with(exception){
            assertEquals("Chave já cadastrada!", status.description)
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertTrue(pixRepository.findAll().size == 1)
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando o cliente tentar cadastrar mais de cinco chaves`() {
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
            tipoChave = TipoChave.CHAVE_ALEATORIA,
            valorChave = "",
            tipoConta = TipoConta.CONTA_CORRENTE
        ))

        pixRepository.save(ChavePix(
            idCliente = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            tipoChave = TipoChave.CHAVE_ALEATORIA,
            valorChave = "",
            tipoConta = TipoConta.CONTA_CORRENTE
        ))

        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CHAVE_ALEATORIA)
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
    internal fun `deve retornar INVALID_ARGUMENT quando a chave tiver mais de 77 caracteres`() {
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
    internal fun `deve retornar INVALID_ARGUMENT quando o tipo de chave for CHAVE_INVALIDA`() {
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
    internal fun `deve retornar INVALID_ARGUMENT quando o tipo de conta for invalido`() {
        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CHAVE_ALEATORIA)
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
    internal fun `deve retornar INVALID_ARGUMENT quando o id do cliente for nulo ou nao for um UUID valido`() {
        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2")
            .setTipoChave(TipoChave.CHAVE_ALEATORIA)
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
    internal fun `deve retornar INVALID_ARGUMENT quando o tipo de chave for diferente de ALEATORIA e o valor da chave for vazio`() {
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
    internal fun `deve retornar INVALID_ARGUMENT quando o tipo de chave for ALEATORIA e o valor for preenchido`() {
        val request = RegistrarChaveRequest
            .newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CHAVE_ALEATORIA)
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
    internal fun `deve retornar INVALID_ARGUMENT quando o tipo de chave for CELULAR e o celular nao tiver formato valido`() {
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
    internal fun `deve retornar INVALID_ARGUMENT quando o tipo de chave for CPF e o cpf nao tiver formato valido`() {
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
    internal fun `deve retornar INVALID_ARGUMENT quando o tipo de chave for EMAIL e o email nao tiver formato valido`() {
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

}