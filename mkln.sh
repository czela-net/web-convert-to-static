#!/bin/bash
# File name: mkln.sh
# Date:      2010/11/02 11:35
# Author:    Jan Chmelensky <chmelej@seznam.cz>
# $Id$

find -maxdepth 1 -type l | xargs rm

#src=`find src/main/groovy/ -maxdepth 1 -type f -name '*groovy'`
src=`find src/ -maxdepth 1 -type f -name '*groovy'`
for i in $src
do
    shell=`basename $i groovy`sh
    if [ ! -e $shell ] ; then
        ln -s lib/runGroovyScript.sh $shell
    fi
done
