package br.com.lukinhasssss.validations

import br.com.lukinhasssss.*
import br.com.lukinhasssss.clients.PixKeyDetailsResponse
import br.com.lukinhasssss.repositories.ChavePixRepository
import com.google.protobuf.Descriptors
import com.google.protobuf.Timestamp
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.time.ZoneOffset

fun ConsultarChaveRequest.isValid(
    pixRepository: ChavePixRepository,
    responseObserver: StreamObserver<ConsultarChaveResponse>?
): Boolean {
    if (hasPixId()) {
        if (pixId.pixId.isBlank()) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("PixId é obrigatório!").asRuntimeException())
            return false
        }

        if (pixId.idCliente.isBlank()) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("IdCliente é obrigatório!").asRuntimeException())
            return false
        }

        if (!pixId.pixId.isUUID()) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("PixId não é um UUID válido!").asRuntimeException())
            return false
        }

        if (!pixId.idCliente.isUUID()) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("IdCliente não é um UUID válido!").asRuntimeException())
            return false
        }

        if (!pixRepository.existsByPixId(pixId.pixId)) {
            responseObserver?.onError(Status.NOT_FOUND.withDescription("Chave Pix não encontrada!").asRuntimeException())
            return false
        }

        if (!pixRepository.existsByPixIdAndIdCliente(pixId.pixId, pixId.idCliente)) {
            responseObserver?.onError(Status.PERMISSION_DENIED.withDescription("Chave Pix não pertence ao cliente!").asRuntimeException())
            return false
        }
    }

    if (hasChavePix()) {
        if (chavePix.isBlank()) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("Chave Pix é obrigatória!").asRuntimeException())
            return false
        }

        if (chavePix.length > 77) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("Chave Pix não pode ter mais de 77 caracteres!").asRuntimeException())
            return false
        }
    }

    return true
}

fun PixKeyDetailsResponse.converter(request: ConsultarChaveRequest): ConsultarChaveResponse {
    if (request.chavePix.isNotBlank()) {
        return ConsultarChaveResponse.newBuilder()
            .setNome(owner.name)
            .setCpf(owner.taxIdNumber)
            .setTipoChave(keyType.converterParaTipoChave())
            .setChave(key)
            .setConta(Conta.newBuilder()
                .setInstituicao(Instituicoes.nome(bankAccount.participant))
                .setAgencia(bankAccount.branch)
                .setNumero(bankAccount.accountNumber)
                .setTipoConta(bankAccount.accountType.converterParaTipoConta())
                .build())
            .setCriadoEm(Timestamp.newBuilder()
                .setSeconds(createdAt.toInstant(ZoneOffset.UTC).epochSecond)
                .setNanos(createdAt.toInstant(ZoneOffset.UTC).nano)
                .build())
            .build()
    }

    return ConsultarChaveResponse.newBuilder()
        .setPixId(request.pixId.pixId)
        .setIdCliente(request.pixId.idCliente)
        .setNome(owner.name)
        .setCpf(owner.taxIdNumber)
        .setTipoChave(keyType.converterParaTipoChave())
        .setChave(key)
        .setConta(Conta.newBuilder()
            .setInstituicao(Instituicoes.nome(bankAccount.participant))
            .setAgencia(bankAccount.branch)
            .setNumero(bankAccount.accountNumber)
            .setTipoConta(bankAccount.accountType.converterParaTipoConta())
            .build())
        .setCriadoEm(Timestamp.newBuilder()
            .setSeconds(createdAt.toInstant(ZoneOffset.UTC).epochSecond)
            .setNanos(createdAt.toInstant(ZoneOffset.UTC).nano)
            .build())
        .build()
}

fun String.converterParaTipoChave(): TipoChave {
    if (this == "RANDOM")
        return TipoChave.ALEATORIA

    if (this == "CPF")
        return TipoChave.CPF

    if (this == "EMAIL")
        return TipoChave.EMAIL

    if (this == "PHONE")
        return TipoChave.CELULAR

    return TipoChave.CHAVE_INVALIDA
}

fun String.converterParaTipoConta(): TipoConta {
    if (this == "CACC")
        return TipoConta.CONTA_CORRENTE

    if (this == "SVGS")
        return TipoConta.CONTA_POUPANCA

    return TipoConta.CONTA_INVALIDA
}