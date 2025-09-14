package com.sukajee.noteapp.database.repository

import com.sukajee.noteapp.database.model.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository: MongoRepository<User, ObjectId> {
	fun findByEmail(email: String): User?
}