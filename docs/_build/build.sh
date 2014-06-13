#!/usr/bin/env bash

VERSION=$1

if [ -z $VERSION ]; then
  echo "Specify a version ie. 0.2.3"
  exit
fi

mkdir $VERSION
curl "http://search.maven.org/remotecontent?filepath=com/mapbox/mapboxsdk/mapbox-android-sdk/$VERSION/mapbox-android-sdk-$VERSION-javadoc.jar" > $VERSION/mapbox-android-sdk-$VERSION.jar

cd $VERSION && unzip mapbox-android-sdk-$VERSION.jar

# Drop some things we dont need
rm mapbox-android-sdk-$VERSION.jar
rm -rf '../../_posts/api/*'

ALL=''
HTMLTOP='<div class="contentContainer">'
HTMLEND='<div class="bottomNav">'

scrape() {
  FR=`grep -n "$HTMLTOP" $1 | grep -o [0-9]*`
  TO=`grep -n "$HTMLEND" $1 | grep -o [0-9]*`
  LINES=`echo "$TO - $FR" | bc`
  tail -n +$FR $1 | head -n $LINES | \
    sed -e 's,<br[ /]*>,,g' \
    -e 's,<hr>,,g' \
    -e 's,<a href="../../../../com/mapbox/mapboxsdk/.*/\([^"]*\).html\(#*[^"]*\)",<a href="{{site.baseurl}}/api/\1\2",g' \
    -e 's,<caption>.*</caption>,,g' \
    -e '1,/^\<div class="description">$/b' \
    -e 's,<ul class="inheritance">,,g' \
    -e 's,<ul class="blockList">,,g' \
    -e 's,<ul class="blockListLast">,,g' \
    -e 's,</ul>,,g' \
    -e 's,<li class="blockList">,,g' \
    -e 's,<li>,,g' \
    -e 's,</li>,,g'
}

for file in `find com/mapbox/mapboxsdk/*/*.html | grep -v package-`; do

CONTENT="\
---
layout: api
title: $(echo ${file##*/} | sed -e 's/\([a-z]\)\([A-Z]\)/\1 \2/g' -e 's/\.html$//')
category: api
tags: $(echo $file | sed 's#.*/\([^/]*\)/[^/]*#\1#')
---"

FILENAME=$(echo '../../_posts/api/0100-01-01-'${file##*/} | sed -e 's/\([a-z]\)\([A-Z]\)/\1\2/g' | tr '[:upper:]' '[:lower:]')
CONTENT="$CONTENT\n$(scrape $file)"

ALL="$ALL\n$(scrape $file)"

echo -e "$CONTENT" > $FILENAME
done

ALLYAML="\
---
layout: api
title: Mapbox Android SDK $VERSION
category: api
---"

echo -e "$ALLYAML $ALL" > '../../api/index.html' 

echo "Complete!"
exit
