package uk.iatom.iAtomSys.client;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.iatom.iAtomSys.common.api.LoadImageRequestPacket;
import uk.iatom.iAtomSys.common.api.StepRequestPacket;
import uk.iatom.iAtomSys.common.api.VMClient;
import uk.iatom.iAtomSys.common.api.VMStateRequestPacket;
import uk.iatom.iAtomSys.common.api.VMStateResponsePacket;

@Component
public class RemoteVMClient implements VMClient {

  private static final Logger logger = LoggerFactory.getLogger(RemoteVMClient.class);

  private final String host;

  public RemoteVMClient(String host) {
    this.host = host;
  }

  @Override
  @Nullable
  public VMStateResponsePacket getState(VMStateRequestPacket packet) {
    URI uri = UriComponentsBuilder.fromHttpUrl(host).path("state").build().toUri();

    try {
      RestTemplate restTemplate = new RestTemplate();
      return restTemplate.postForEntity(uri, packet, VMStateResponsePacket.class).getBody();

    } catch (RestClientException rce) {
      logger.error("Error fetching remote VM state from %s".formatted(uri), rce);
      return null;
    }
  }

  @Override
  @NonNull
  public String step(StepRequestPacket packet) {
    URI uri = UriComponentsBuilder.fromHttpUrl(host).path("command/step").build().toUri();

    try {
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> responseEntity = restTemplate.postForEntity(uri, packet, String.class);
      String body = responseEntity.getBody();
      return body == null ? "<Null response>" : body;

    } catch (RestClientException rce) {
      logger.error("Error executing step command: %s".formatted(uri), rce);
      return "Request error: %s".formatted(rce.getClass().getSimpleName());
    }
  }

  @Override
  @NonNull
  public String loadmem(LoadImageRequestPacket packet) {
    URI uri = UriComponentsBuilder.fromHttpUrl(host).path("command/loadImage").build().toUri();

    try {
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> responseEntity = restTemplate.postForEntity(uri, packet, String.class);
      String body = responseEntity.getBody();
      return body == null ? "<Null response>" : body;

    } catch (RestClientException rce) {
      logger.error("Error executing loadmem command: %s".formatted(uri), rce);
      return "Request error: %s".formatted(rce.getClass().getSimpleName());
    }
  }
}
