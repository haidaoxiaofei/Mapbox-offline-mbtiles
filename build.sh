echo
echo
echo Mapbox Android SDK builder
echo
echo

curl -O http://downloads.gradle.org/distributions/gradle-1.8-bin.zip
unzip gradle-1.8-bin
gradle-1.8/bin/gradle build
mv build/apk/mapbox-android-sdk-debug-unaligned.apk