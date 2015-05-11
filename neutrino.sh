#!/bin/bash

name=$(./gradlew -q getVersion)

function start {

    if [ ! -f build/libs/neutrino.jar  ]
        then
            ./gradlew build
    fi

    if [ 0 -eq $? ]
        then
            java -jar build/libs/neutrino.jar
    fi
}

function compile {
    echo -e "building $name"
    ./gradlew build
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
    echo -e "$name\n"
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

