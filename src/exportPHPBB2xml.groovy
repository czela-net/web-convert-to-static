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

Sql sql = Helper.newSqlInstance("phpbb.properties", this)

int cnt = 0;
sql.eachRow("SELECT topic_id, forum_id, min(post_id) post_id FROM phpbb_posts group by topic_id, forum_id".toString()) { row ->
    try {
        String url = "https://www.czela.net/f/viewtopicz.php?f=${row.forum_id}&t=${row.topic_id}&p=${row.post_id}"
        //def data = new URL(url).getText()
        def data = doGet(url)

        def dir = new File("contentB/forum_${fmt6(row.forum_id)}")
        if (!dir.exists()) dir.mkdirs()
        def file = new File(dir, "topic_${fmt6(row.topic_id)}.xml")
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
    static String doGet (def url) {
        def conn = new URL(url).openConnection()
        conn.setRequestProperty('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8')
        conn.setRequestProperty('Cookie','PHPSESSID=6jkjg645k41j5b4qegdn0jn722; phpbb3_h4ku8_u=2; phpbb3_h4ku8_k=; phpbb3_h4ku8_sid=2a3833e289fd7bc63bfbf6ac72087c69; __tsid=0e2a6815-c12f-412f-b49e-27d69d318dbd; style_cookie=null');

        return conn.getInputStream().getText()
    }

static def fmt6(def s) {
    if (s == null) return ""
    String ss = s.toString()
    def l = ss.length()
    if (l >= 6) return s
    return "000000$ss".substring(l)
}
