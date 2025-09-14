package com.sukajee.noteapp.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class HashEncoder {
	
	private val bcrypt = BCryptPasswordEncoder()
	
	fun encode(rawString: String): String = bcrypt.encode(rawString)
	
	fun matches(rawString: String, encodedString: String): Boolean = bcrypt.matches(rawString, encodedString)
}