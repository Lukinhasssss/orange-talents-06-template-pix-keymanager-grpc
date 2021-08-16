package br.com.lukinhasssss.services

import br.com.lukinhasssss.RegistrarChaveServiceGrpc
import br.com.lukinhasssss.RegistrarChaveRequest
import br.com.lukinhasssss.RegistrarChaveResponse
import br.com.lukinhasssss.clients.BCBClient
import br.com.lukinhasssss.clients.CreatePixKeyRequest
import br.com.lukinhasssss.clients.ItauClient
import br.com.lukinhasssss.repositories.ChavePixRepository
import br.com.lukinhasssss.validations.converterParaChavePix
import br.com.lukinhasssss.validations.isValid
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class RegistrarChaveService(
    private val pixRepository: ChavePixRepository,
    private val itauClient: ItauClient,
    private val bcbClient: BCBClient
) : RegistrarChaveServiceGrpc.RegistrarChaveServiceImplBase() {

    private val logger = LoggerFactory.getLogger(RegistrarChaveService::class.java)

    override fun registrarChave(
        request: RegistrarChaveRequest?,
        responseObserver: StreamObserver<RegistrarChaveResponse>?
    ) {

        if (request!!.isValid(pixRepository, responseObserver)) {
            try {
                itauClient.consultarConta(request.idCliente, request.tipoConta.name).let { response ->
                    if (response.status.code == 404 || response.status.code == 500)
                        throw HttpClientResponseException("", response)

                    bcbClient.registrarChave(CreatePixKeyRequest(response.body()!!, request))
                    pixRepository.save(request.converterParaChavePix()).let { chavePix ->
                        responseObserver?.onNext(RegistrarChaveResponse.newBuilder().setPixId(chavePix.pixId).build())
                        responseObserver?.onCompleted()
                    }
                }
            } catch (e: HttpClientResponseException) {
                if (e.status.code == 404)
                    responseObserver?.onError(Status.NOT_FOUND
                        .withDescription("Cliente não encontrado!")
                        .asRuntimeException())

                if (e.status.code == 500)
                    responseObserver?.onError(Status.INTERNAL
                        .withDescription("Não foi possível processar a solicitação!")
                        .asRuntimeException())

                responseObserver?.onCompleted()
            }
        }

    }
}
