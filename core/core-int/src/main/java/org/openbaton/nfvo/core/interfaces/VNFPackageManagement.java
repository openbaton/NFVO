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

package org.openbaton.nfvo.core.interfaces;

import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.VimException;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by mpa on 05/05/15.
 */

public interface VNFPackageManagement {

    /**
     * This operation allows submitting and
     * validating the VNF Package.
     *
     * @param pack
     */
    VNFPackage onboard(byte[] pack) throws IOException, VimException, NotFoundException, SQLException;

    /**
     * This operation allows disabling the
     * VNF Package, so that it is not
     * possible to instantiate any further.
     */
    void disable();

    /**
     * This operation allows enabling
     * the VNF Package.
     */
    void enable();

    /**
     * This operation allows updating
     * the VNF Package.
     *
     * @param id
     * @param pack_new
     */
    VNFPackage update(String id, VNFPackage pack_new);

    VNFPackage query(String id);

    /**
     * This operation is used to query
     * information on VNF Packages.
     */
    Iterable<VNFPackage> query();

    /**
     * This operation is used to remove a
     * disabled VNF Package.
     *
     * @param id
     */
    void delete(String id);
}
