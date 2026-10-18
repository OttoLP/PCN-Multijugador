@echo off
rem Compila y ejecuta el cliente (sin Maven). Uso: ejecutar.bat [archivo.properties]
cd /d "%~dp0"
if not exist build mkdir build
javac -encoding UTF-8 -sourcepath src -cp "lib\*" -d build src\main\Game.java
if errorlevel 1 exit /b 1
set CONFIG=%~1
if "%CONFIG%"=="" set CONFIG=cliente.properties
java -Dconfig=%CONFIG% -cp "build;src;lib\*" main.Game
