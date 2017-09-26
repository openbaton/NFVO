package org.openbaton.nfvo.api.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/error")
public class ErrorRequestHandler {

  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Autowired private Gson gson;

  @RequestMapping(value = "errors", method = RequestMethod.GET)
  public JsonObject renderErrorPage(HttpServletRequest httpRequest) {

    String errorMsg = "";
    int httpErrorCode = (Integer) httpRequest.getAttribute("javax.servlet.error.status_code");
    ;

    switch (httpErrorCode) {
      case 400:
        {
          errorMsg = "Http Error Code: 400. Bad Request";
          break;
        }
      case 401:
        {
          errorMsg = "Http Error Code: 401. Unauthorized";
          break;
        }
      case 404:
        {
          errorMsg = "Http Error Code: 404. Resource not found";
          break;
        }
      case 500:
        {
          errorMsg = "Http Error Code: 500. Internal Server Error";
          break;
        }
    }
    JsonObject errorPage = new JsonObject();
    errorPage.addProperty("errorMsg", errorMsg);
    return errorPage;
  }
}
