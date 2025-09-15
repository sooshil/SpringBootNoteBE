package com.sukajee.noteapp.database.model

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "refresh_tokens")
data class RefreshToken(
	val userId: ObjectId,
	val hashedToken: String,
	@Indexed(expireAfter = "0s") val expiresAt: Instant,
	val createdAt: Instant = Instant.now()
)
