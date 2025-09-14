package com.sukajee.noteapp.controllers

import com.sukajee.noteapp.controllers.NoteController.NoteResponse
import com.sukajee.noteapp.database.model.Note
import com.sukajee.noteapp.database.repository.NoteRepository
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/notes")
class NoteController(
	private val repository: NoteRepository
) {
	
	data class NoteRequest(
		val id: String?,
		val title: String,
		val content: String,
		val color: Long
	)
	
	data class NoteResponse(
		val id: String,
		val title: String,
		val content: String,
		val color: Long,
		val createdAt: Instant
	)
	
	// POST http://localhost:8080/notes
	@PostMapping
	fun save(
		@RequestBody body: NoteRequest
	): NoteResponse {
		val note = repository.save(
			Note(
				id = body.id?.let { ObjectId(it) } ?: ObjectId.get(),
				ownerId = ObjectId.get(),
				title = body.title,
				content = body.content,
				color = body.color,
				createdAt = Instant.now()
			)
		)
		return note.toResponse()
	}
	
	// GET http://localhost:8080/notes?ownerId=5f7d7d5d0d8d0d0001c7d5d0
	@GetMapping
	fun findByOwnerId(
		@RequestParam(required = true) ownerId: String
	): List<NoteResponse> {
		return repository.findByOwnerId(ObjectId(ownerId)).map {
			it.toResponse()
		}
	}
	
	// DELETE http://localhost:8080/notes/5f7d7d5d0d8d0d0001c7d5d0
	@DeleteMapping(path = ["/{id}"])
	fun deleteById(@PathVariable id: String) {
		repository.deleteById(ObjectId(id))
	}
}

private fun Note.toResponse() = NoteResponse(
	id = id.toString(),
	title = title,
	content = content,
	color = color,
	createdAt = createdAt
)