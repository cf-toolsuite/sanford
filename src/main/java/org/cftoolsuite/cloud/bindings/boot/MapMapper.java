package org.cftoolsuite.cloud.bindings.boot;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

// @see https://github.com/spring-cloud/spring-cloud-bindings/blob/main/spring-cloud-bindings/src/main/java/org/springframework/cloud/bindings/boot/MapMapper.java
final class MapMapper {

    private final Map<String, String> source;

    private final Map<String, Object> destination;

    MapMapper(Map<String, String> source, Map<String, Object> destination) {
        this.source = source;
        this.destination = destination;
    }

    Source from(String... keys) {
        return new SourceImpl(keys);
    }

    interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    interface Source {
        void to(String key);

        void toIfAbsent(String key);

        void to(String key, Function<String, Object> function);

        void to(String key, TriFunction<String, String, String, Object> function);

        Source when(Predicate<Object> predicate);

    }

    final class SourceImpl implements Source {

        private final String[] keys;

        private SourceImpl(String[] keys) {
            this.keys = keys;
        }

        @Override
        public void to(String key) {
            to(key, v -> v);
        }

        @Override
        public void toIfAbsent(String key) {
            if (destination.containsKey(key)) {
                return;
            }
            to(key, v -> v);
        }

        @Override
        public void to(String key, Function<String, Object> function) {
            if (keys.length != 1) {
                throw new IllegalStateException(
                        String.format("source size %d cannot be transformed as one argument", keys.length));
            }

            if (!Arrays.stream(keys).allMatch(source::containsKey)) {
                return;
            }

            destination.put(key, function.apply(source.get(keys[0])));
        }

        @Override
        public void to(String key, TriFunction<String, String, String, Object> function) {
            if (keys.length != 3) {
                throw new IllegalStateException(
                        String.format("source size %d cannot be consumed as three arguments", keys.length));
            }

            if (!Arrays.stream(keys).allMatch(source::containsKey)) {
                return;
            }

            destination.put(key, function.apply(source.get(keys[0]), source.get(keys[1]), source.get(keys[2])));
        }

        @Override
        public Source when(Predicate<Object> predicate) {
            if (keys.length != 1) {
                throw new IllegalStateException(
                        String.format("source size %d cannot be transformed as one argument", keys.length));
            }

            if (predicate.test(source.get(keys[0]))) {
                return this;
            } else {
                return new NoopSource();
            }
        }
    }

    final static class NoopSource implements Source {

        @Override
        public void to(String key) {

        }

        @Override
        public void toIfAbsent(String key) {

        }

        @Override
        public void to(String key, Function<String, Object> function) {

        }

        @Override
        public void to(String key, TriFunction<String, String, String, Object> function) {

        }

        @Override
        public Source when(Predicate<Object> predicate) {
            return this;
        }
    }
}
