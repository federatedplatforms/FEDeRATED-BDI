package nl.tno.federated.services

import java.net.HttpURLConnection
import java.net.URL

object GraphDBService {
    fun validateData(): Int {
        val url = URL("http://examplaoeuaoueaaoeue.com")
        val con: HttpURLConnection = url.openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        con.connectTimeout = 1000;
        con.readTimeout = 1000;
        return con.responseCode
    }
}