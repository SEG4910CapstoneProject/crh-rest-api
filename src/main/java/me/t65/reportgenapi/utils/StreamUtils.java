package me.t65.reportgenapi.utils;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class StreamUtils {
    public <K, T> Map<K, T> getIdObjectMap(
            Collection<T> tCollection, Function<? super T, ? extends K> keyMapper) {
        return tCollection.stream().collect(Collectors.toMap(keyMapper, Function.identity()));
    }

    public <K, V> Map<K, V> filterEntriesFromMap(
            Map<K, V> map, Predicate<Map.Entry<K, V>> predicate) {
        return map.entrySet().stream()
                .filter(predicate)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
