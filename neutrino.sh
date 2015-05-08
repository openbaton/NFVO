#!/bin/bash


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
    echo -e "neutrino Usage:\n\t ./neutrino.sh <option>\n\n\n\twhere option is\n\t\t * compile\n\t\t * start\n\t\t * test\n"
}

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

