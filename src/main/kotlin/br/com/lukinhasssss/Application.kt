package br.com.lukinhasssss

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.lukinhasssss")
		.start()
}

