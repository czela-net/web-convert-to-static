import static net.czela.common.Helper.addAttr
import static net.czela.common.Helper.fmt6
import static net.czela.common.Helper.nvl
import static net.czela.common.Helper.toTS

def parser = new XmlSlurper()
def contentDir = new File(args[0])

findFiles(contentDir, parser)

def findFiles (File dir, def parser) {
	//println(dir.absolutePath)
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
			def postDate = toTS(row.post_date.text())
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
${addAttr('title', postTitle)}
${addAttr('author', authorName)}
${addAttr('postId', postId)}
${addAttr('forumId', forumId)}
${addAttr('topicId', topicId)}
${addAttr('date', postDate)}
${addAttr('postReputation', postReputation)}
${addAttr('authorPostsCounter', authorPostsCounter)}
${addAttr('authorRegisteredFrom', authorRegisteredFrom)}
${addAttr('authorReputation', authorReputation)}
${addAttr('authorAvatar', authorAvatar)}
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

