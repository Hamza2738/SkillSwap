package org.service.Competences;

import java.sql.SQLException;
import java.util.List;

public interface ICRUD<T> {
    void add(T t) throws SQLException;
    void update(T t) throws SQLException;
    void delete(int id) throws SQLException;
    List<T> getAll() throws SQLException;
}