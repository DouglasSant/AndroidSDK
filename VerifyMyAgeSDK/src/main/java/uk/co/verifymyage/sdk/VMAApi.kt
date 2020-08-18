package uk.co.verifymyage.sdk

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Base64



class VMAApi(apiId: String, apiKey: String, apiSecret: String) {
    private var url :String = "https://api-dot-verifymyage.appspot.com"
    private var apiId : String = apiId
    private var apiKey : String = apiKey
    private var apiSecret : String = apiSecret

    private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF // Here is the conversion
            hexChars[j * 2] = HEX_ARRAY[v.ushr(4)]
            hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
        }

        return String(hexChars)
    }

    private fun hmac(input: String, key: String): String{
        val sha256Hmac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
        sha256Hmac.init(secretKey)

        return bytesToHex(sha256Hmac.doFinal(input.toByteArray())).toLowerCase()
    }

    private fun hmacHeader(input: String): String{
        val time = System.currentTimeMillis() / 1000
        var hmac = hmac("$apiKey$time$input", apiSecret)
        return "$apiId:$time:$hmac"
    }

    fun verifications(customer: VmaCustomer): JSONObject {
        val response = StringBuilder()
        val url = URL("$url/verifications")
        val postData: ByteArray = customer.toJsonString().toByteArray()

        with(url.openConnection() as HttpURLConnection){
            requestMethod = "POST"
            connectTimeout = 300000
            connectTimeout = 300000
            doOutput = true

            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", hmacHeader(customer.id))
            try {
                val outputStream = DataOutputStream(outputStream)
                outputStream.write(postData)
                outputStream.flush()
            } catch (exception: Exception) {
                println("Exception while posting data ${exception.message}")
            }
            println("Response code: $responseCode")

            var inputStream = errorStream
            if (errorStream == null) {
                inputStream = getInputStream()
            }

            try{
                BufferedReader(
                    InputStreamReader(inputStream, "utf-8")
                ).use { br ->
                    var responseLine: String? = null
                    while (br.readLine().also { responseLine = it } != null) {
                        response.append(responseLine!!.trim { it <= ' ' })
                    }

                }
                println("============= RESPONSE FROM [POST] $url ===================")
                println(response.toString())
                println("===============================================================\n")
            } catch (exception: Exception) {
                println("Exception while reading data ${exception.message}")

            }
        }

        return JSONObject(response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun faceUpdate(hash: String, bitmap: Bitmap) {
        val response = StringBuilder()
        val url = URL("$url/verifications/$hash/face")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageString: String = Base64.getEncoder().encodeToString(baos.toByteArray())

        val postData = "{ \"image\": \"$imageString\" }".toByteArray()

        with(url.openConnection() as HttpURLConnection){
            requestMethod = "PUT"
            connectTimeout = 300000
            doOutput = true

            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", hmacHeader(hash))
            try {
                val outputStream = DataOutputStream(outputStream)
                outputStream.write(postData)
                outputStream.flush()
            } catch (exception: Exception) {
                println("Exception while posting data ${exception.message}")
            }
            println("Response code: $responseCode")

            var inputStream = errorStream
            if (errorStream == null) {
                inputStream = getInputStream()
            }

            try{
                BufferedReader(
                    InputStreamReader(inputStream, "utf-8")
                ).use { br ->
                    var responseLine: String? = null
                    while (br.readLine().also { responseLine = it } != null) {
                        response.append(responseLine!!.trim { it <= ' ' })
                    }

                }
                println("============= RESPONSE FROM [PUT] $url ===================")
                println(response.toString())
                println("===============================================================\n")
            } catch (exception: Exception) {
                println("Exception while reading data ${exception.message}")

            }
        }
    }

    fun getBase64FromFile(path: String?): ByteArray {
        var bmp: Bitmap? = null
        var baos: ByteArrayOutputStream? = null
        var byteArr = ByteArray(8192)
        try {
            bmp = BitmapFactory.decodeFile(path)
            baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            byteArr = baos.toByteArray()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return byteArr
    }

}