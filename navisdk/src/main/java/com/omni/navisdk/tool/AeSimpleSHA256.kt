package com.omni.navisdk.tool

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object AeSimpleSHA256 {
    private fun convertToHex(data: ByteArray): String {
        val buf = StringBuilder()
        for (b in data) {
            var halfbyte: Int = b.toInt() ushr 4 and 0x0F
            var two_halfs = 0
            do {
                buf.append(if (0 <= halfbyte && halfbyte <= 9) ('0'.toInt() + halfbyte).toChar() else ('a'.toInt() + (halfbyte - 10)).toChar())
                halfbyte = b.toInt() and 0x0F
            } while (two_halfs++ < 1)
        }
        return buf.toString()
    }

    @Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
    fun SHA256(text: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val textBytes = text.toByteArray(charset("iso-8859-1"))
        md.update(textBytes, 0, textBytes.size)
        val sha1hash = md.digest()
        return convertToHex(sha1hash)
    }
}