package cz.rest.dao;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.HashMap;
import java.util.Map;

@Component
@RequestScope
public class IdentityMap {

    private record Key(
            Class<?> type,
            Object id
    ) {}

    private final Map<Key, Object> items = new HashMap<>();

    private final Map<Key, List<Object>> listIds = new HashMap<>();

    public <T> List<T> getList(Class<T> type, Object listKey) {
        List<Object> ids = listIds.get(new Key(type, listKey));
        if (ids == null) {
            return null;
        }
        List<T> result = new ArrayList<>();
        for (Object id : ids) {
            result.add(get(type, id));
        }
        return result;
    }

    public <T> List<T> rememberList(Class<T> type, Object listKey, List<T> rows, Function<T, ?> idOf) {
        List<Object> ids = new ArrayList<>();
        List<T> result = new ArrayList<>();
        for (T row : rows) {
            Object id = idOf.apply(row);
            ids.add(id);
            result.add(remember(type, id, row));
        }
        listIds.put(new Key(type, listKey), ids);
        return result;
    }

    public void forgetList(Class<?> type, Object listKey) {
        listIds.remove(new Key(type, listKey));
    }

    public <T> T get(Class<T> type, Object id) {
        Key key = new Key(type, id);
        return type.cast(items.get(key));
    }

    public <T> void put(Class<T> type, Object id, T item) {
        Key key = new Key(type, id);
        items.put(key, item);
    }

    public <T> T remember(Class<T> type, Object id, T loaded) {
        T existing = get(type, id);
        if (loaded.equals(existing)) {
            return existing;
        }
        put(type, id, loaded);
        return loaded;
    }

    public void remove(Class<?> type, Object id) {
        items.remove(new Key(type, id));
    }
}
