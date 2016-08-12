  <img src="https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/openBaton.png" width="250"/>

  Copyright © 2015-2016 [Open Baton](http://openbaton.org).
  Licensed under [Apache v2 License](http://www.apache.org/licenses/LICENSE-2.0).
  
[![Build Status](https://travis-ci.org/openbaton/NFVO.svg?branch=master)](https://travis-ci.org/openbaton/NFVO)
[![Join the chat at https://gitter.im/openbaton/NFVO](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/openbaton/NFVO?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Documentation Status](https://readthedocs.org/projects/openbaton-docs/badge/?version=stable)](http://openbaton-docs.readthedocs.io/en/stable/?badge=stable)

# Open Baton NFV Orchestrator

Open Baton NFVO is an open source project providing a reference implementation of the NFVO based on the ETSI NFV MANO specification. 

## Technical Requirements

In order to execute the NFVO you need to have installed a JVM and RabbitMQ. Those components are typically installed as part of the bootstrap installation scripts provided.

In addition, you need to have a running and configured Virtualized Infrastructure Manager (VIM). Open Baton provides a plugin for getting started with OpenStack as initial VIM. 

## How to install Open Baton NFVO

In order to install Open Baton you can follow the [installation guide](http://openbaton.github.io/documentation/nfvo-installation-deb/) that will guide you to the installation procedure step by step. You can choose between installing a development version (compiling the latest version available) or a more stable version (downloading the binary packages from our repository). 

You can also execute the installation by your own checking out this repository and compiling the source code with gradle. However, you need to make sure that all the requirements are satisfied (RabbitMQ properly configured, etc.), therefore we suggest to follow the documentation for avoiding any issues. 

## How to use Open Baton NFVO

### Dashboard

Open Baton provides an easy to use dashboard. After you complete the installation of the NFVO, the dashboard should be available on port 8080. Therefore, connecting to [http://localhost:8080] you should be able to access the dashboard and start playing with it. Please refer to the [documentation][openbaton-doc] for more details.

### APIs

For the full list of APIs please refer to our [API DOC](http://get.openbaton.org/api/ApiDoc.pdf)

## How to extend Open Baton NFVO

In our [documentation](http://openbaton.github.io/documentation/extend/) there are multiple tutorials that guide you through this process

## Issue tracker

Issues and bug reports should be posted to the GitHub Issue Tracker of this project

# What is Open Baton?

OpenBaton is an open source project providing a comprehensive implementation of the ETSI Management and Orchestration (MANO) specification.

Open Baton is a ETSI NFV MANO compliant framework. Open Baton was part of the OpenSDNCore (www.opensdncore.org) project started almost three years ago by Fraunhofer FOKUS with the objective of providing a compliant implementation of the ETSI NFV specification.

Open Baton is easily extensible. It integrates with OpenStack, and provides a plugin mechanism for supporting additional VIM types. It supports Network Service management either using a generic VNFM or interoperating with VNF-specific VNFM. It uses different mechanisms (REST or PUB/SUB) for interoperating with the VNFMs. It integrates with additional components for the runtime management of a Network Service. For instance, it provides autoscaling and fault management based on monitoring information coming from the the monitoring system available at the NFVI level.

## Source Code and documentation

The Source Code of the other Open Baton projects can be found [here][openbaton-github] and the documentation can be found [here][openbaton-doc] .

## News and Website

Check the [Open Baton Website][openbaton]
Follow us on Twitter @[openbaton][openbaton-twitter].

## Licensing and distribution
Copyright [2015-2016] Open Baton project

Licensed under the Apache License, Version 2.0 (the "License");

you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Support
The Open Baton project provides community support through the Open Baton Public Mailing List and through StackOverflow using the tags openbaton.

## Supported by
  <img src="https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/fokus.png" width="250"/><img src="https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/tu.png" width="150"/>

[fokus-logo]: https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/fokus.png
[openbaton]: http://openbaton.org
[openbaton-doc]: http://openbaton.org/documentation
[openbaton-github]: http://github.org/openbaton
[openbaton-logo]: https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/openBaton.png
[openbaton-mail]: mailto:users@openbaton.org
[openbaton-twitter]: https://twitter.com/openbaton
[tub-logo]: https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/tu.png
