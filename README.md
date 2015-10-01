# OpenBaton
[![Build Status](https://travis-ci.org/openbaton/NFVO.svg?branch=master)](https://travis-ci.org/openbaton/NFVO)
[![Join the chat at https://gitter.im/openbaton/NFVO](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/openbaton/NFVO?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

OpenBaton is an open source project providing a reference implementation of the NFVO and VNFM based on the ETSI [NFV MANO] specification. 

## Getting Started

`openbaton` is implemented in java using the [spring.io] framework. For more details about the NFVO architecture, you can refer to the following page.

## install the latest NFVO version from the source code

The NFVO uses the Java Messaging System for communicating with the VNFMs. Therefore it is a prerequisites to have ActiveMQ up and running. To facilitate the installation procedures we provide an installation script which can be used for installing the NFVO and the prerequired libraries. Considering that this script needs to install some system libraries, it is required to execute it as super user. 

```bash
sudo su -
curl -fsSkL https://gitlab.fokus.fraunhofer.de/openbaton/bootstrap/raw/develop/openbaton.sh |bash
```

At the end of the installation procedure, if there are no errors, the dashboard should be reachable at: http://localhost:8080. At this point the NFVO is ready to be used. Please refer to the NFVO user guide for how to start using it. 

## Development

Want to contribute? Great! Get in contact with us. You can find us on twitter @[openbaton]

## News and Website
Information about OpenBaton can be found on our @[website]. Follow us on Twitter @[openbaton].


[spring.io]:https://spring.io/
[NFV MANO]:http://www.etsi.org/deliver/etsi_gs/NFV-MAN/001_099/001/01.01.01_60/gs_nfv-man001v010101p.pdf
[openbaton]:http://twitter.com/openbaton
[website]:http://www.open-baton.org
