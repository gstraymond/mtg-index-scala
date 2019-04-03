#!/bin/bash
set -x
today=`date +%F`

rm /tmp/import_${today}.html
cd /root/git/mtg-index-scala
git pull
git submodule update --init --remote
./gradlew clean installDist
JAVA_OPTS="-Xmx1g" build/install/mtg-index-scala/bin/mtg-index-scala >> /tmp/import_${today}.html
cd pics
git add .
git commit -m "Import ${today}"
git push
cd ..
git add .
git commit -m "Import ${today}"
git push
cat /tmp/import_${today}.html | mail -s "MGS import ${today}" tcherno@gmail.com
sed -i '1i <pre>' /tmp/import_${today}.html
echo "</pre>" >> /tmp/import_${today}.html
cp /tmp/import_${today}.html /root/www/report/imports/.