  <img src="https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/openBaton.png" width="250"/>

  Copyright © 2015-2016 [Open Baton](http://openbaton.org).
  Licensed under [Apache v2 License](http://www.apache.org/licenses/LICENSE-2.0).
  
[![Build Status](https://travis-ci.org/openbaton/NFVO.svg?branch=master)](https://travis-ci.org/openbaton/NFVO)
[![Join the chat at https://gitter.im/openbaton/NFVO](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/openbaton/NFVO?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Documentation Status](https://readthedocs.org/projects/openbaton-docs/badge/?version=stable)](http://openbaton-docs.readthedocs.io/en/stable/?badge=stable)

# Open Baton NFV Orchestrator

Open Baton NFVO is an open source project providing a reference implementation of the NFVO based on the ETSI NFV MANO specification. 

# Getting Started

## Technical Requirements

In order to execute the NFVO you need to have installed:

* Java JRE 7 (or higher)
* RabbitMQ
* (Optional) MySQL

## Setup environment

Execute the following commands:

```bash
sudo mkdir /var/log/openbaton
sudo chown -R $USER: /var/log/openbaton
```

## RabbitMQ configuration

Execute the following commands:

```bash
rabbitmqctl add_user admin openbaton
rabbitmqctl set_user_tags admin administrator
rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"
rabbitmq-plugins enable rabbitmq_management
```
Restart RabbitMQ and ensure you can access it at http://localhost:15672 with username: admin and password: openbaton.

## Set up MySQL

Execute the following commands:

```bash
mysql -uroot -p
create database if not exists openbaton;
GRANT ALL PRIVILEGES ON openbaton.* TO admin@'%' IDENTIFIED BY 'changeme';
```

## Configure Open Baton

The property file is called "openbaton.properties".

### Database

Ensure your properties look like below:

```properties
# Active configurations
spring.datasource.url=jdbc:mysql://localhost:3306/openbaton?useSSL=false
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.show-sql=false
# ddl-auto available values: create-drop, update
spring.jpa.hibernate.ddl-auto=update

# MYSQL configuration (enable it in order to avoid timeout exceptions)
spring.datasource.validationQuery=SELECT 1
spring.datasource.testOnBorrow=true
```

### RabbitMQ

By default RabbitMQ is installed on the host of the NFVO. 
Be aware of the fact that if you want any of your additional external components to be executed on a different host, you will need RabbitMQ to be reachable also from the outside.

In general, also the NFVO can be executed on a different host changing the following property:
```properties
nfvo.rabbit.brokerIp = localhost 
```

to:

```properties
nfvo.rabbit.brokerIp = <the rabbitmq broker ip>
```

### Package Repository

Set the IP of the Package Repository host in the following property:
```properties
nfvo.package-repository.ip=localhost
```

## Start Open Baton

Start Open Baton with this command:

```bash
./bin/openbaton-nfvo start
```
**Note**: launch the openbaton-nfvo with ./bin/openbaton-nfvo (not with ./openbaton-nfvo). Otherwise it won't load the plugins.

After few seconds you can access to the Open Baton GUI at http://localhost:8080

**Note**: you can check out the logs at /var/log/openbaton/openbaton.log
