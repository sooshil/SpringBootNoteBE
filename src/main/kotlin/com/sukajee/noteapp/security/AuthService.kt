package com.sukajee.noteapp.security

import com.sukajee.noteapp.database.model.RefreshToken
import com.sukajee.noteapp.database.model.User
import com.sukajee.noteapp.database.repository.RefreshTokenRepository
import com.sukajee.noteapp.database.repository.UserRepository
import org.bson.types.ObjectId
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.*

/**
 * Service class responsible for handling authentication and token management.
 */
@Service
class AuthService(
	private val jwtService: JwtService,
	private val userRepository: UserRepository,
	private val hashEncoder: HashEncoder,
	private val refreshTokenRepository: RefreshTokenRepository
) {

	/**
	 * Data class to hold a pair of access and refresh tokens.
	 */
	data class TokenPair(
		val accessToken: String,
		val refreshToken: String
	)

	/**
	 * Registers a new user with the provided email and password.
	 *
	 * @param email The email of the new user.
	 * @param password The password of the new user.
	 * @return The registered user.
	 */
	fun register(
		email: String,
		password: String
	): User {
		return userRepository.save(
			User(
				email = email,
				hashedPassword = hashEncoder.encode(password)
			)
		)
	}

	/**
	 * Authenticates a user with the provided email and password.
	 *
	 * @param email The email of the user.
	 * @param password The password of the user.
	 * @return A pair of access and refresh tokens.
	 * @throws BadCredentialsException If the credentials are invalid.
	 */
	fun login(
		email: String,
		password: String
	): TokenPair {
		val user = userRepository.findByEmail(email) ?: throw BadCredentialsException("Invalid credentials.")
		if (!hashEncoder.matches(password, user.hashedPassword)) {
			throw BadCredentialsException("Invalid credentials.")
		}
		val newAccessToken = jwtService.generateAccessToken(user.id.toHexString())
		val newRefreshToken = jwtService.generateRefreshToken(user.id.toHexString())

		storeRefreshToken(
			userId = user.id,
			rawRefreshToken = newRefreshToken
		)

		return TokenPair(
			accessToken = newAccessToken,
			refreshToken = newRefreshToken
		)
	}

	/**
	 * Refreshes the access token using the provided refresh token.
	 *
	 * @param refreshToken The refresh token to be used for refreshing the access token.
	 * @return A new pair of access and refresh tokens.
	 * @throws IllegalArgumentException If the refresh token is invalid or not recognized.
	 */
	@Transactional
	fun refresh(refreshToken: String): TokenPair {
		val exception = IllegalArgumentException("Invalid refresh token.")
		if (!jwtService.validateRefreshToken(refreshToken)) {
			throw exception
		}
		val userId = jwtService.getUserIdFromToken(refreshToken)
		val user = userRepository.findById(ObjectId(userId)).orElseThrow {
			exception
		}
		val hashed = hashToken(refreshToken)
		refreshTokenRepository.findByUserIdAndHashedToken(
			userId = user.id,
			hashedToken = hashed
		) ?: throw IllegalArgumentException("Refresh token not recognized (may be used or expired.")
		refreshTokenRepository.deleteByUserIdAndHashedToken(user.id, refreshToken)

		val newAccessToken = jwtService.generateAccessToken(userId)
		val newRefreshToken = jwtService.generateRefreshToken(userId)

		storeRefreshToken(user.id, newRefreshToken)
		return TokenPair(
			accessToken = newAccessToken,
			refreshToken = newRefreshToken
		)
	}

	/**
	 * Stores a refresh token in the repository after hashing it and setting its expiration time.
	 *
	 * @param userId The ID of the user associated with the refresh token.
	 * @param rawRefreshToken The raw refresh token to be stored.
	 */
	private fun storeRefreshToken(userId: ObjectId, rawRefreshToken: String) {
		val hashed = hashToken(rawRefreshToken)
		val expiryMs = jwtService.refreshTokenValidityMs
		val expiresAt = Instant.now().plusMillis(expiryMs)

		refreshTokenRepository.save(
			RefreshToken(
				userId = userId,
				hashedToken = hashed,
				expiresAt = expiresAt
			)
		)
	}

	/**
	 * Hashes the provided token using SHA-256 and encodes it to Base64.
	 *
	 * @param token The token to be hashed.
	 * @return The hashed and Base64 encoded token.
	 */
	private fun hashToken(token: String): String {
		val digest = MessageDigest.getInstance("SHA-256")
		val hashBytes = digest.digest(token.encodeToByteArray())
		return Base64.getEncoder().encodeToString(hashBytes)
	}
}
