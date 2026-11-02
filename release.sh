#!/bin/zsh
# Publica una versió nova a GitHub. Les apps instal·lades la detecten i s'actualitzen soles.
# Abans, puja versionCode i versionName a app/build.gradle.kts.
# Ús: ./release.sh "Què hi ha de nou"
set -euo pipefail
cd "$(dirname "$0")"

code=$(grep -E 'versionCode = ' app/build.gradle.kts | grep -oE '[0-9]+')
name=$(grep -E 'versionName = ' app/build.gradle.kts | sed -E 's/.*"(.*)".*/\1/')
notes=${1:-"Versió $name"}

if gh release view "v$name" >/dev/null 2>&1; then
    echo "La versió v$name ja existeix: puja versionCode i versionName primer." >&2
    exit 1
fi

export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleRelease -q

out=build/release
rm -rf $out && mkdir -p $out
cp app/build/outputs/apk/release/app-release.apk $out/carlina-palius.apk
echo "{\"versionCode\": $code, \"versionName\": \"$name\"}" > $out/version.json

gh release create "v$name" $out/carlina-palius.apk $out/version.json \
    --title "Carlina Palius $name" --notes "$notes"
