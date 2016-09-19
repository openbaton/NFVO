package org.openbaton.nfvo.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.interfaces.QuotaManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lto on 19/09/16.
 */
@RestController
@RequestMapping("/api/v1/quotas")
public class RestQuota {

  @Autowired private Gson gson;
  @Autowired private QuotaManagement quotaManagement;

  /**
   *
   * @param projectId
   * @return { "total":{quota object}, "left":{quota object} }
   */
  @RequestMapping(method = RequestMethod.GET)
  public JsonObject getAllQuota(@RequestHeader(value = "project-id") String projectId)
      throws VimException, PluginException {
    JsonObject result = new JsonObject();
    result.add(
        "total",
        gson.fromJson(gson.toJson(quotaManagement.getAllQuota(projectId)), JsonObject.class));
    result.add(
        "left",
        gson.fromJson(gson.toJson(quotaManagement.getLeftQuota(projectId)), JsonObject.class));
    return result;
  }
}
