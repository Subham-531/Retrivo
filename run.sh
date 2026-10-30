#!/usr/bin/env bash

# FindIt: macOS / Linux One-Click Launcher
JAVA_CMD="/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home/bin/java"
if ! [ -x "$JAVA_CMD" ]; then
  JAVA_CMD="java"
fi

echo "============================================================"
echo "  Starting FindIt: Smart Lost and Found Matcher"
echo "============================================================"
echo "Web Application URL: http://localhost:8080/"
echo "Demo Credentials:"
echo "  Student: suyash@campus.edu  / Student@123"
echo "  Admin:   admin@findit.campus.edu / Admin@123"
echo "============================================================"

exec "$JAVA_CMD" \
  -Dserver.port=8080 \
  -jar "backend/target/findit-backend-1.0.0.jar" \
  --spring.datasource.url="jdbc:mysql://localhost:3306/findit_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  --spring.datasource.username=root \
  --spring.datasource.password=subham123

