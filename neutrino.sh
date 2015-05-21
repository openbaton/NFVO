#!/bin/bash

version="0.2-SNAPSHOT"

function start {

    if [ ! -d build/  ]
        then
            ./gradlew build -x test
    fi

    if [ 0 -eq $? ]
        then
            java -jar "build/libs/neutrino-$version.jar"
    fi
}

function compile {
    ./gradlew build -x test
}

function tests {
    ./gradlew test
}

function clean {
    ./gradlew clean
}

function end {
    exit
}
function usage {
    echo -e "Neutrino\n"
    echo -e "Usage:\n\t ./neutrino.sh <option>\n\t"
    echo -e "where option is"
    echo -e "\t\t * compile"
    echo -e "\t\t * start"
    echo -e "\t\t * test"
    echo -e "\t\t * clean"
}

if [ $# -eq 0 ]
   then
        usage
        exit 1
fi

for var in "$@"
do
    case $var in
        "clean" )
            clean ;;
        "sc" )
            clean
            compile
            start ;;
        "start" )
            start ;;
        "compile" )
            compile ;;
        "test" )
            tests ;;
        * )
            usage
            end ;;
    esac
done

