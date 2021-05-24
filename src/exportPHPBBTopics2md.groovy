/*
 * Cilem cviceni je vytahnout vsechno z fora a pripravit data pro Hugo, 
 * ktery nasledne vygeneruje statitcky archiv fora (neb uz ho asi nepotrebujeme udrzovat)
 *
 * Pro fora a topiky vygeneruju _index.md
 */

import groovy.sql.Sql
import net.czela.common.Helper

import java.text.SimpleDateFormat

import static net.czela.common.Helper.addAttr
import static net.czela.common.Helper.fmt6

Sql sql = Helper.newSqlInstance("phpbb.properties", this)
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
sdf.setTimeZone(TimeZone.getTimeZone("CET"))
def query = """SELECT
f.forum_id, 
f.forum_name,
t.topic_id,
t.topic_title,
t.topic_last_post_id,
t.topic_last_poster_id,
t.topic_last_poster_name,
t.topic_last_post_subject,
FROM_UNIXTIME(t.topic_last_post_time) as topic_last_post_time
FROM phpbb_topics t
JOIN phpbb_forums f ON t.forum_id = f.forum_id"""

sql.eachRow(query) { row ->

   def article = """---
${addAttr('title', row.TOPIC_TITLE)}
${addAttr('date', row.TOPIC_LAST_POST_TIME)}
${addAttr('forum_name', row.FORUM_NAME)}
${addAttr('topic_last_post_id', fmt6(row.TOPIC_LAST_POST_ID))}
${addAttr('topic_last_poster_id', fmt6(row.TOPIC_LAST_POSTER_ID))}
${addAttr('author', row.TOPIC_LAST_POSTER_NAME)}
${addAttr('topic_last_post_subject', row.TOPIC_LAST_POST_SUBJECT)}
folder_type: "topic"
draft: false
---
"""
def dir = new File("contentC/forum_${fmt6(row.FORUM_ID)}/topic_${fmt6(row.TOPIC_ID)}")
if (! dir.exists()) dir.mkdirs()
def file = new File(dir,"_index.md")
if (file.exists()) file.delete()

file << article
}

query = """SELECT 
f.forum_id,
f.forum_name,
f.forum_desc,
f.forum_image,
f.forum_type,
f.forum_status,
f.forum_posts,
f.forum_topics,
f.forum_topics_real,
f.forum_last_post_subject,
f.forum_last_poster_name,
f.forum_flags,
FROM_UNIXTIME(f.forum_last_post_time) as forum_last_post_time
FROM phpbb_forums f"""

sql.eachRow(query) { row ->
    def article = """---
${addAttr('title', row.FORUM_NAME)}
${addAttr('date', row.FORUM_LAST_POST_TIME)}
${addAttr('forum_id', fmt6(row.FORUM_ID))}
${addAttr('forum_desc', row.FORUM_DESC)}
${addAttr('forum_image', row.FORUM_IMAGE)}
${addAttr('forum_type', row.FORUM_TYPE)}
${addAttr('forum_status', row.FORUM_STATUS)}
${addAttr('forum_posts', row.FORUM_POSTS)}
${addAttr('forum_topics', row.FORUM_TOPICS)}
${addAttr('forum_topics_real', row.FORUM_TOPICS_REAl)}
${addAttr('forum_last_post_subject', row.FORUM_LAST_POST_SUBJECT)}
${addAttr('author', row.FORUM_LAST_POSTER_NAME)}
${addAttr('forum_flags', row.FORUM_FLAGS)}
folder_type: "forum"
draft: false
---
"""
    def dir = new File("contentD/forum_${fmt6(row.FORUM_ID)}")
    if (! dir.exists()) dir.mkdirs()
    def file = new File(dir,"_index.md")
    if (file.exists()) file.delete()

    file << article
}

def buf = ""
def ignore = [0]
while(true) {
    int cnt = 0
    def cond = ignore.collect({ it as String }).join(", ")
    sql.eachRow("SELECT * FROM phpbb_forums p WHERE parent_id != 0 AND p.forum_id not in ($cond) AND not exists (SELECT 1 FROM phpbb_forums ch WHERE p.forum_id = ch.parent_id AND ch.forum_id not in ($cond))".toString()) { row ->
        ignore.add(row.FORUM_ID)
        def ch = fmt6(row.FORUM_ID)
        def p = fmt6(row.PARENT_ID)
        buf += "mv forum_${ch} forum_${p}/\n"
        cnt++
    }
    if (cnt == 0) break
}
file = new File("forum_hierarchy.sh")
if (file.exists()) file.delete()
file << buf


