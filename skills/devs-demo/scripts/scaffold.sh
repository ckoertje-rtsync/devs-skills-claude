#!/usr/bin/env sh
# Copy the devs-demo templates into a Closure-DEVS gradle project.
# Usage: scaffold.sh <repo-root> <java-package> <java-source-dir-of-package> <demo-name>
#   e.g. scaffold.sh ~/proj Models.java src/Models/java my-demo
# Writes <pkg dir>/TraceRecorder.java and DemoDataRunner.java, demos/<demo-name>.html (the worked
# example to adapt), demos/test/check-demo.js, demos/data/, and prints the gradle task to append.
set -eu
ROOT=$1; PKG=$2; PKGDIR=$3; NAME=$4
HERE=$(cd "$(dirname "$0")/.." && pwd)
T=$HERE/resources/templates
mkdir -p "$ROOT/$PKGDIR" "$ROOT/demos/data" "$ROOT/demos/test"
BASE=$(printf '%s' "$NAME" | sed 's/-demo$//')
CAMEL=$(printf '%s' "$BASE" | sed 's/[^A-Za-z0-9]//g')
API=$(printf '%s' "$CAMEL" | cut -c1 | tr 'a-z' 'A-Z')$(printf '%s' "$CAMEL" | cut -c2-)Demo
DATA=$(printf '%s' "$BASE" | tr 'a-z-' 'A-Z_')_DEMO
for f in TraceRecorder.java DemoDataRunner.java; do
  [ -e "$ROOT/$PKGDIR/$f" ] && { echo "exists, skipped: $PKGDIR/$f"; continue; }
  sed "s/__PACKAGE__/$PKG/g; s/__GLOBAL__/$DATA/g" "$T/$f" > "$ROOT/$PKGDIR/$f"; echo "wrote $PKGDIR/$f"
done
if [ ! -e "$ROOT/demos/$NAME.html" ]; then
  sed "s/NTN_DEMO/$DATA/g; s/NTNDemo/$API/g" "$HERE/resources/examples/ntn-flare/ntn-flare-demo.html" > "$ROOT/demos/$NAME.html"
  echo "wrote demos/$NAME.html (worked example: adapt CAT, kindOf, pathTaken, geometry, project panel)"
fi
if [ ! -e "$ROOT/demos/test/check-demo.js" ]; then
  sed "s/__PAGE__/$NAME/g; s/__API__/$API/g; s/__DATA__/$DATA/g; s/__ROOT__/Transmitter_GS/g" "$T/check-demo.js" > "$ROOT/demos/test/check-demo.js"
  echo "wrote demos/test/check-demo.js"
fi
echo; echo "Append to build.gradle:"; sed "s/__PACKAGE__/$PKG/g" "$T/build.gradle.snippet"
echo; echo "Page global: window.$DATA   pure-function API: window.$API"
