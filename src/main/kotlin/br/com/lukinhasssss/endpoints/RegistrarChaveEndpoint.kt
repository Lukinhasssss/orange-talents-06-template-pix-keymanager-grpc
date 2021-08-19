package br.com.lukinhasssss.endpoints

import br.com.lukinhasssss.RegistrarChaveRequest
import br.com.lukinhasssss.RegistrarChaveResponse
import br.com.lukinhasssss.RegistrarChaveServiceGrpc
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
class RegistrarChaveEndpoint(
    private val pixRepository: ChavePixRepository,
    private val itauClient: ItauClient,
    private val bcbClient: BCBClient
) : RegistrarChaveServiceGrpc.RegistrarChaveServiceImplBase() {

    private val logger = LoggerFactory.getLogger(RegistrarChaveEndpoint::class.java)

    override fun registrarChave(
        request: RegistrarChaveRequest?,
        responseObserver: StreamObserver<RegistrarChaveResponse>?
    ) {

        if (request != null && request.isValid(pixRepository, responseObserver)) {
            try {
                itauClient.consultarConta(request.idCliente, request.tipoConta.name).let { response ->
                    if (response.status.code != 200)
                        throw HttpClientResponseException("", response)

                    bcbClient.registrarChave(CreatePixKeyRequest(response.body()!!, request)).let { bcbResponse ->
                        if (bcbResponse.status.code != 201)
                            throw HttpClientResponseException("", bcbResponse)

                        pixRepository.save(request.converterParaChavePix(bcbResponse.body()!!)).let { chavePix ->
                            responseObserver?.onNext(RegistrarChaveResponse.newBuilder().setPixId(chavePix.pixId).build())
                            responseObserver?.onCompleted()
                        }
                    }
                }
            } catch (e: HttpClientResponseException) {
                if (e.status.code == 404)
                    responseObserver?.onError(Status.NOT_FOUND
                        .withDescription("Conta não encontrada!")
                        .asRuntimeException())

                if (e.status.code == 422)
                    responseObserver?.onError(Status.ALREADY_EXISTS
                        .withDescription("Chave já registrada!")
                        .asRuntimeException())

                if (e.status.code == 500)
                    responseObserver?.onError(Status.UNKNOWN
                        .withDescription("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)!")
                        .asRuntimeException())
            }
        }

    }
}
