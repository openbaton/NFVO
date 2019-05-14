package org.openbaton.nfvo.security.authentication;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

public class CustomClientDetailsService implements ClientDetailsService {

  private List<BaseClientDetails> clientDetailsRepo;

  public CustomClientDetailsService() {
    clientDetailsRepo = new ArrayList<>();
  }

  @Override
  public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
    for (BaseClientDetails baseClientDetails : clientDetailsRepo)
      if (baseClientDetails.getClientId().equals(clientId)) return baseClientDetails;
    throw new ClientRegistrationException("Invalid clientId: " + clientId);
  }

  public void addclientDetails(BaseClientDetails baseClientDetails) {
    clientDetailsRepo.add(baseClientDetails);
  }
}
