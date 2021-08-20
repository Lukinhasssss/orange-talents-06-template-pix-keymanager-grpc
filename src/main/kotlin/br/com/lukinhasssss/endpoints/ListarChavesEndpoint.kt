package br.com.lukinhasssss.endpoints

import br.com.lukinhasssss.ListarChavesRequest
import br.com.lukinhasssss.ListarChavesResponse
import br.com.lukinhasssss.ListarChavesServiceGrpc
import br.com.lukinhasssss.repositories.ChavePixRepository
import br.com.lukinhasssss.validations.isUUID
import com.google.protobuf.Timestamp
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.time.ZoneOffset
import javax.inject.Singleton

@Singleton
class ListarChavesEndpoint(
    private val pixRepository: ChavePixRepository
) : ListarChavesServiceGrpc.ListarChavesServiceImplBase() {

    override fun listarChaves(request: ListarChavesRequest, responseObserver: StreamObserver<ListarChavesResponse>) {

        if (!request.idCliente.isUUID()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("IdCliente não é um UUID válido!").asRuntimeException())
            return
        }

        val chaves = pixRepository.findByIdCliente(request.idCliente).map { chave ->
            ListarChavesResponse.ChavesPix.newBuilder()
                .setPixId(chave.pixId)
                .setIdCliente(chave.idCliente)
                .setTipoChave(chave.tipoChave)
                .setValorChave(chave.valorChave)
                .setTipoConta(chave.tipoConta)
                .setCriadoEm(
                    Timestamp.newBuilder()
                    .setSeconds(chave.criadaEm.toInstant(ZoneOffset.UTC).epochSecond)
                    .setNanos(chave.criadaEm.toInstant(ZoneOffset.UTC).nano)
                    .build())
                .build()
        }

        responseObserver.onNext(ListarChavesResponse.newBuilder().addAllChaves(chaves).build())
        responseObserver.onCompleted()

    }

}