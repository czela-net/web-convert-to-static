# je potreba a) mit otevreny tunel na kazbundu aby fungovalo spojeni
#            b) prihlasit se k foru a zkopirovat cerstvy cookies
#rm -rf contentA/ contentB/ contentC/ contentD/ czela-forum/public czela-forum/content/*
#
#set -e
#./exportPHPBB2md.sh
#./exportPHPBB2xml.sh
#./exportPHPBBTopics2md.sh
#
## smazu soubory co nejsou XML ale HTML s error hlaskou
#find contentB -type f | xargs grep -n -m 1 DOCTYPE | grep ':1:' | cut -d: -f1 | xargs rm
#find contentB/ -type f -name '*md' -delete
#find contentB/ -type d -empty  -delete
#./parseXml2md.sh contentB/


hugoc='czela-forum/content'
rm -rf $hugoc/f*
(cd contentD/ ; tar cf - --exclude '*xml' f*) | (cd $hugoc/ ; tar xf - )
(cd contentC/ ; tar cf - --exclude '*xml' f*) | (cd $hugoc/ ; tar xf - )
(cd contentB/ ; tar cf - --exclude '*xml' f*) | (cd $hugoc/ ; tar xf - )

(cd $hugoc ; bash /home/chmelej/prace/czela/web-convert-to-static/forum_hierarchy.sh)
(cd czela-forum ; hugo )
