#!/bin/bash
# File name: hugo_server.sh
# Date:      2021/05/19 14:40
# Author:    Jan Chmelensky <chmelej@seznam.cz>
sensible-browser http://localhost:1313 &
hugo server -D
