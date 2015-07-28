#!/bin/bash

_base=/opt
_openbaton_base="${_base}/openbaton"
_openbaton_config_file=/etc/openbaton/openbaton.properties

function start {
    screen -S openbaton "${_openbaton_base}/bin/openbaton --spring.config.location=file:${_openbaton_config_file}"
}

function stop {
    if screen -list | grep "openbaton"; then
	    screen -S openbaton -p 0 -X stuff "exit$(printf \\r)"
    fi
}

function kill {
    if screen -list | grep "openbaton"; then
	    screen -X -S openbaton kill
    fi
}

function end {
    exit
}
function usage {
    echo -e "Open-Baton\n"
    echo -e "Usage:\n\t ./openbaton.sh <option>\n\t"
    echo -e "where option is"
    echo -e "\t\t * start"
    echo -e "\t\t * stop"
    echo -e "\t\t * kill"
}

##
#   MAIN
#   TODO start activemq and/or define application.properties
##

if [ $# -eq 0 ]
   then
        usage
        exit 1
fi

declare -a cmds=($@)
for (( i = 0; i <  ${#cmds[*]}; ++ i ))
do
    case ${cmds[$i]} in
        "start" )
            start ;;
        "stop" )
            stop ;;
        "kill" )
            kill ;;
        * )
            usage
            end ;;
    esac
done

