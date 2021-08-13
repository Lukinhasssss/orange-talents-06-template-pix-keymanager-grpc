package br.com.lukinhasssss.entities

import br.com.lukinhasssss.TipoChave
import br.com.lukinhasssss.TipoConta
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
data class ChavePix(

    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    val pixId: String? = null,

    @Column(nullable = false, length = 36)
    val idCliente: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoChave,

    @Column(nullable = false, length = 77)
    val valorChave: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoConta: TipoConta,

    val criadaEm: LocalDateTime = LocalDateTime.now()

)
