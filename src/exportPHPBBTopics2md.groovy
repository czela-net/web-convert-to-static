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
title: ${delTags(row.topic_title)}
date: "${row.topic_last_post_time}"
forum_name: "${row.forum_name}"
topic_last_post_id: "${fmt6(row.topic_last_post_id)}"
topic_last_poster_id: "${fmt6(row.topic_last_poster_id)}"
author: "${row.topic_last_poster_name}"
topic_last_post_subject: ${delTags(row.topic_last_post_subject)}
folder_type: "topic"
draft: false
---
"""
def dir = new File("contentC/forum_${fmt6(row.forum_id)}/topic_${fmt6(row.topic_id)}")
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
title: ${delTags(row.forum_name)}
date: "${row.forum_last_post_time}"
forum_id: "${fmt6(row.forum_id)}"
forum_desc: ${delTags(row.forum_desc)}
forum_image: "${row.forum_image}"
forum_type: "${row.forum_type}"
forum_status: "${row.forum_status}"
forum_posts: "${row.forum_posts}"
forum_topics: "${row.forum_topics}"
forum_topics_real: "${row.forum_topics_real}"
forum_last_post_subject: "${row.forum_last_post_subject}"
author: "${row.forum_last_poster_name}"
forum_flags: "${row.forum_flags}"
folder_type: "forum"
draft: false
---
"""
    def dir = new File("contentD/forum_${fmt6(row.forum_id)}")
    if (! dir.exists()) dir.mkdirs()
    def file = new File(dir,"_index.md")
    if (file.exists()) file.delete()

    file << article
}

def buf = ""
def ignore = [0];
while(true) {
    int cnt = 0
    def cond = ignore.collect({ it as String }).join(", ")
    sql.eachRow("SELECT * FROM phpbb_forums p WHERE parent_id != 0 AND p.forum_id not in ($cond) AND not exists (SELECT 1 FROM phpbb_forums ch WHERE p.forum_id = ch.parent_id AND ch.forum_id not in ($cond))".toString()) { row ->
        ignore.add(row.FORUM_ID);
        def ch = fmt6(row.FORUM_ID)
        def p = fmt6(row.PARENT_ID)
        buf += "mv forum_${ch} forum_${p}/\n"
        cnt++;
    }
    if (cnt == 0) break
}
file = new File("forum_hierarchy.sh")
if (file.exists()) file.delete()
file << buf



static def fmt6(def s) {
    if (s == null) return ""
    String ss = s.toString()
    def l = ss.length()
    if (l >= 6) return s
    return "000000$ss".substring(l)
}

static def delTags(def x) {
    String xx = x.replaceAll(/<[^<>]+>/,'')
    (xx.contains('"'))?"'${xx}'":"\"${xx}\""
}

static def nvl(def a, def b) {
    (a != null && a.toString().length > 0)?a:b
}
