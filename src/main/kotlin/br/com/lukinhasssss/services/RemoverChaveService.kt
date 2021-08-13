package br.com.lukinhasssss.services

import br.com.lukinhasssss.RemoverChaveServiceGrpc
import br.com.lukinhasssss.RemoverChaveRequest
import br.com.lukinhasssss.RemoverChaveResponse
import br.com.lukinhasssss.repositories.ChavePixRepository
import br.com.lukinhasssss.validations.isValid
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class RemoverChaveService(
    private val pixRepository: ChavePixRepository
) : RemoverChaveServiceGrpc.RemoverChaveServiceImplBase() {

    override fun removerChave(request: RemoverChaveRequest?, responseObserver: StreamObserver<RemoverChaveResponse>?) {
        if (request!!.isValid(pixRepository, responseObserver)) {
            pixRepository.deleteById(request.pixId)
            responseObserver?.onNext(RemoverChaveResponse.newBuilder().build())
            responseObserver?.onCompleted()
        }
    }
}