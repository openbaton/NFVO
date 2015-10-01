# OpenBaton

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

### License

[spring.io]:https://spring.io/
[NFV MANO]:http://docbox.etsi.org/ISG/NFV/Open/Published/gs_NFV-MAN001v010101p%20-%20Management%20and%20Orchestration.pdf
[openbaton]:http://twitter.com/openbaton
[website]:http://www.open-baton.org
