/*
 * Cilem cviceni je vytahnout vsechno z fora a pripravit data pro Hugo, 
 * ktery nasledne vygeneruje statitcky archiv fora (neb uz ho asi nepotrebujeme udrzovat)
 *
 * generator vygeneruje vsechny zpravy PHPBB format neresi
 */

import groovy.sql.Sql
import net.czela.common.Helper

import java.text.SimpleDateFormat

Sql sql = Helper.newSqlInstance("phpbb.properties", this)
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
sdf.setTimeZone(TimeZone.getTimeZone("CET"));
def query = """SELECT 
p.post_id, 
p.topic_id, 
p.forum_id, 
p.poster_id AS user_id, 
u.username,
f.forum_name,
t.topic_title,
p.post_subject,
p.post_text,
FROM_UNIXTIME(post_time) as post_time
FROM phpbb_posts p
JOIN phpbb_topics t ON p.topic_id = t.topic_id
JOIN phpbb_forums f ON p.forum_id = f.forum_id
JOIN phpbb_users u ON u.user_id = p.poster_id"""
//WHERE p.poster_id = 2"""

sql.eachRow(query) { row ->
   def article = """---
title: "$row.post_subject"
author: "$row.username"
postId: $row.post_id
forumId : $row.forum_id 
topicId : $row.topic_id 
date: "${sdf.format(row.post_time)}"
userId : $row.user_id 
forumName: "$row.forum_name"
topicTitle: "$row.topic_title"
draft: false
---
$row.post_text
"""
def dir = new File("contentA/forum_${fmt6(row.forum_id)}/topic_${fmt6(row.topic_id)}")
if (! dir.exists()) dir.mkdirs()
def file = new File(dir,"post_${fmt6(row.post_id)}.md")
if (! file.exists()) file.delete()

file << article
}

static def fmt6(def s) {
    if (s == null) return ""
    String ss = s.toString()
    def l = ss.length()
    if (l >= 6) return s
    return "000000$ss".substring(l)
}
