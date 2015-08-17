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
 INITIALIZED (1),

 /**
  * Inactive - Configured
  */
 INACTIVE (2),

 /*
 * Scaling
 */
 SCALING(3),

 /**
  * Active - Configured
  */
 ACTIVE (4),

 /**
  * Terminated
  */
 TERMINATED (5);


 private int value;

 Status(int value) {
  this.value = value;
 }
}
