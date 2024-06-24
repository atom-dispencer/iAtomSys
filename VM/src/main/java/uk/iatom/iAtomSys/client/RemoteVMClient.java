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
import uk.iatom.iAtomSys.common.api.LoadRequestPacket;
import uk.iatom.iAtomSys.common.api.MemoryRequestPacket;
import uk.iatom.iAtomSys.common.api.MemoryResponsePacket;
import uk.iatom.iAtomSys.common.api.PortPacket;
import uk.iatom.iAtomSys.common.api.RegisterPacket;
import uk.iatom.iAtomSys.common.api.SetRequestPacket;
import uk.iatom.iAtomSys.common.api.StepRequestPacket;
import uk.iatom.iAtomSys.common.api.VmClient;
import uk.iatom.iAtomSys.common.api.VmStatus;

@Component
public class RemoteVMClient implements VmClient {

  private static final Logger logger = LoggerFactory.getLogger(RemoteVMClient.class);

  private final String host;

  public RemoteVMClient(String host) {
    this.host = host;
  }

  @Override
  @Nullable
  public VmStatus getStatus() {
    URI uri = UriComponentsBuilder.fromHttpUrl(host).path("state/status").build().toUri();

    try {
      RestTemplate restTemplate = new RestTemplate();
      return restTemplate.getForObject(uri, VmStatus.class);

    } catch (RestClientException rce) {
      logger.error("Error fetching remote VM status from %s".formatted(uri), rce);
      return null;
    }
  }

  @Override
  @Nullable
  public MemoryResponsePacket getMemory(MemoryRequestPacket packet) {
    URI uri = UriComponentsBuilder.fromHttpUrl(host).path("state/memory").build().toUri();

    try {
      RestTemplate restTemplate = new RestTemplate();
      return restTemplate.postForEntity(uri, packet, MemoryResponsePacket.class).getBody();

    } catch (RestClientException rce) {
      logger.error("Error fetching remote VM state from %s".formatted(uri), rce);
      return null;
    }
  }

  @Override
  public RegisterPacket[] getRegisters() {
    URI uri = UriComponentsBuilder.fromHttpUrl(host).path("state/registers").build().toUri();

    try {
      RestTemplate restTemplate = new RestTemplate();
      return restTemplate.getForEntity(uri, RegisterPacket[].class).getBody();

    } catch (RestClientException rce) {
      logger.error("Error fetching remote VM state from %s".formatted(uri), rce);
      return null;
    }
  }

  @Override
  public PortPacket[] getPorts() {
    URI uri = UriComponentsBuilder.fromHttpUrl(host).path("state/ports").build().toUri();

    try {
      RestTemplate restTemplate = new RestTemplate();
      return restTemplate.getForEntity(uri, PortPacket[].class).getBody();

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
  public String loadmem(LoadRequestPacket packet) {
    URI uri = UriComponentsBuilder.fromHttpUrl(host).path("command/load_image").build().toUri();

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

  @Override
  @NonNull
  public String set(SetRequestPacket packet) {
    URI uri = UriComponentsBuilder.fromHttpUrl(host).path("command/set").build().toUri();

    try {
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> responseEntity = restTemplate.postForEntity(uri, packet, String.class);
      String body = responseEntity.getBody();
      return body == null ? "<Null response>" : body;

    } catch (RestClientException rce) {
      logger.error("Error executing set command: %s".formatted(uri), rce);
      return "Request error: %s".formatted(rce.getClass().getSimpleName());
    }
  }
}
