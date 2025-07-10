package dart.package0.dan_xi.repository.forum

enum class PushNotificationServiceType { APNS, MIPUSH;
	fun toStringRepresentation() = when (this) {
		APNS -> "apns"
		MIPUSH -> "mipush"
	}
}

enum class SetStatusMode { ADD, DELETE }

class NotLoginError(
	val errorMessage: String,
) : RuntimeException()

class QuizUnansweredError(
	val errorMessage: String,
) : RuntimeException()

class LoginExpiredError : Exception()

class PushNotificationRegData(
	val deviceId: String,
	val token: String,
	val type: PushNotificationServiceType,
)
