#!/bin/bash
sudo java \
-Dorg.slf4j.simpleLogger.defaultLogLevel=trace \
-jar eink-rpi-server.jar \
4 0 192.168.1.2 8080

#-Dpi4j.library.path=$(realpath ./lib/armhf/) \
