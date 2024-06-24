package uk.iatom.iAtomSys.common.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * Extra information which a developer debugging the VM may need to know, such as function names.
 * This may include assembly labels, C-style function names, and significant developer comments.
 * This does not include Register or Port data, as those have designated endpoints.
 *
 * @param labels
 */
public record DebugSymbols(
    Map<Integer, String> labels,
    Map<Integer, String> functions,
    Map<Integer, String> comments
) {

  private static final Logger logger = LoggerFactory.getLogger(DebugSymbols.class);

  public static DebugSymbols empty() {
    return new DebugSymbols(new HashMap<>(), new HashMap<>(), new HashMap<>());
  }

  public static @Nullable DebugSymbols fromJson(InputStream stream) {
    ObjectMapper mapper = new ObjectMapper();

    try {
      return mapper.readValue(stream, DebugSymbols.class);
    } catch (IOException e) {
      logger.error("Error parsing DebugSymbols JSON", e);
      return null;
    }
  }

  public DebugSymbols takeRelevant(short startAddress, short endAddress) {
    Predicate<Entry<Integer, String>> addressFilter = entry -> entry.getKey() >= startAddress
        && entry.getKey() <= endAddress;
    Collector<Entry<Integer, String>, ?, Map<Integer, String>> mapCollector = Collectors.toMap(
        Entry::getKey, Entry::getValue);
    Function<Map<Integer, String>, Map<Integer, String>> selector = (input) -> input.entrySet()
        .stream().filter(addressFilter).collect(mapCollector);

    Map<Integer, String> labelsRelevant = selector.apply(labels);
    Map<Integer, String> functionsRelevant = selector.apply(functions);
    Map<Integer, String> commentsRelevant = selector.apply(comments);

    return new DebugSymbols(labelsRelevant, functionsRelevant, commentsRelevant);
  }
}
