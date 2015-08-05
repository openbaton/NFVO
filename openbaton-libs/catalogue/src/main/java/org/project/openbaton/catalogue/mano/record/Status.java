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
  * Error
  */
 ERROR(0),
 /**
  * Instantiated - Not Configured
  */
 INITIAILZED (1),

 /**
  * Inactive - Configured
  */
 INACTIVE (2),

 /**
  * Active - Configured
  */
 ACTIVE (3),

 /**
  * Terminated
  */
 TERMINATED (4);

 private int value;

 Status(int value) {
  this.value = value;
 }
}
