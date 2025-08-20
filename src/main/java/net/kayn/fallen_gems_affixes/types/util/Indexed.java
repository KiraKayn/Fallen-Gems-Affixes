package net.kayn.fallen_gems_affixes.types.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public interface Indexed<T> extends Supplier<T> {
    int getId();

    T get();

    boolean isPresent();

    static <T> Indexed<T> simple(int index, T value) {return new SimpleIndexed<>(index, value);}

    public record SimpleIndexed<T>(int index, T value) implements Indexed<T>{

        @Override
        public int getId() {
            return this.index;
        }

        @Override
        public T get() {
            return this.value;
        }

        @Override
        public boolean isPresent() {
            return this.get() != null;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SimpleIndexed(int index1, Object value1))) return false;
            return this.index == index1 && Objects.equals(this.value, value1);
        }

        @Override
        public int hashCode() {
            if(this.value == null) return 31 * index;
            return 31 * index + value.hashCode();
        }

        @Override
        public @NotNull String toString() {
            if(!this.isPresent()) return String.valueOf(index);
            return value.toString() + index;
        }
    }
}
