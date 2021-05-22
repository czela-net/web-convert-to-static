#!/bin/bash
# File name: clean.sh
# Date:      2021/05/03 23:50
# Author:    Jan Chmelensky <chmelej@seznam.cz>

# smazu soubory co nejsou XML ale HTML s error hlaskou
find contentB -type f | xargs grep -n -m 1 DOCTYPE | grep ':1:' | cut -d: -f1 | xargs rm
find contentB/ -type f -name '*md' -delete
find contentB/ -type d -empty  -delete


