#!/bin/bash
set -x
today=`date +%F`
app_dir=app

cd /root/git/pics
git pull
cd ..
mkdir -p $app_dir
rm -rf $app_dir/*
cd $app_dir
curl -sL https://api.github.com/repos/gstraymond/mtg-index-scala/releases/latest | \
  jq -r '.assets[].browser_download_url' | \
  xargs -I {} curl -L {} -so release.zip
unzip release.zip
cd ..
JAVA_OPTS=-Xmx820m find . -name 'mtg-index-scala' -exec {} \; > /tmp/import_${today}.html
rm -rf /tmp/mtg-search/
cd pics
git add .
git commit -m "Import ${today}"
git push
sed -i '1i <pre>' /tmp/import_${today}.html
echo "</pre>" >> /tmp/import_${today}.html
cp /tmp/import_${today}.html /www/report/imports/.
