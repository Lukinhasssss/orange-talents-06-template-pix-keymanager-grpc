package br.com.lukinhasssss.clients

import br.com.lukinhasssss.RegistrarChaveRequest
import br.com.lukinhasssss.TipoChave
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${clients.bcb}")
interface BCBClient {

    @Get("/{key}")
    fun buscarChavePorId(@PathVariable key: String): HttpResponse<Unit>

    @Post
    @Produces(MediaType.APPLICATION_XML)
    fun registrarChave(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete("/{key}")
    @Produces(MediaType.APPLICATION_XML)
    fun removerChave(@PathVariable key: String, @Body request: DeletePixKeyRequest): HttpResponse<DeletePixKeyResponse>

}

data class CreatePixKeyResponse(
    val key: String
)

data class CreatePixKeyRequest(
    val keyType: String,
    val key: String,
    var bankAccount: BankAccountRequest,
    val owner: OwnerRequest
) {
    constructor(dadosDaConta: DadosDaContaResponse, request: RegistrarChaveRequest) : this(
        keyType = KeyType.converter(request.tipoChave),
        key = request.valorChave,
        bankAccount = BankAccountRequest(dadosDaConta),
        owner = OwnerRequest(dadosDaConta)
    )
}

data class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: String
)

data class DeletePixKeyRequest(
    val key: String,
    val participant: String = "60701190"
)

data class BankAccountRequest(val dadosDaConta: DadosDaContaResponse) {
    val participant = dadosDaConta.instituicao.ispb
    val branch = dadosDaConta.agencia
    val accountNumber = dadosDaConta.numero
    val accountType = if (dadosDaConta.tipo == "CONTA_CORRENTE") "CACC" else "SVGS"
}

data class OwnerRequest(val dadosDaConta: DadosDaContaResponse) {
    val type = "LEGAL_PERSON"
    val name = dadosDaConta.titular.nome
    val taxIdNumber = dadosDaConta.titular.cpf
}

enum class KeyType() {
    PHONE, CPF, EMAIL, RANDOM;

    companion object {
        fun converter(tipoChave: TipoChave): String {
            val map = HashMap<TipoChave, KeyType>()
            map[TipoChave.CELULAR] = PHONE
            map[TipoChave.CPF] = CPF
            map[TipoChave.EMAIL] = EMAIL
            map[TipoChave.ALEATORIA] = RANDOM

            return map[tipoChave]!!.name
        }
    }
}