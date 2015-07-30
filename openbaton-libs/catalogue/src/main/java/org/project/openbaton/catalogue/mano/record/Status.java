/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.openbaton.catalogue.mano.record;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
public enum Status {
 /**
  * Instantiated - Not Configured
  */
 INITIAILZED,

 /**
  * Inactive - Configured
  */
 INACTIVE,

 /**
  * Active - Configured
  */
 ACTIVE,

 /**
  * Terminated
  */
 TERMINATED
}
