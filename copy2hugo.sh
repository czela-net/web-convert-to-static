hugoc='/home/chmelej/prace/czela/web-convert-to-static/czela-forum/content'
rm -rf $hugoc/fo*
(cd contentD/ ; tar cf - --exclude '*xml' fo*) | (cd $hugoc/ ; tar xf - )
(cd contentC/ ; tar cf - --exclude '*xml' fo*) | (cd $hugoc/ ; tar xf - )
(cd contentB/ ; tar cf - --exclude '*xml' fo*) | (cd $hugoc/ ; tar xf - )

(cd $hugoc ; bash /home/chmelej/prace/czela/web-convert-to-static/forum_hierarchy.sh)
