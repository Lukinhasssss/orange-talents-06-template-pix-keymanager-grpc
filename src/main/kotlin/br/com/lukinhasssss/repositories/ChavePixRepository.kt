package br.com.lukinhasssss.repositories

import br.com.lukinhasssss.entities.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, String> {

    fun existsByValorChave(valorChave: String): Boolean

}