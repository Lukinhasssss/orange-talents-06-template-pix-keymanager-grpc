package br.com.lukinhasssss.validations

import br.com.lukinhasssss.RemoverChaveRequest
import br.com.lukinhasssss.RemoverChaveResponse
import br.com.lukinhasssss.repositories.ChavePixRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver

fun RemoverChaveRequest.isValid(
    pixRepository: ChavePixRepository,
    responseObserver: StreamObserver<RemoverChaveResponse>?
): Boolean {
    with(this) {
        pixId.ifBlank {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("Campo obrigatório!").asRuntimeException())
            return false
        }

        idCliente.ifBlank {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription("Campo obrigatório!").asRuntimeException())
            return false
        }

        if (!pixRepository.existsByPixId(pixId)) {
            responseObserver?.onError(Status.NOT_FOUND.withDescription("Chave não encontrada!").asRuntimeException())
            return false
        }

        if (!pixRepository.existsByPixIdAndIdCliente(pixId, idCliente)) {
            responseObserver?.onError(Status.PERMISSION_DENIED.withDescription("Chave só pode ser removida pelo seu próprio dono!").asRuntimeException())
            return false
        }
        return true
    }
}