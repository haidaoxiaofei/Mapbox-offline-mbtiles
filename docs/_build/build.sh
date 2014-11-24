#!/usr/bin/env bash

VERSION=$1

if [ -z $VERSION ]; then
  echo "Specify a version ie. 0.2.3"
  exit
fi

mkdir $VERSION
curl --get -L  "http://search.maven.org/remotecontent?filepath=com/mapbox/mapboxsdk/mapbox-android-sdk/$VERSION/mapbox-android-sdk-$VERSION-javadoc.jar" > $VERSION/mapbox-android-sdk-$VERSION.jar

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
    sed -e 's,<a href="[./]*com/mapbox/mapboxsdk/.*/\([^"]*\).html\(#*[^"]*\)",<a href="{{site.baseurl}}/api/\L\1\2",g' | \
    sed -e 's,<br[ /]*>,,g' | \
    sed -e 's,<hr>,,g' | \
    sed -e 's,<caption>,<caption class="small dark strong round-top pad1 fill-darken3">,g' | \
    sed -e '1,/^\<div class="description">$/b' | \
    sed -e 's,<ul class="inheritance">,,g' | \
    sed -e 's,<ul class="blockList">,,g' | \
    sed -e 's,<ul class="blockListLast">,,g' | \
    sed -e 's,</ul>,,g' | \
    sed -e 's,<li class="blockList">,,g' | \
    sed -e 's,<li>,,g' | \
    sed -e 's,</li>,,g'
}

for file in `find com/mapbox/mapboxsdk -name "*.html" | grep -v package-`; do

CONTENT="\
---
layout: api
title: $(echo ${file##*/} | sed -e 's/\([a-z]\)\([A-Z]\)/\1\2/g' -e 's/\.html$//')
category: api
tags: $(echo $file | sed 's,com/mapbox/mapboxsdk/\([^/]*\)/.*,\1,')
---"

FILENAME=$(echo '../../_posts/api/0100-01-01-'${file##*/} | sed -e 's/\([a-z]\)\([A-Z]\)/\1\2/g' | tr '[:upper:]' '[:lower:]')
CONTENT="$CONTENT\n$(scrape $file)"

ALL="$ALL\n <h2>$(echo ${file##*/} | sed -e 's/\([a-z]\)\([A-Z]\)/\1\2/g' -e 's/\.html$//')</h2>\n $(scrape $file)"

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
