package com.sukajee.noteapp.database.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "notes")
data class Note(
	@Id val id: ObjectId = ObjectId.get(),
	val ownerId: ObjectId,
	val title: String,
	val content: String,
	val color: Long,
	val createdAt: Instant
)
