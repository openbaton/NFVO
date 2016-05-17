package org.openbaton.nfvo.api;

import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.core.interfaces.LogManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by lto on 17/05/16.
 */
@RestController
@RequestMapping("/api/v1/logs")
public class RestLogs {

    @Autowired
    private LogManagement logManager;

    @RequestMapping(value = "{nsrId}/vnfrecord/{vnfrName}/hostname/{hostname}", method = RequestMethod.GET)
    public List<String> getLog(@PathVariable("nsrId") String nsrId, @PathVariable("vnfrName") String vnfrName, @PathVariable("hostname") String hostname) throws NotFoundException {

        return logManager.getLog(nsrId, vnfrName, hostname);

    }
}
