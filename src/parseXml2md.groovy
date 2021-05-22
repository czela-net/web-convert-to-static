def parser = new XmlSlurper()
def contentDir = new File(args[0])

findFiles(contentDir, parser)

def findFiles (File dir, def parser) {
	println(dir.absolutePath)
	for(File f in dir.listFiles()) {
		if (f.directory) {
			findFiles(f, parser)
		} else {
			if (f.name.endsWith(".xml")) {
				parseXml(f, parser)
			}
		}
	}
}

def parseXml(File source, def parser) {
	File dir = new File(source.absolutePath.substring(0, source.absolutePath.length() - 4))
    if (dir.exists()) return

	def m = source.absolutePath =~ /forum_(\d+).topic_(\d+)/
	def forumId = m[0][1]
	def topicId = m[0][2]

	def data = parser.parse(source)
	data.row.each { row ->
		try {
			def postId = row.post_id.text()
			def postTitle = row.post_subject.text()
			def authorName = row.user_fullname.a.text()
			def authorAvatar = row.user_avatar.img.'@src'.text()
			def postDate = toTS(row.post_date.text()) // TODO format stř 02. črc 2014 11:23:21
			def postHtmlMessage = row.post_message.text()
			def postReputation = nvl(row.post_reputation.text(), 0)
			def authorPostsCounter = nvl(row.poster_posts.text(), 0)
			def authorRegisteredFrom = toTS(row.poster_joined.text())
			def authorReputation = nvl(row.u_reputation.text(), 0)

			def inFile = new File("/tmp/forum_parser.html")
			if (inFile.exists()) inFile.delete()
			inFile << "<html><body>$postHtmlMessage</body></html>"

			def outFile = new File("/tmp/forum_parser.md")
			if (outFile.exists()) outFile.delete()

			int exitValue = (new ProcessBuilder("./convert.sh")).redirectInput(inFile).redirectOutput(outFile).start().waitFor()
			assert exitValue == 0
			def md = new String(outFile.readBytes())
			def markdown = """---
title: "$postTitle"
author: "$authorName"
postId: "$postId"
forumId: "$forumId"
topicId: "$topicId"
date: "$postDate"
postReputation: "$postReputation"
authorPostsCounter: "$authorPostsCounter"
authorRegisteredFrom: "$authorRegisteredFrom"
authorReputation: "$authorReputation"
authorAvatar: "$authorAvatar"
---
$md
"""

			if (!dir.exists()) dir.mkdir()
			File out = new File(dir, "post_${fmt6(postId)}.md")
			if (out.exists()) out.delete()
			out << markdown
		} catch(Exception e) {
			println("ERROR: ${e.getMessage()}")
		}
	}
}

def static nvl(def o, def d) {
	o == null ? d : o
}

def static toTS(String date) {
	def m = date =~ /(...) (\d\d)\. (...) (\d{4}) (\d{1,2}:\d{2}:\d{2})/
	if (m.find()) {
		def dow = m[0][1]
		def day = m[0][2]
		def mon = m[0][3]
		def year = m[0][4]
		def time = m[0][5]
		switch (mon) {
			case 'led': mon = '01'; break
			case 'úno': mon = '02'; break
			case 'bře': mon = '03'; break
			case 'dub': mon = '04'; break
			case 'kvě': mon = '05'; break
			case 'čer': mon = '06'; break
			case 'črc': mon = '07'; break
			case 'srp': mon = '08'; break
			case 'zář': mon = '09'; break
			case 'říj': mon = '10'; break
			case 'lis': mon = '11'; break
			case 'pro': mon = '12'; break
			default:
				println("Unknown name $mon")
		}
		return "$year-$mon-${day}T$time"
	//} else {
	//	assert false, "Can not parse '$date'"
	}
}

static def fmt6(def s) {
    if (s == null) return ""
    String ss = s.toString()
    def l = ss.length()
    if (l >= 6) return s
    return "000000$ss".substring(l)
}
