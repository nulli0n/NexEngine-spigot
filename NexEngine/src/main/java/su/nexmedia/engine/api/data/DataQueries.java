package su.nexmedia.engine.api.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.data.connection.AbstractDataConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataQueries {

    @Nullable
    public static <T> T readData(@NotNull AbstractDataConnector connector,
                                 @NotNull String table,
                                 @NotNull Map<String, String> whereMap,
                                 @NotNull Function<ResultSet, T> dataFunction) {
        return readData(connector, table, Collections.emptyList(), whereMap, dataFunction);
    }

    @Nullable
    public static <T> T readData(@NotNull AbstractDataConnector connector,
                                 @NotNull String table,
                                 @NotNull Collection<String> columnsList,
                                 @NotNull Map<String, String> whereMap,
                                 @NotNull Function<ResultSet, T> dataFunction) {
        List<T> list = readData(connector, table, columnsList, whereMap, dataFunction, 1);
        return list.isEmpty() ? null : list.get(0);
    }

    @NotNull
    public static <T> List<@NotNull T> readData(@NotNull AbstractDataConnector connector,
                                                @NotNull String table,
                                                @NotNull Map<String, String> whereMap,
                                                @NotNull Function<ResultSet, T> dataFunction,
                                                int amount) {
        return readData(connector, table, Collections.emptyList(), whereMap, dataFunction, amount);
    }

    @NotNull
    public static <T> List<@NotNull T> readData(@NotNull AbstractDataConnector connector,
                                                @NotNull String table,
                                                @NotNull Collection<String> columnsList,
                                                @NotNull Map<String, String> whereMap,
                                                @NotNull Function<ResultSet, T> dataFunction,
                                                int amount) {

        String columns = columnsList.isEmpty() ? "*" : columnsList.stream().map(column -> "`" + column + "`").collect(Collectors.joining(", "));
        String wheres = whereMap.keySet().stream().map(column -> "`" + column + "` = ?").collect(Collectors.joining(" AND "));
        String sql = "SELECT " + columns + " FROM " + table + (wheres.isEmpty() ? "" : " WHERE " + wheres);

        return executeQuery(connector, sql, table, whereMap.values(), dataFunction, amount);
    }

    public static <T> void readData(@NotNull AbstractDataConnector connector,
                                    @NotNull String table,
                                    @NotNull Map<String, String> whereMap,
                                    @NotNull Consumer<ResultSet> dataFunction) {
        readData(connector, table, Collections.emptyList(), whereMap, dataFunction);
    }

    public static <T> void readData(@NotNull AbstractDataConnector connector,
                                    @NotNull String table,
                                    @NotNull Collection<String> columnsList,
                                    @NotNull Map<String, String> whereMap,
                                    @NotNull Consumer<ResultSet> dataFunction) {

        String columns = columnsList.isEmpty() ? "*" : columnsList.stream().map(column -> "`" + column + "`").collect(Collectors.joining(", "));
        String wheres = whereMap.keySet().stream().map(column -> "`" + column + "` = ?").collect(Collectors.joining(" AND "));
        String sql = "SELECT " + columns + " FROM " + table + (wheres.isEmpty() ? "" : " WHERE " + wheres);

        executeQuery(connector, sql, table, whereMap.values(), dataFunction);
    }

    public static boolean executeUpdate(@NotNull AbstractDataConnector connector,
                                        @NotNull String table,
                                        @NotNull LinkedHashMap<String, String> valuesMap,
                                        @NotNull Map<String, String> whereMap) {

        String values = valuesMap.keySet().stream().map(entry -> "`" + entry + "` = ?").collect(Collectors.joining(", "));
        String wheres = whereMap.keySet().stream().map(entry -> "`" + entry + "` = ?").collect(Collectors.joining(" AND "));
        String sql = "UPDATE " + table + " SET " + values + (wheres.isEmpty() ? "" : " WHERE " + wheres);

        return executeStatement(connector, sql, valuesMap.values(), whereMap.values());
    }

    public static boolean executeInsert(@NotNull AbstractDataConnector connector,
                                        @NotNull String table,
                                        @NotNull LinkedHashMap<String, String> keys) {

        String columns = keys.keySet().stream().map(column -> "`" + column + "`").collect(Collectors.joining(", "));
        String values = keys.values().stream().map(value -> "?").collect(Collectors.joining(", "));
        String sql = "INSERT INTO " + table + "(" + columns + ") VALUES(" + values + ")";

        return executeStatement(connector, sql, new HashSet<>(), keys.values());
    }

    public static boolean executeDelete(@NotNull AbstractDataConnector connector,
                                        @NotNull String table,
                                        @NotNull Map<String, String> whereMap) {

        String wheres = whereMap.keySet().stream().map(entry -> "`" + entry + "` = ?").collect(Collectors.joining(" AND "));
        String sql = "DELETE FROM " + table + (wheres.isEmpty() ? "" : " WHERE " + wheres);

        return executeStatement(connector, sql, whereMap.values());
    }

    public static boolean executeStatement(@NotNull AbstractDataConnector connector, @NotNull String sql) {
        return executeStatement(connector, sql, Collections.emptySet());
    }

    private static boolean executeStatement(@NotNull AbstractDataConnector connector, @NotNull String sql,
                                            @NotNull Collection<String> values1) {
        return executeStatement(connector, sql, values1, Collections.emptySet());
    }

    private static boolean executeStatement(@NotNull AbstractDataConnector connector, @NotNull String sql,
                                            @NotNull Collection<String> values1, @NotNull Collection<String> values2) {

        try (Connection connection = connector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            int count = 1;
            for (String columnName : values1) {
                statement.setString(count++, columnName);
            }
            for (String columnValue : values2) {
                statement.setString(count++, columnValue);
            }

            statement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @NotNull
    public static <T> List<@NotNull T> executeQuery(@NotNull AbstractDataConnector connector, @NotNull String sql,
                                                    @NotNull String table,
                                                    @NotNull Collection<String> values1,
                                                    @NotNull Function<ResultSet, T> dataFunction,
                                                    int amount) {

        List<T> list = new ArrayList<>();
        try (Connection connection = connector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            int count = 1;
            for (String wValue : values1) {
                statement.setString(count++, wValue);
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next() && (amount < 0 || list.size() < amount)) {
                list.add(dataFunction.apply(resultSet));
            }
            resultSet.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        list.removeIf(Objects::isNull);

        return list;
    }

    public static <T> void executeQuery(@NotNull AbstractDataConnector connector,
                                        @NotNull String sql,
                                        @NotNull String table,
                                        @NotNull Collection<String> values1,
                                        @NotNull Consumer<ResultSet> dataFunction) {

        try (Connection connection = connector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            int count = 1;
            for (String wValue : values1) {
                statement.setString(count++, wValue);
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                dataFunction.accept(resultSet);
            }
            resultSet.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
