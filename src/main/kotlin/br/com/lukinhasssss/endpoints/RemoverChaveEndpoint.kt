package br.com.lukinhasssss.endpoints

import br.com.lukinhasssss.RemoverChaveServiceGrpc
import br.com.lukinhasssss.RemoverChaveRequest
import br.com.lukinhasssss.RemoverChaveResponse
import br.com.lukinhasssss.clients.BCBClient
import br.com.lukinhasssss.clients.DeletePixKeyRequest
import br.com.lukinhasssss.repositories.ChavePixRepository
import br.com.lukinhasssss.validations.isValid
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Singleton

@Singleton
class RemoverChaveEndpoint(
    private val pixRepository: ChavePixRepository,
    private val bcbClient: BCBClient
) : RemoverChaveServiceGrpc.RemoverChaveServiceImplBase() {

    override fun removerChave(request: RemoverChaveRequest?, responseObserver: StreamObserver<RemoverChaveResponse>?) {
        if (request!!.isValid(pixRepository, responseObserver)) {
            try {
                val chavePix = pixRepository.findById(request.pixId).get()

                bcbClient.removerChave(chavePix.valorChave, DeletePixKeyRequest(chavePix.valorChave)).let { response ->
                    if (response.status.code != 200)
                        throw HttpClientResponseException("", response)

                    pixRepository.deleteById(request.pixId)
                    responseObserver?.onNext(RemoverChaveResponse.newBuilder().build())
                    responseObserver?.onCompleted()
                }
            } catch (e: HttpClientResponseException) {
                if (e.status.code == 403)
                    responseObserver?.onError(Status.PERMISSION_DENIED
                        .withDescription("Chave Pix não pertence ao cliente!")
                        .asRuntimeException())

                if (e.status.code == 404)
                    responseObserver?.onError(Status.NOT_FOUND
                        .withDescription("Chave Pix não encontrada!")
                        .asRuntimeException())

                responseObserver?.onError(Status.UNKNOWN
                    .withDescription("Erro ao remover chave Pix no Banco Central do Brasil (BCB)!")
                    .asRuntimeException())
            }
        }
    }
}