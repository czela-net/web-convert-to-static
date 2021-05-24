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

import static net.czela.common.Helper.fmt6

Sql sql = Helper.newSqlInstance("phpbb.properties", this)

int cnt = 0;
sql.eachRow("SELECT topic_id, forum_id, min(post_id) post_id FROM phpbb_posts group by topic_id, forum_id".toString()) { row ->
    try {
        String url = "https://www.czela.net/f/viewtopicz.php?f=${row.FORUM_ID}&t=${row.TOPIC_ID}&p=${row.POST_ID}"
        def data = doGet(url)

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
static String doGet (def url) {
    def conn = new URL(url).openConnection()
    conn.setRequestProperty('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8')
    conn.setRequestProperty('Cookie', 'phpbb3_h4ku8_u=2; phpbb3_h4ku8_k=; phpbb3_h4ku8_sid=9b63d0b983534b19e6e9303efe1f95f0; __tsid=70dd809c-3876-47fd-abc6-2540d6ce6203; style_cookie=null')
    return conn.getInputStream().getText()
}

