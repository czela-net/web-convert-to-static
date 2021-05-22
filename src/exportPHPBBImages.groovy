/*
 * Cilem cviceni je vytahnout vsechno z fora a pripravit data pro Hugo, 
 * ktery nasledne vygeneruje statitcky archiv fora (neb uz ho asi nepotrebujeme udrzovat)
 *
 * Pro fora a topiky vygeneruju _index.md
 */

import groovy.sql.Sql
import net.czela.common.Helper

import java.text.SimpleDateFormat

Sql sql = Helper.newSqlInstance("phpbb.properties", this)

def buf = ""
sql.eachRow("SELECT physical_filename, real_filename FROM phpbb_attachments") { row ->
    def p = row.PHYSICAL_FILENAME
    def r = row.REAL_FILENAME.replaceAll(/\s+/,'_')
    buf += "mv '${p}' '$r'\n"
}
def out = new File("rename_images.sh")
if (out.exists()) out.delete()
out << buf
