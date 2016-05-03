# Open Baton
[![Build Status](https://travis-ci.org/openbaton/NFVO.svg?branch=master)](https://travis-ci.org/openbaton/NFVO)
[![Join the chat at https://gitter.im/openbaton/NFVO](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/openbaton/NFVO?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Documentation Status](https://readthedocs.org/projects/openbaton-docs/badge/?version=latest)](http://openbaton-docs.readthedocs.org/en/latest/?badge=latest)

Open Baton is an open source project providing a reference implementation of the NFVO and VNFM based on the ETSI [NFV MANO] specification. 

## Getting Started

Open Baton is implemented in java using the [spring.io] framework. For more details about the NFVO architecture, you can refer to the following page.

## install the latest NFVO version from the source code


The NFVO uses the AMQP protocol for communicating with the VNFMs. Therefore an implementation of it is necessary, we chose RabbitMQ. To facilitate the installation procedures we provide an installation procedure which will install the NFVO and the prerequired libraries. To execute the following command you need to have curl installed (see http://curl.haxx.se/).

To install the OpenBaton NFVO through its debian package you can type the following command:

```bash
bash <(curl -fsSkL http://get.openbaton.org/bootstrap)
```

Please follow the documentation on [our website][installation-guide] for more information on how to configure and use it.

## Development

Want to contribute? Great! [Get in contact with us](mailto:info@openbaton.org).

## News and Website
Information about OpenBaton can be found on our @[website]. Follow us on Twitter @[openbaton].

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

## Supported by
Open Baton is a project developed by Fraunhofer FOKUS and TU Berlin. It is supported by different European publicly funded projects: 

* [NUBOMEDIA][nubomedia]
* [Mobile Cloud Networking][mcn]
* [CogNet][cognet]

[spring.io]:https://spring.io/
[NFV MANO]:http://www.etsi.org/deliver/etsi_gs/NFV-MAN/001_099/001/01.01.01_60/gs_nfv-man001v010101p.pdf
[openbaton]:http://twitter.com/openbaton
[website]:http://www.open-baton.org
[nubomedia]: https://www.nubomedia.eu/
[mcn]: http://mobile-cloud-networking.eu/site/
[cognet]: http://www.cognet.5g-ppp.eu/cognet-in-5gpp/
[installation-guide]: http://openbaton.github.io/documentation/nfvo-installation/
