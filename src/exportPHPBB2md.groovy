/*
 * Cilem cviceni je vytahnout vsechno z fora a pripravit data pro Hugo, 
 * ktery nasledne vygeneruje statitcky archiv fora (neb uz ho asi nepotrebujeme udrzovat)
 *
 * generator vygeneruje vsechny zpravy PHPBB format neresi
 */

import groovy.sql.Sql
import net.czela.common.Helper

import java.text.SimpleDateFormat

import static net.czela.common.Helper.fmt6

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
title: "$row.POST_SUBJECT"
author: "$row.USERNAME"
postId: $row.POST_ID
forumId : $row.FORUM_ID 
topicId : $row.TOPIC_ID 
date: "${sdf.format(row.POST_TIME)}"
userId : $row.USER_ID 
forumName: "$row.FORUM_NAME"
topicTitle: "$row.TOPIC_TITLE"
draft: false
---
$row.POST_TEXT
"""
def dir = new File("contentA/forum_${fmt6(row.FORUM_ID)}/topic_${fmt6(row.TOPIC_ID)}")
if (! dir.exists()) dir.mkdirs()
def file = new File(dir,"post_${fmt6(row.POST_ID)}.md")
if (! file.exists()) file.delete()

file << article
}
