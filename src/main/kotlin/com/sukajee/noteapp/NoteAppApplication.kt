package com.sukajee.noteapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NoteAppApplication

fun main(args: Array<String>) {
	runApplication<NoteAppApplication>(*args)
}
