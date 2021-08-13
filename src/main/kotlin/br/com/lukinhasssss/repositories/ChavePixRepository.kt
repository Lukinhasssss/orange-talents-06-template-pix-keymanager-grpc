package br.com.lukinhasssss.repositories

import br.com.lukinhasssss.TipoChave
import br.com.lukinhasssss.entities.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, String> {

    fun existsByTipoChaveAndValorChave(tipoChave: TipoChave, valorChave: String): Boolean

    fun existsByPixIdAndIdCliente(pixId: String, idCliente: String): Boolean

    fun existsByPixId(pixId: String): Boolean

}