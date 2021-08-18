package br.com.lukinhasssss.services

import br.com.lukinhasssss.ConsultarChaveRequest
import br.com.lukinhasssss.ConsultarChaveResponse
import br.com.lukinhasssss.ConsultarChaveServiceGrpc
import br.com.lukinhasssss.clients.BCBClient
import br.com.lukinhasssss.repositories.ChavePixRepository
import br.com.lukinhasssss.validations.converter
import br.com.lukinhasssss.validations.isValid
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class ConsultarChaveService(
    private val pixRepository: ChavePixRepository,
    private val bcbClient: BCBClient
) : ConsultarChaveServiceGrpc.ConsultarChaveServiceImplBase() {

    override fun consultarChave(
        request: ConsultarChaveRequest?,
        responseObserver: StreamObserver<ConsultarChaveResponse>?
    ) {
        if (request!!.isValid(pixRepository, responseObserver)) {
            try {
                if (request.chavePix.isBlank()) {
                    val chave = pixRepository.findById(request.pixId.pixId).get().valorChave
                    bcbClient.buscarChave(chave).let { pixKeyDetailsResponse ->
                        responseObserver?.onNext(pixKeyDetailsResponse.body()!!.converter(request))
                        responseObserver?.onCompleted()
                    }
                    return
                }
                if (request.pixId.isInitialized) {
                    bcbClient.buscarChave(request.chavePix).let { pixKeyDetailsResponse ->
                        responseObserver?.onNext(pixKeyDetailsResponse.body()!!.converter(request))
                        responseObserver?.onCompleted()
                    }
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }

}