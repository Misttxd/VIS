package cz.gastro.dao;

import java.util.List;

public interface Dao<T> {
    List<T> getAllItems();
    long create(T item);
}
