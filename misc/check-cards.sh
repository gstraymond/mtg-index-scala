while read card; do
  RESULT=$(curl --get -s --data-urlencode "q=title:${card}" "localhost:9200/mtg/_search?size=50" | \
    jq -r '.hits.hits[]._source.title' | \
    grep "${card}" | \
    wc -l | \
    tr -d ' ')
  if [ "$RESULT" == "0" ]; then  
    echo "${card}"
  fi
done < cards