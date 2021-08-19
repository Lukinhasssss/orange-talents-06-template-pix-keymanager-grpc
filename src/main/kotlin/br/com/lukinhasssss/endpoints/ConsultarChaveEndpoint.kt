package br.com.lukinhasssss.endpoints

import br.com.lukinhasssss.ConsultarChaveRequest
import br.com.lukinhasssss.ConsultarChaveResponse
import br.com.lukinhasssss.ConsultarChaveServiceGrpc
import br.com.lukinhasssss.clients.BCBClient
import br.com.lukinhasssss.repositories.ChavePixRepository
import br.com.lukinhasssss.validations.converter
import br.com.lukinhasssss.validations.isValid
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Singleton

@Singleton
class ConsultarChaveEndpoint(
    private val pixRepository: ChavePixRepository,
    private val bcbClient: BCBClient
) : ConsultarChaveServiceGrpc.ConsultarChaveServiceImplBase() {

    override fun consultarChave(
        request: ConsultarChaveRequest?,
        responseObserver: StreamObserver<ConsultarChaveResponse>?
    ) {
        if (request!!.isValid(pixRepository, responseObserver)) {
            try {
                if (request.hasPixId()) {
                    val chave = pixRepository.findById(request.pixId.pixId).get().valorChave
                    bcbClient.buscarChave(chave).let { pixKeyDetailsResponse ->
                        if (pixKeyDetailsResponse.status.code != 200)
                            throw HttpClientResponseException("", pixKeyDetailsResponse)

                        responseObserver?.onNext(pixKeyDetailsResponse.body()!!.converter(request))
                        responseObserver?.onCompleted()
                    }
                    return
                }
                if (request.hasChavePix()) {
                    bcbClient.buscarChave(request.chavePix).let { pixKeyDetailsResponse ->
                        if (pixKeyDetailsResponse.status.code != 200)
                            throw HttpClientResponseException("", pixKeyDetailsResponse)

                        responseObserver?.onNext(pixKeyDetailsResponse.body()!!.converter(request))
                        responseObserver?.onCompleted()
                    }
                }
            } catch (e: HttpClientResponseException) {
                if (e.status.code == 404)
                    responseObserver?.onError(
                        Status.NOT_FOUND
                        .withDescription("Chave Pix não encontrada!")
                        .asRuntimeException())

                responseObserver?.onError(
                    Status.UNKNOWN
                    .withDescription("Erro ao consultar chave Pix no Banco Central do Brasil (BCB)!")
                    .asRuntimeException())
            }
        }
    }

}