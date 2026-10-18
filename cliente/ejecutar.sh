#!/usr/bin/env bash
# Compila y ejecuta el cliente (sin Maven). Uso: bash ejecutar.sh [archivo.properties]
cd "$(dirname "$0")" || exit 1
mkdir -p build
javac -encoding UTF-8 -sourcepath src -cp "lib/*" -d build src/main/Game.java || exit 1
CONFIG="${1:-cliente.properties}"
java -Dconfig="$CONFIG" -cp "build:src:lib/*" main.Game
