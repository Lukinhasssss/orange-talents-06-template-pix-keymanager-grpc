package br.com.lukinhasssss.validations

import br.com.lukinhasssss.RegistrarChaveRequest
import br.com.lukinhasssss.RegistrarChaveResponse
import br.com.lukinhasssss.TipoChave
import br.com.lukinhasssss.TipoConta
import br.com.lukinhasssss.entities.ChavePix
import br.com.lukinhasssss.repositories.ChavePixRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import sun.jvm.hotspot.oops.CellTypeState.value
import java.util.*

fun RegistrarChaveRequest.converterParaChavePix(): ChavePix = ChavePix(
    idCliente = UUID.fromString(this.idCliente),
    tipoChave = this.tipoChave,
    valorChave = this.valorChave.ifBlank { UUID.randomUUID().toString() },
    tipoConta = this.tipoConta
)

fun RegistrarChaveRequest.isValid(
    pixRepository: ChavePixRepository,
    responseObserver: StreamObserver<RegistrarChaveResponse>?
): Boolean {
    with(this) {
        if (pixRepository.existsByValorChave(valorChave)) {
            responseObserver?.onError(Status.ALREADY_EXISTS.withDescription("Chave já cadastrada!").asRuntimeException())
            return false
        }

        if (valorChave.length > 77) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("Chave deve ter no máximo 77 caracteres!").asRuntimeException())
            return false
        }

        if (tipoConta == null) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("Tipo de conta é obrigatório!").asRuntimeException())
            return false
        }

        if (tipoConta == TipoConta.CONTA_INVALIDA) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("Tipo de conta é inválido!").asRuntimeException())
            return false
        }

        if (idCliente.isBlank() || !idCliente.isUUID()) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("Id do cliente é obrigatório!").asRuntimeException())
            return false
        }

        if (tipoChave != TipoChave.CHAVE_ALEATORIA && valorChave.isBlank()) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("Chave deve ser informada!").asRuntimeException())
            return false
        }

        if (tipoChave == TipoChave.CHAVE_ALEATORIA && valorChave.isNotBlank()) {
            responseObserver?.onError(Status.INVALID_ARGUMENT
                .withDescription("Quando o tipo de chave for aleatória o valor não deve ser preenchido!")
                .asRuntimeException())
            return false
        }

        if (tipoChave == TipoChave.CELULAR && !valorChave.isCelularValid()) {
            responseObserver?.onError(Status.INVALID_ARGUMENT
                .withDescription("Formato inválido para a chave CELULAR!")
                .augmentDescription("Formato esperado: +5585988714077")
                .asRuntimeException())
            return false
        }

        if (tipoChave == TipoChave.CPF && !valorChave.isCpfValid()) {
            responseObserver?.onError(Status.INVALID_ARGUMENT
                .withDescription("Formato inválido para a chave CPF!")
                .augmentDescription("Formato esperado: 12345678912")
                .asRuntimeException())
            return false
        }

        if (tipoChave == TipoChave.EMAIL && !valorChave.isEmailValid()) {
            responseObserver?.onError(Status.INVALID_ARGUMENT
                .withDescription("Formato inválido para a chave EMAIL!")
                .augmentDescription("Exemplo de email válido: email_teste@email.com")
                .asRuntimeException())
            return false
        }
    }
    return true
}

fun String.isEmailValid(): Boolean {
    return this.matches("[a-zA-Z0-9+._%-+]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+".toRegex())
}

fun String.isCelularValid(): Boolean {
    return this.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
}

fun String.isCpfValid(): Boolean {
    return this.matches("^[0-9]{11}\$".toRegex())
}

fun String.isUUID(): Boolean {
    return try {
        UUID.fromString(this)
        true
    } catch (ex: Exception) {
        false
    }
    // Outra forma de validar se é um UUID válido -> this.matches("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})".toRegex())
}