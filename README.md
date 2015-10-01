OpenBaton LIBS
----------------

OpenBaton is an open source project providing a reference implementation of the NFVO and VNFM based on the [ETSI][NFV MANO] specification, is implemented in java using the [spring.io] framework. It consists of two main components: a NFVO and a generic VNFM. This project **openbaton-libs** contains modules that are shared among different projects inside the **OpenBaton** system.

#### How does this works? 

As said before, **openbaton-libs** are shared folders. In order to achieve that we took advantage of git subtrees. We belive that in this way it will be easier for users since they don't have to take care of procedures for retrieving any sub-folder. If you are reading that, it means that you are a developer so it is important that you are aware of a couple of things. 

###### Who contains **openbaton-libs**?

The project containing **openbaton-libs** are almost all, but not all use everything available from **openbaton-libs**.

* [NFVO][nfvo-link]: contains **openbaton-libs** and uses almst all 
* [Generic VNFM][generic-link]: contains openbaton-libs and uses only vnfm-sdks 
* All the plugins contains the plugin-sdk, that in turn contains **openbaton-libs**.
* [OpenBaton Client][client-link]: contains **openbaton-libs**, basically because of the catalogue.

###### Ok, then?

Well, any modification to this project needs to be reflected in all other projects, so it is extremely important that doesn't brake the status of the depending projects.

#### Version
0.6

##### Installation

No installation required.

### Development

Want to contribute? Great! Get in contact with us. You can find us on twitter @[openbaton]

### News and Website
Information about OpenBaton can be found on our website. Follow us on Twitter @[openbaton].

### License

[nfvo-link]: https://github.com/openbaton/NFVO
[generic-link]:https://github.com/openbaton/generic-vnfm
[client-link]: https://github.com/openbaton/openbaton-client
[spring.io]:https://spring.io/
[NFV MANO]:http://docbox.etsi.org/ISG/NFV/Open/Published/gs_NFV-MAN001v010101p%20-%20Management%20and%20Orchestration.pdf
[openbaton]:http://twitter.com/openbaton
