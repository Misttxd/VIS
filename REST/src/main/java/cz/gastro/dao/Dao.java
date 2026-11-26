package cz.gastro.dao;

import java.util.List;

public interface Dao<T> {
    List<T> getAllItems();
    int create(T item);
}
