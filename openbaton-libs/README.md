Open Baton LIBS
----------------

Open Baton is an open source project providing a reference implementation of the NFVO and VNFM based on the [ETSI][NFV MANO] specification, is implemented in java using the [spring.io] framework. It consists of two main components: a NFVO and a generic VNFM. This project **openbaton-libs** contains modules that are shared among different projects inside the **Open Baton** framework.

## How does this works? 

As said before, **openbaton-libs** are shared folders. In order to achieve that we took advantage of git subtrees. We belive that in this way it will be easier for users since they don't have to take care of procedures for retrieving any sub-folder. If you are reading that, it means that you are a developer so it is important that you are aware of a couple of things. 

## Which project uses **openbaton-libs**?

The project containing **openbaton-libs** are almost all, but not all use everything available from **openbaton-libs**.

* [NFVO][nfvo-link]: contains **openbaton-libs** and uses almst all 
* [Generic VNFM][generic-link]: contains openbaton-libs and uses only vnfm-sdks 
* All the plugins contains the plugin-sdk, that in turn contains **openbaton-libs**.
* [OpenBaton Client][client-link]: contains **openbaton-libs**, basically because of the catalogue.

## Ok, then?

Well, any modification to this project needs to be reflected in all other projects, so it is extremely important that doesn't brake the status of the depending projects.

## Development

Want to contribute? Great! Get in contact with us. You can find us on twitter @[openbaton]

## News and Website
Information about OpenBaton can be found on our website. Follow us on Twitter @[openbaton].

## License

Copyright (c) 2015-2016 Fraunhofer FOKUS. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


[nfvo-link]: https://github.com/openbaton/NFVO
[generic-link]:https://github.com/openbaton/generic-vnfm
[client-link]: https://github.com/openbaton/openbaton-client
[spring.io]:https://spring.io/
[NFV MANO]:http://docbox.etsi.org/ISG/NFV/Open/Published/gs_NFV-MAN001v010101p%20-%20Management%20and%20Orchestration.pdf
[openbaton]:http://twitter.com/openbaton
