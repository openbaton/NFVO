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

package org.openbaton.nfvo.vim_interfaces.vim;

import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.vim.drivers.interfaces.ClientInterfaces;
import org.openbaton.exceptions.VimException;

/**
 * Created by lto on 20/05/15.
 */
public interface VimBroker {
    void addClient(ClientInterfaces client, String type);

    ClientInterfaces getClient(String type);

    Vim getVim(String type, String name);

    Vim getVim(String type);

    Vim getVim(String type, int port);

    Vim getVim(String type, String name, String port);

    Quota getLeftQuota(VimInstance vimInstance) throws VimException;
}
