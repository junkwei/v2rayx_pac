import java.io.File
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

const val gfwListUrl = "https://raw.githubusercontent.com/gfwlist/gfwlist/master/gfwlist.txt"   // https://bitbucket.org/gfwlist/gfwlist/raw/HEAD/gfwlist.txt

val customUrl ="""
    userstyles.org
    unsplash.com
    developer.android.com
    flutter.dev
    docker.com
    twitch.tv
""".trimIndent()

fun main() {
    val url = URL(gfwListUrl)
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.connect()
    val base64Text = String(connection.inputStream.readBytes()).replace("\n", "")
    val lines = String(Base64.getDecoder().decode(base64Text)).split("\n")
    val list = arrayListOf<String>()
    // handle customUrl
    for (curl in customUrl.split("\n")) {
        list.add(curl)
    }
    for (line in lines) {
        if (line.startsWith("|")) {
            list.add(line.replace("|", ""))
        }else if (line.startsWith(".")) {
            list.add(line.substring(1))
        }
    }
    val sb = StringBuilder()
    for (i in list.indices) {
        if (i == list.size - 1) {
            sb.append("  \"${list.elementAt(i)}\"")
        } else {
            sb.append("  \"${list.elementAt(i)}\",\n")
        }
    }

    val pacJsContent = """// update: ${Date()}

var V2Ray = "SOCKS5 127.0.0.1:1081; SOCKS 127.0.0.1:1081; DIRECT;";

var domains = [
$sb
];

function FindProxyForURL(url, host) {
    for (var i = domains.length - 1; i >= 0; i--) {
      if (dnsDomainIs(host, domains[i])) {
            return V2Ray;
      }
    }
    return "DIRECT";
}
    """

    val pacJsFile = File("pac.js")
    pacJsFile.writeText(pacJsContent)
}