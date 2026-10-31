@echo off
cd /d "%~dp0"
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt test\AllTests.java
java -ea -cp out AllTests
