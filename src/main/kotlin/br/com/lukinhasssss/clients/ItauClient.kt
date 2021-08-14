package br.com.lukinhasssss.clients

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${clients.itau}")
interface ItauClient {

//    @Get("/clientes/{idCliente}")
//    fun verificarSeClienteExiste(@PathVariable idCliente: String): Unit

    @Get("/clientes/{idCliente}/contas{?tipo}")
    fun buscarContaPorTipo(idCliente: String, @QueryValue tipo: String): HttpResponse<Unit>

}

//data class DadosDaContaResponse(
//    val tipo: String,
//    val instituicao: Map<String, String>,
//    val agencia: String,
//    val numero: String,
//    val titular: Map<String, String>
//)