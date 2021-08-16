package br.com.lukinhasssss.clients

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${clients.itau}")
interface ItauClient {

    @Get("/clientes/{idCliente}")
    fun consultarCliente(@PathVariable idCliente: String): HttpResponse<DadosDoClienteResponse>

    @Get("/clientes/{idCliente}/contas{?tipo}")
    fun consultarConta(idCliente: String, @QueryValue tipo: String): HttpResponse<DadosDaContaResponse>

}

data class DadosDaContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
)

data class DadosDoClienteResponse(
    val id: String,
    val nome: String,
    val cpf: String,
    val instituicao: InstituicaoResponse
)

data class InstituicaoResponse(
    val nome: String,
    val ispb: String
)

data class TitularResponse(
    val id: String,
    val nome: String,
    val cpf: String
)