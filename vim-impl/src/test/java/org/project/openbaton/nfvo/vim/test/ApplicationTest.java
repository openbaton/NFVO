/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.project.openbaton.nfvo.vim.test;

import org.project.openbaton.clients.interfaces.ClientInterfaces;
import org.project.openbaton.nfvo.vim.broker.VimBroker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

/**
 * Created by lto on 30/04/15.
 */
@Configuration
@ComponentScan(basePackages = {"org.project.openbaton.nfvo.vim"}, basePackageClasses = VimBroker.class)
public class ApplicationTest {

    @Bean
    ClientInterfaces openstackClient(){
        ClientInterfaces clientInterfaces = mock(ClientInterfaces.class);

        return clientInterfaces;
    }

}
