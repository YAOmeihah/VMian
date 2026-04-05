package com.java.vmian.util

import java.security.MessageDigest

/**
 * 加密工具类
 */
object CryptoUtils {
    
    /**
     * 生成MD5签名
     * @param input 输入字符串
     * @return MD5哈希值
     */
    fun generateMd5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
