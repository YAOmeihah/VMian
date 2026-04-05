package com.java.vmian.data.remote

object SecureEndpointBuilder {
    fun build(host: String, path: String): String {
        val normalizedHost = if (host.startsWith("http://") || host.startsWith("https://")) {
            host.trimEnd('/')
        } else {
            "http://${host.trimEnd('/')}"
        }
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return normalizedHost + normalizedPath
    }
}
