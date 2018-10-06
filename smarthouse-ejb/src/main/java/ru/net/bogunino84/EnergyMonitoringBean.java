package ru.net.bogunino84;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class EnergyMonitoringBean {


    /**
     * Логгер
     *
     * @see EnergyMonitoringBean#applog_
     */
    private final static Logger applog_ = LoggerFactory.getLogger(EnergyMonitoringBean.class);

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


    @Resource(lookup = "java:jboss/SMARTDB", type = DataSource.class)
    private DataSource dataSource_;

    @Resource
    private TimerService timerService;

    /**
     * Процедура создания таймеров для работы с Доминатором
     *
     * @see EnergyMonitoringBean#createTimers()
     */
    void createTimers() {

        TimerConfig timerConfig1 = new TimerConfig();
        timerConfig1.setPersistent(false);

        applog_.trace("Выделили память для TimerConfig1");
        timerConfig1.setInfo("CHECK_SERIAL_PORT");
        applog_.trace("Записали аббревиатуру таймера CHECK_SERIAL_PORT");
        ScheduleExpression scheduleExpression1 = new ScheduleExpression();
        applog_.trace("Выделили память для ScheduleExpression1");

        scheduleExpression1.second("0");
        scheduleExpression1.minute("*/1");
        scheduleExpression1.hour("*");
        scheduleExpression1.dayOfWeek("*");
        scheduleExpression1.dayOfMonth("*");
        scheduleExpression1.month("*");
        scheduleExpression1.year("*");

        applog_.info("Создаем таймер");

        timerService.createCalendarTimer(scheduleExpression1, timerConfig1);

        applog_.info("Таймер CHECK_SERIAL_PORT создан");

        TimerConfig timerConfig2 = new TimerConfig();
        timerConfig2.setPersistent(false);

        applog_.trace("Выделили память для TimerConfig2");
        timerConfig2.setInfo("DOMINATOR_STAT");
        applog_.trace("Записали аббревиатуру таймера DOMINATOR_STAT");
        ScheduleExpression scheduleExpression2 = new ScheduleExpression();
        applog_.trace("Выделили память для ScheduleExpression2");

        scheduleExpression2.second("*/5");
        scheduleExpression2.minute("*");
        scheduleExpression2.hour("*");
        scheduleExpression2.dayOfWeek("*");
        scheduleExpression2.dayOfMonth("*");
        scheduleExpression2.month("*");
        scheduleExpression2.year("*");

        applog_.info("Создаем таймер");

        timerService.createCalendarTimer(scheduleExpression2, timerConfig2);

        applog_.info("Таймер DOMINATOR_STAT создан");
    }

    /**
     * Процедура обработки события таймера
     *
     * @param timer Ссылка на таймер класса Timer
     * @see EnergyMonitoringBean#fireTimer(Timer)
     */
    @SuppressWarnings("unused")
    @Timeout
    private void fireTimer(Timer timer) {
        String timerInfo = timer.getInfo().toString();

        applog_.info(String.format("Перешли к выполнению запроса от таймера %s", timerInfo));

        if (timerInfo.equals("DOMINATOR_STAT")) {
            applog_.info("Необходимо обработать ячейки памяти");
            writeStat();
        }

        if (timerInfo.equals("CHECK_SERIAL_PORT")) {
            applog_.info("Необходимо проверить Serial Port");
            checkSerialPort();
        }
    }


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
            sql = "UPDATE devices SET is_active=? WHERE dv_id=?";
            stmt_ = connection_.prepareStatement(sql);
            stmt_.setString(1, yn);
            stmt_.setInt(2, 1);

            stmt_.executeUpdate();

            stmt_.close();

            sql = "UPDATE device_properties SET value_date=sysdate() WHERE dv_dv_id=? AND pr_pr_id=?";
            stmt_ = connection_.prepareStatement(sql);
            stmt_.setInt(1, 1);
            stmt_.setInt(2, 1);

            stmt_.executeUpdate();

            stmt_.close();

        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }

    }

    private void createDominatorPort() {

        String sql = "UPDATE device_properties dp\n" +
                "   SET dp.value_string = ?\n" +
                " WHERE dp.dv_dv_id = 1 AND dp.pr_pr_id = 5";

        applog_.info("Создаем переменную port_");
        port_ = new DominatorSerialPort();

        applog_.info("Инициализируем порт Доминатора");
        if (port_.initDominatorPort()) {
            try {
                PreparedStatement stmt = connection_.prepareStatement(sql);
                stmt.setString(1, port_.getPortName());
                stmt.executeUpdate();

                stmt.close();
                createTimers();
            } catch (SQLException e) {
                applog_.error(e.getLocalizedMessage());
            }
        } else {
            try {
                PreparedStatement stmt = connection_.prepareStatement(sql);
                stmt.setString(1, "");
                stmt.executeUpdate();

                stmt.close();
            } catch (SQLException e) {
                applog_.error(e.getLocalizedMessage());
            }
        }
    }


    //@SuppressWarnings("Duplicates")
    @PostConstruct
    private void init() {

        applog_.info("***************   Идет инициализация класса EnergyMonitoringBean   ******************");



        applog_.info("Осуществляем соединение с базой данных");

        try {

            connection_ = dataSource_.getConnection();


            applog_.info(String.format("Соединение с базой данных выполнено. Схема БД: %s", connection_.getSchema()));

        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }

        createDominatorPort();

    }

    @SuppressWarnings("Duplicates")
    @PreDestroy
    void destroy() {
        applog_.info("Завершение работы класса EnergyMonitoringBean");

        port_.destroyDominatorPort();

        for (Timer timer : timerService.getAllTimers()) {
            timer.cancel();
        }

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


    private void writeStat() {

        applog_.info("Проверяем соединение с базой данных");
        if (connection_ != null) {
            try {
                PreparedStatement statStmt = connection_.prepareStatement("SELECT table_name, hour, date_str, cell_name, cell_address, dmc_id, create_stat_table FROM dm_cells_read_vw");

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

                                sql = String.format("INSERT INTO %s(op_date, stat_value, hour) VALUES(str_to_date(?,'%%d.%%m.%%Y %%H:%%i:%%s'), ?, ?)", tableName);
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

                                stmt_.executeUpdate();

                                stmt_.close();
                            }

                            // Записываем текущее состояние
                            int dmc_id = statRs.getInt("dmc_id");
                            sql = "UPDATE dm_cells SET current_value=?, last_update_time=sysdate() WHERE dmc_id=?";
                            stmt_ = connection_.prepareStatement(sql);
                            stmt_.setInt(1, data);
                            stmt_.setInt(2, dmc_id);
                            stmt_.executeUpdate();

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
