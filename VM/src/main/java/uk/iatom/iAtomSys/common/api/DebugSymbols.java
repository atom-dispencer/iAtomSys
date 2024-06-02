package uk.iatom.iAtomSys.common.api;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Extra information which a developer debugging the VM may need to know, such as function names.
 * This may include assembly labels, C-style function names, and significant developer comments.
 * This does not include Register or Port data, as those have designated endpoints.
 *
 * @param labels
 */
public record DebugSymbols(
//    Map<Integer, String> portAddresses,
//    Map<Integer, String> registerAddresses,
    Map<Integer, String> labels,
    Map<Integer, String> functions,
    Map<Integer, String> comments
) {

//  public Map<Integer, String> getReservedAddresses() {
//    Stream<Entry<Integer, String>> combinedStream = Stream.concat(
//        portAddresses.entrySet().stream(),
//        registerAddresses.entrySet().stream()
//    );
//    // TODO What if there's a duplicate entry?
//    return combinedStream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//  }

  public DebugSymbols takeRelevant(short startAddress, short endAddress) {
    Predicate<Entry<Integer, String>> filter = entry -> entry.getKey() >= startAddress && entry.getKey() <= endAddress;
    Collector<Entry<Integer, String>, ?, Map<Integer, String>> map = Collectors.toMap(Entry::getKey, Entry::getValue);
    Function<Map<Integer, String>, Map<Integer, String>> relevantificator = (input) -> input.entrySet().stream().filter(filter).collect(map);

//    Map<Integer, String> portsRelevant = relevantificator.apply(portAddresses);
//    Map<Integer, String> registersRelevant = relevantificator.apply(registerAddresses);
    Map<Integer, String> labelsRelevant = relevantificator.apply(labels);

    return new DebugSymbols( /* portsRelevant, registersRelevant, */ labelsRelevant);
  }

  public static DebugSymbols fromJson() {

  }

}
