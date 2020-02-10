#!/bin/bash

urldecode=/root/urldecode.sed
report=/www/report
log=/usr/share/nginx/elasticsearch.log
today=`date +%d/%b/%Y`
month=`date +/%b/%Y:`

if [ $1 = "all" ]; then
        echo "report all"
        grep -a $month $log | sed -f $urldecode > /tmp/all
        /usr/bin/goaccess -p /etc/goaccess.conf -f /tmp/all > $report/all_`date +%Y_%B`.html
fi

if [ $1 = "daily" ]; then
        echo "report daily"
        grep -a $today $log | sed -f $urldecode > /tmp/daily
        /usr/bin/goaccess -p /etc/goaccess.conf -f /tmp/daily > $report/all_`date +%Y_%m_%d`.html
fi
