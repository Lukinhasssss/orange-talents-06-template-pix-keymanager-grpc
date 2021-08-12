package br.com.lukinhasssss.controllers

import br.com.lukinhasssss.RegistrarChaveRequest
import br.com.lukinhasssss.RegistrarChaveResponse
import br.com.lukinhasssss.RegistrarChaveServiceGrpc
import br.com.lukinhasssss.clients.ItauClient
import br.com.lukinhasssss.entities.ChavePix
import br.com.lukinhasssss.repositories.ChavePixRepository
import br.com.lukinhasssss.validations.isValid
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

@Singleton
class RegistrarChaveController(
    private val pixRepository: ChavePixRepository,
    private val itauClient: ItauClient
) : RegistrarChaveServiceGrpc.RegistrarChaveServiceImplBase() {

    private val logger = LoggerFactory.getLogger(RegistrarChaveController::class.java)

    override fun registrarChave(request: RegistrarChaveRequest?,
        responseObserver: StreamObserver<RegistrarChaveResponse>?
    ) {

        if (request!!.isValid(pixRepository, responseObserver)) {
            try {
                itauClient.buscarContaPorTipo(request.idCliente, request.tipoConta.name).let { response ->
                    if (response.status.code == 404)
                        throw HttpClientResponseException("Não foi possível encontrar o cliente com o id informado!", response)
                }

                pixRepository.save(ChavePix(
                    idCliente = UUID.fromString(request.idCliente),
                    tipoChave = request.tipoChave,
                    valorChave = request.valorChave.ifBlank { UUID.randomUUID().toString() },
                    tipoConta = request.tipoConta
                )).let { chavePix ->
                    responseObserver?.onNext(RegistrarChaveResponse.newBuilder().setPixId(chavePix.pixId).build())
                    responseObserver?.onCompleted()
                }
            } catch (e: HttpClientResponseException) {
                logger.info("Ocorreu um erro ao fazer a requisição no client externo: {}", e.status.code)
                if (e.status.code == 404)
                    responseObserver?.onError(Status.NOT_FOUND
                        .withDescription(e.localizedMessage)
                        .asRuntimeException())
                if (e.status.code == 500)
                responseObserver?.onError(Status.INTERNAL
                    .withDescription("Não foi possível fazer a requisição para o cliente externo!")
                    .asRuntimeException())
                responseObserver?.onCompleted()
            }
        }

    }

}
