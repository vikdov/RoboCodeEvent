@echo off
echo Compiling...
if not exist out mkdir out
javac -d out src\Main.java

echo Running...
java -cp out Main

