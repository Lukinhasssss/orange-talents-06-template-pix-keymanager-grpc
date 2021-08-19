package br.com.lukinhasssss.validations

import br.com.lukinhasssss.RegistrarChaveRequest
import br.com.lukinhasssss.RegistrarChaveResponse
import br.com.lukinhasssss.TipoChave
import br.com.lukinhasssss.TipoConta
import br.com.lukinhasssss.clients.CreatePixKeyResponse
import br.com.lukinhasssss.entities.ChavePix
import br.com.lukinhasssss.repositories.ChavePixRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver

fun RegistrarChaveRequest.converterParaChavePix(createPixKeyResponse: CreatePixKeyResponse): ChavePix {
    return ChavePix(
        idCliente = idCliente,
        tipoChave = tipoChave,
        valorChave = createPixKeyResponse.key,
        tipoConta = tipoConta
    )
}

fun RegistrarChaveRequest.isValid(
    pixRepository: ChavePixRepository,
    responseObserver: StreamObserver<RegistrarChaveResponse>?
): Boolean {
        if (tipoChave == TipoChave.CHAVE_INVALIDA) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("Tipo de chave é inválido!").asRuntimeException())
            return false
        }

        if (tipoConta == TipoConta.CONTA_INVALIDA) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("Tipo de conta é inválido!").asRuntimeException())
            return false
        }

        if (pixRepository.findByIdCliente(idCliente).size == 5) {
            responseObserver?.onError(Status.INVALID_ARGUMENT
                .withDescription("Número máximo de chaves cadastradas!")
                .augmentDescription("Para cadastrar mais chaves remova uma chave existente.")
                .asRuntimeException())
            return false
        }

        if (valorChave.length > 77) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("Chave deve ter no máximo 77 caracteres!").asRuntimeException())
            return false
        }

        if (idCliente.isBlank() || !idCliente.isUUID()) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("Id do cliente deve ser um UUID válido!").asRuntimeException())
            return false
        }

        if (tipoChave != TipoChave.ALEATORIA && valorChave.isBlank()) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("Chave deve ser informada!").asRuntimeException())
            return false
        }

        if (tipoChave == TipoChave.ALEATORIA && valorChave.isNotBlank()) {
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
    return this.matches("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})".toRegex())

//    return try {
//        UUID.fromString(this)
//        true
//    } catch (ex: Exception) {
//        false
//    }
}