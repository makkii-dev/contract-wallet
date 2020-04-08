#!/bin/bash

# search the javac executable
if type -p javac; then
    JAVAC=javac
    JAR=jar
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/javac" ]];  then
    JAVAC="$JAVA_HOME/bin/javac"
    JAR="$JAVA_HOME/bin/jar"
else
    echo "No javac found in your system!"
    exit 2
fi

# clean
echo "Cleaning the build folder..."
rm -fr "./build"

# compile
echo "Compiling the source code..."
$JAVAC --release 10 -cp "./avm/*" -d "./build" "${@:2}"  || exit 3

# assemble the bytecode
echo "Assembling the final jar..."
cd "./build"
$JAR -xf "../avm/org-aion-avm-userlib.jar"
mkdir -p "META-INF"
echo "Main-Class: $1" > "./META-INF/MANIFEST.MF"
$JAR -cfm "dapp.jar" "./META-INF/MANIFEST.MF" .
cd ..

# done
echo "The jar has been generated at: $(realpath ./build/dapp.jar)"