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
  * Null -
  */
 NULL (1),
 /**
  * Instantiated - Not Configured
  */
 INITIALIZED (2),

 /**
  * Inactive - Configured
  */
 INACTIVE (3),

 /*
 * Scaling
 */
 SCALING(4),

 /**
  * Active - Configured
  */
 ACTIVE (5),

 /**
  * Terminated
  */
 TERMINATED (6);


 private int value;

 Status(int value) {
  this.value = value;
 }
}
