package org.openbaton.nfvo.core.interfaces;

import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.Future;

/**
 * Created by lto on 10/03/16.
 */
public interface EventManagement {
    @Async
    Future<Void> removeUnreachableEndpoints();
}
