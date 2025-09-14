package com.sukajee.noteapp

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.runApplication

@EnableAutoConfiguration(exclude = [MongoAutoConfiguration::class])
@SpringBootApplication
class NoteAppApplication

fun main(args: Array<String>) {
	runApplication<NoteAppApplication>(*args)
}
