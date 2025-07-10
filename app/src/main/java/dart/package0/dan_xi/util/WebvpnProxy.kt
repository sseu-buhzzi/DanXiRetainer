package dart.package0.dan_xi.util

object WebvpnProxy {
	const val DIRECT_CONNECT_TEST_URL = "https://forum.fduhole.com"

	const val WEBVPN_UIS_LOGIN_URL =
		"https://uis.fudan.edu.cn/authserver/login?service=https%3A%2F%2Fid.fudan.edu.cn%2Fidp%2FthirdAuth%2Fcas"
	const val WEBVPN_ID_REQUEST_URL =
		"https://id.fudan.edu.cn/idp/authCenter/authenticate?service=https%3A%2F%2Fwebvpn.fudan.edu.cn%2Flogin%3Fcas_login%3Dtrue"
	const val WEBVPN_LOGIN_URL = "https://webvpn.fudan.edu.cn/login"

	val vpnPrefix = mapOf(
		"www.fduhole.com" to
			"https://webvpn.fudan.edu.cn/https/77726476706e69737468656265737421e7e056d221347d5871048ce29b5a2e",
		"auth.fduhole.com" to
			"https://webvpn.fudan.edu.cn/https/77726476706e69737468656265737421f1e2559469366c45760785a9d6562c38",
		"danke.fduhole.com" to
			"https://webvpn.fudan.edu.cn/https/77726476706e69737468656265737421f4f64f97227e6e546b0086a09d1b203a73",
		"forum.fduhole.com" to
			"https://webvpn.fudan.edu.cn/https/77726476706e69737468656265737421f6f853892a7e6e546b0086a09d1b203a46",
		"image.fduhole.com" to
			"https://webvpn.fudan.edu.cn/https/77726476706e69737468656265737421f9fa409b227e6e546b0086a09d1b203ab8",
		"yjsxk.fudan.edu.cn" to
			"https://webvpn.fudan.edu.cn/http/77726476706e69737468656265737421e9fd52842c7e6e457a0987e29d51367bba7b"
	)
}
