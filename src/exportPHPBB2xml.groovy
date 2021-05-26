/*
 * Cilem cviceni je vytahnout vsechno z fora a pripravit data pro Hugo, 
 * ktery nasledne vygeneruje staticky archiv fora (neb uz ho asi nepotrebujeme udrzovat)

 * Abych nemusel resit PHPBB formatovani, tak jsem si upravil PHPBB sablonu tak aby pro kazde vlakno  
 * generovala xml i se posty ktere jsou vygenerovany v html jako CDATA. posty pak pomoci pandoc 
 * zkonvertuju do markdown
 * 
 * Aby to fungovalo spravne musim se prihlasit do fora. !
 */
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7')

import groovy.sql.Sql
import net.czela.common.Helper

import java.nio.charset.StandardCharsets

import static net.czela.common.Helper.fmt6
import static net.czela.common.Helper.nvl


String cookie = login()
Sql sql = Helper.newSqlInstance("phpbb.properties", this)

int cnt = 0;
sql.eachRow("SELECT topic_id, forum_id, min(post_id) post_id FROM phpbb_posts group by topic_id, forum_id".toString()) { row ->
    try {
        String url = "https://www.czela.net/f/viewtopicz.php?f=${row.FORUM_ID}&t=${row.TOPIC_ID}&p=${row.POST_ID}"
        def data = doGet(url, cookie)

        def dir = new File("contentB/forum_${fmt6(row.FORUM_ID)}")
        if (!dir.exists()) dir.mkdirs()
        def file = new File(dir, "topic_${fmt6(row.TOPIC_ID)}.xml")
        if (!file.exists()) file.delete()
        file << data
    } catch (Exception e) {
        println(e.getMessage())
    }
    cnt++
    if (cnt % 10 == 0) {
        print((cnt % 100 ==0)?"+":".")
    }
}

static String doGet (def url, def cookie) {
    def conn = new URL(url).openConnection()
    conn.setRequestProperty('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8')
    if (cookie != null) {
        conn.setRequestProperty('Cookie', cookie)
    }
    return conn.getInputStream().getText()
}

static Object login() {
    CookieManager cookieManager = new CookieManager();
    CookieHandler.setDefault(cookieManager);
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);

    String url = 'https://www.czela.net/f/ucp.php?mode=login'
    String user = nvl(System.getenv('FORUM_USER'), 'anonymous')
    String passwd = nvl(System.getenv('FORUM_PASSWD'), 'unknown')

    HttpURLConnection conn = new URL(url).openConnection()
    conn.setRequestMethod("GET")
    conn.setRequestProperty('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8')
    conn.setDoOutput(true)
    def t = conn.getInputStream().getText()
    def m = t =~ /<input type="hidden" name="sid" value="([^"]+)"/
    def sid = m[0][1]

    CookieStore cookieStore = cookieManager.getCookieStore();

    conn = new URL(url).openConnection()
    conn.setRequestMethod("POST")
    conn.setRequestProperty('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8')
    conn.setRequestProperty('Content-Type', 'application/x-www-form-urlencoded');
    conn.setDoInput(true)
    conn.setDoOutput(true)
    def message = "username=$user&password=$passwd&redirect=.%2Fsearch.php%3Fsearch_id%3Dnewposts&sid=$sid&login=Přihlásit+se".getBytes(StandardCharsets.UTF_8)
    conn.setRequestProperty( "Content-Length", Integer.toString(message.length));
    conn.setUseCaches(false);
    OutputStream os = conn.getOutputStream()
    os.write(message)
    os.flush()
    os.close()

    t = conn.getInputStream().getText().replaceAll(/<[^><]+>/,'')

    assert t.contains("Byli jste úspěšně přihlášeni")

    return conn.getHeaderField("Set-Cookie")
}

