package ru.net.bogunino84;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Основной класс для мониторинга электроэнергии
 *
 * @see EnergyMonitoringBean
 */
@Singleton(name = "EnergyMonitoringEJB")
@LocalBean
@Startup
//@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class EnergyMonitoringBean {

    /**
     * Логгер
     *
     * @see EnergyMonitoringBean#applog_
     */
    private final static Logger applog_ = LogManager.getLogger(EnergyMonitoringBean.class);

    /**
     * Максимальная длина страницы
     */
    private static final int MAX_PAGE_LENGTH = 256;

    /**
     * Ссылка на класс последовательного порта Доминатора
     *
     * @see EnergyMonitoringBean
     */

    private DominatorSerialPort port_;


    @Resource(mappedName = "jdbc/SMARTDB", type = DataSource.class)
    private DataSource dataSource_;


    /**
     * Соединение с базой данных
     *
     * @see EnergyMonitoringBean#connection_
     */
    private Connection connection_ = null;


    /**
     * Метод, возвражающий ссылку на соединение с базой данных
     *
     * @return Ссылка на соединение с базой данных
     * @see EnergyMonitoringBean#getConnection()
     */
    public Connection getConnection() {
        return connection_;
    }

    @Schedule(minute = "*/1", hour = "*", info = "Таймер для метода checkSerialPort")
    private void checkSerialPort() {
        String yn;
        if (port_.checkDominatorPort()) {
            yn = "Y";
        } else {
            yn = "N";
        }

        String sql;
        PreparedStatement stmt_;


        try {
            sql = "UPDATE devices SET active=? WHERE dv_id=?";
            stmt_ = connection_.prepareStatement(sql);
            stmt_.setString(1, yn);
            stmt_.setInt(2, 1);

            stmt_.executeQuery();
            stmt_.close();

            sql = "UPDATE device_properties SET value_date=sysdate WHERE dv_id=? AND pr_id=?";
            stmt_ = connection_.prepareStatement(sql);
            stmt_.setInt(1, 1);
            stmt_.setInt(2, 1);

            stmt_.executeQuery();
            stmt_.close();

        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }

    }


    @SuppressWarnings("Duplicates")
    @PostConstruct
    private void init() {

        applog_.info("***************   Идет инициализация класса EnergyMonitoringBean   ******************");
        applog_.info(String.format("Уровень логирования= %s", applog_.getLevel().toString()));


        applog_.info("Осуществляем соединение с базой данных");

        try {

            connection_ = dataSource_.getConnection();
            connection_.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            applog_.info(String.format("Соединение с базой данных выполнено. Схема БД: %s", connection_.getSchema()));

        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }

        String sql = "UPDATE DEVICE_PROPERTIES dp\n" +
                "   SET DP.VALUE_STRING = ?\n" +
                " WHERE DP.DV_ID = 1 AND DP.PR_ID = 5";

        applog_.info("Инициализируем порт Доминатора");
        port_ = new DominatorSerialPort();

        if (port_.initDominatorPort()) {
            try {
                PreparedStatement stmt = connection_.prepareStatement(sql);
                stmt.setString(1, port_.getPortName());
                stmt.executeQuery();
                stmt.close();
            } catch (SQLException e) {
                applog_.error(e.getLocalizedMessage());
            }
        } else {
            try {
                PreparedStatement stmt = connection_.prepareStatement(sql);
                stmt.setString(1, "");
                stmt.executeQuery();
                stmt.close();
            } catch (SQLException e) {
                applog_.error(e.getLocalizedMessage());
            }
        }


    }

    @SuppressWarnings("Duplicates")
    @PreDestroy
    void destroy() {
        applog_.info("Завершение работы класса EnergyMonitoringBean");

        port_.destroyDominatorPort();

        try {
            if (connection_ != null && !connection_.isClosed()) {
                applog_.info(String.format("Закрываем соединение с базой данных. Схема БД: %s", connection_.getSchema()));
                connection_.close();
                applog_.info("Соединение с базой данных закрыто");
            }
        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }

        applog_.info("***************   Работа класса EnergyMonitoringBean завершена   ******************");


    }

    @Schedule(second = "*/5", minute = "*", hour = "*", info = "Таймер для метода writeStat")
    private void writeStat() {

        applog_.info("Проверяем соединение с базой данных");
        if (connection_ != null) {
            try {
                PreparedStatement statStmt = connection_.prepareStatement("SELECT table_name, hour, date_str, cell_name, cell_address, dmc_id, create_stat_table FROM DM_CELLS_READ_VW");

                //while (true) {
                applog_.info("Получаем список ячеек для чтения");
                ResultSet statRs = statStmt.executeQuery();
                while (statRs.next()) {

                    String tableName = statRs.getString("table_name");
                    applog_.debug(String.format("Таблица %s", tableName));

                    applog_.info("Проверяем доступность последовательного порта");
                    if (port_ != null) {
                        int result[] = new int[MAX_PAGE_LENGTH];

                        applog_.info(String.format("Читаем ячейку %s по адресу %s", statRs.getString("cell_name"), statRs.getString("cell_address")));

                        if (port_.readFromMemory(result, Integer.parseInt(statRs.getString("cell_address"), 16), 1) > 0) {
                            applog_.info(String.format("Ячейку %s по адресу %s прочитали. Записываем результат", statRs.getString("cell_name"), statRs.getString("cell_address")));
                            int data = result[1];

                            applog_.debug(String.format("data= %d", data));
                            applog_.debug(String.format("tableName= %s", tableName));

                            String sql;
                            PreparedStatement stmt_;

                            if (statRs.getString("create_stat_table").equals("Y")) {

                                sql = String.format("INSERT INTO %s(op_date, stat_value, hour) VALUES(to_date(?,'dd.mm.yyyy hh24:mi:ss'), ?, ?)", tableName);
                                applog_.debug(String.format("SQL= %s", sql));
                                stmt_ = connection_.prepareStatement(sql);

                                //noinspection JpaQueryApiInspection
                                stmt_.setString(1, statRs.getString("date_str"));
                                applog_.debug(String.format("Значение 1-го параметра: %s", statRs.getString("date_str")));

                                //noinspection JpaQueryApiInspection
                                stmt_.setInt(2, data);
                                applog_.debug(String.format("Значение 2-го параметра: %d", data));

                                //noinspection JpaQueryApiInspection
                                stmt_.setString(3, statRs.getString("hour"));
                                applog_.debug(String.format("Значение 3-го параметра: %s", statRs.getString("hour")));

                                stmt_.executeQuery();
                                stmt_.close();
                            }

                            // Записываем текущее состояние
                            int dmc_id = statRs.getInt("DMC_ID");
                            sql = "UPDATE DM_CELLS SET current_value=?, last_update_time=sysdate WHERE DMC_ID=?";
                            stmt_ = connection_.prepareStatement(sql);
                            stmt_.setInt(1, data);
                            stmt_.setInt(2, dmc_id);
                            stmt_.executeQuery();
                            stmt_.close();
                        }

                    } else {
                        applog_.info("Порт пока не доступен");
                    }

                }
                statStmt.close();
            } catch (SQLException e) {
                applog_.error(e.getLocalizedMessage());
            }
        }
    }
}
