#!/bin/bash
urldecode=/home/ubuntu/urldecode.sed
goaccess=/home/ubuntu/goaccess-0.7/goaccess
report=/home/ubuntu/www/report
log=/usr/share/nginx/elasticsearch.log
tomorrow=`date -d "tomorrow" +%d/%b/%Y`
today=`date +%d/%b/%Y`
lastweek=`date -d "last week" +%d/%b/%Y`

if [ $1 = "all" ]; then
        echo "report all"
        awk '$4>"['$lastweek'" && $4<"['$tomorrow'"' $log | sed -f $urldecode | $goaccess > $report/all_`date +%Y_%W`.html
fi

if [ $1 = "search" ]; then
        echo "report search"
        awk '$4>"['$lastweek'" && $4<"['$tomorrow'"' $log | grep '/magic/card/' | sed -f $urldecode | $goaccess > $report/search_`date +%Y_%W`.html
fi

if [ $1 = "android" ]; then
        echo "report android"
        awk '$4>"['$lastweek'" && $4<"['$tomorrow'"' $log | grep 'Android' | sed -f $urldecode | $goaccess > $report/android_`date +%Y_%W`.html
fi

if [ $1 = "daily" ]; then
        echo "report daily"
        grep $today $log | sed -f $urldecode | $goaccess > $report/all_`date +%Y_%m_%d`.html
fi
