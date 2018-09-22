package ru.net.bogunino84;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Основной класс Умный Дом
 *
 * @see SmarthouseBean
 */
@Singleton(name = "SmarthouseEJB")
@LocalBean
//@Startup
public class SmarthouseBean {
    public SmarthouseBean() {
    }


    @Resource
    private TimerService timerService;

    @EJB
    private HP_ILO_Bean hpIloBean;

    @EJB
    private WNR3500L_Bean wnr3500lBean;

    @EJB
    private PS1810_8G_Bean ps18108gBean;

    @EJB
    private WN2500RP_Bean wn2500rpBean;

    @EJB
    private TL_WA850RE_Bean tlwa850reBean;

    @EJB
    private SMSBean smsBean;

    /**
     * Соединение с базой данных
     *
     * @see SmarthouseBean#connection_
     */
    private Connection connection_ = null;

    /**
     * Логгер
     *
     * @see SmarthouseBean#applog_
     */
    private final static Logger applog_ = LogManager.getLogger(SmarthouseBean.class);

    @Resource(lookup = "java:jboss/SMARTDB", type = DataSource.class)
    private DataSource dataSource_;

    @SuppressWarnings("Duplicates")
    @PostConstruct
    private void init() {

        applog_.info("***************   Идет инициализация класса SmarthouseBean   ******************");
        applog_.info(String.format("Уровень логирования= %s", applog_.getLevel().toString()));

        applog_.info("Осуществляем соединение с базой данных");

        try {
            connection_ = dataSource_.getConnection();

            //connection_.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            applog_.info(String.format("Соединение с базой данных выполнено. Схема БД: %s", connection_.getSchema()));
        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }

        // включаем мониторинг
        applog_.info("Включаем мониторинг");
        createTimers();

    }


    @SuppressWarnings("Duplicates")
    @PreDestroy
    private void destroy() {
        applog_.info("Завершение работы класса SmarthouseBean");

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

        applog_.info("***************   Работа класса SmarthouseBean завершена   ******************");
    }


    /**
     * Процедура создания таймеров
     *
     * @see SmarthouseBean#createTimers()
     */
    private void createTimers() {
        String sql = "SELECT \n" +
                "tm_id, abbreviation, attr_second, \n" +
                "   attr_minute, attr_hour, attr_dayofweek, \n" +
                "   attr_dayofmonth, attr_month, attr_year, \n" +
                "   is_active\n" +
                "FROM timer_properties\n" +
                "WHERE is_active='Y'";

        try {
            applog_.info("Перешли к выполнению запроса");
            PreparedStatement stmt = connection_.prepareStatement(sql);
            applog_.info("Читаем результаты");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                applog_.info(String.format("Создаем таймер %s", rs.getString("abbreviation")));

                TimerConfig timerConfig = new TimerConfig();
                timerConfig.setPersistent(false);

                applog_.trace("Выделили память для TimerConfig");
                timerConfig.setInfo(rs.getString("abbreviation"));
                applog_.trace("Записали Info");
                ScheduleExpression scheduleExpression = new ScheduleExpression();
                applog_.trace("Выделили память для ScheduleExpression");

                if (rs.getString("attr_second") != null && (!(rs.getString("attr_second").isEmpty()))) {
                    scheduleExpression.second(rs.getString("attr_second"));
                    applog_.info(String.format("Записали атрибут ATTR_SECOND: %s", rs.getString("attr_second")));
                }
                if (rs.getString("attr_minute") != null && (!(rs.getString("attr_minute").isEmpty()))) {
                    scheduleExpression.minute(rs.getString("attr_minute"));
                    applog_.info(String.format("Записали атрибут ATTR_MINUTE: %s", rs.getString("attr_minute")));
                }
                if (rs.getString("attr_hour") != null && (!(rs.getString("attr_hour").isEmpty()))) {
                    scheduleExpression.hour(rs.getString("attr_hour"));
                    applog_.info(String.format("Записали атрибут ATTR_HOUR: %s", rs.getString("attr_hour")));
                }
                if (rs.getString("attr_dayofweek") != null && (!(rs.getString("attr_dayofweek").isEmpty()))) {
                    scheduleExpression.dayOfWeek(rs.getString("attr_dayofweek"));
                    applog_.info(String.format("Записали атрибут ATTR_DAYOFWEEK: %s", rs.getString("attr_dayofweek")));
                }
                if (rs.getString("attr_dayofmonth") != null && (!(rs.getString("attr_dayofmonth").isEmpty()))) {
                    scheduleExpression.dayOfMonth(rs.getString("attr_dayofmonth"));
                    applog_.info(String.format("Записали атрибут ATTR_DAYOFMONTH: %s", rs.getString("attr_dayofmonth")));
                }
                if (rs.getString("attr_month") != null && (!(rs.getString("attr_month").isEmpty()))) {
                    scheduleExpression.month(rs.getString("attr_month"));
                    applog_.info(String.format("Записали атрибут ATTR_MONTH: %s", rs.getString("attr_month")));
                }
                if (rs.getString("attr_year") != null && (!(rs.getString("attr_year").isEmpty()))) {
                    scheduleExpression.year(rs.getString("attr_year"));
                    applog_.info(String.format("Записали атрибут ATTR_YEAR: %s", rs.getString("attr_year")));
                }

                applog_.info("Создаем таймер");

                timerService.createCalendarTimer(scheduleExpression, timerConfig);

                applog_.info(String.format("Таймер %s создан", rs.getString("abbreviation")));
            }

            stmt.close();

        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }
    }

    /**
     * Процедура обработки события таймера
     *
     * @param timer Ссылка на таймер класса Timer
     * @see SmarthouseBean#fireTimer(Timer)
     */
    @SuppressWarnings("unused")
    @Timeout
    private void fireTimer(Timer timer) {

        String sql = "SELECT tp.abbreviation,\n" +
                "       tu.dm_dm_id,\n" +
                "       tu.dv_dv_id,\n" +
                "       dvc.dv_abbr,\n" +
                "       dm.sql_alarm_on,\n" +
                "       dm.sql_alarm_off,\n" +
                "       dm.alarm_on,\n" +
                "       dm.is_active,\n" +
                "       dm.message_on,\n" +
                "       dm.message_off\n" +
                "  FROM timer_properties  tp\n" +
                "       JOIN timer_usages tu ON tu.tm_tm_id = tp.tm_id AND tu.is_active = 'Y'\n" +
                "       LEFT JOIN devices dvc ON dvc.dv_id = tu.dv_dv_id AND dvc.is_enabled = 'Y'\n" +
                "       LEFT JOIN device_monitoring dm\n" +
                "           ON dm.id = tu.dm_dm_id AND dm.is_active = 'Y'\n" +
                " WHERE tp.abbreviation = ? AND tp.is_active = 'Y'";

        try {
            applog_.info(String.format("Перешли к выполнению запроса от таймера %s", timer.getInfo().toString()));
            PreparedStatement stmt = connection_.prepareStatement(sql);
            stmt.setString(1, timer.getInfo().toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                applog_.trace("Вошли в цикл");
                // IP устройства
                if (rs.getLong("dv_dv_id") > 0) {
                    if (rs.getString("dv_abbr").equals(hpIloBean.getAbbreviation())) {
                        applog_.info("Необходимо обработать устройство HP_ILO");
                        hpIloBean.requestDevice();
                    }
                    if (rs.getString("dv_abbr").equals(wnr3500lBean.getAbbreviation())) {
                        applog_.info("Необходимо обработать устройство WNR3500L");
                        wnr3500lBean.requestDevice();
                    }
                    if (rs.getString("dv_abbr").equals(ps18108gBean.getAbbreviation())) {
                        applog_.info("Необходимо обработать устройство PS1810_8G");
                        ps18108gBean.requestDevice();
                    }
                    if (rs.getString("dv_abbr").equals(wn2500rpBean.getAbbreviation())) {
                        applog_.info("Необходимо обработать устройство WN2500RP");
                        wn2500rpBean.requestDevice();
                    }
                    if (rs.getString("dv_abbr").equals(tlwa850reBean.getAbbreviation())) {
                        applog_.info("Необходимо обработать устройство TL_WA850RE");
                        tlwa850reBean.requestDevice();
                    }
                    if (rs.getString("dv_abbr").equals(smsBean.getAbbreviation())) {
                        applog_.info("Необходимо обработать устройство SMS");
                        smsBean.requestDevice();
                    }
                }

                // Мониторинг
                if (rs.getLong("dm_dm_id") > 0) {
                    applog_.trace("Выполняем мониторинг");
                    String sqlAlarmOn = clobToString(rs.getClob("sql_alarm_on"));
                    String sqlAlarmOff = clobToString(rs.getClob("sql_alarm_off"));
                    String alarmOn = rs.getString("alarm_on");
                    String isActive = rs.getString("is_active");
                    String messageOn = rs.getString("message_on");
                    String messageOff = rs.getString("message_off");
                    long id = rs.getLong("dm_dm_id");

                    // В зависимости от состояния таймера выполним запрос
                    applog_.trace("Проверяем текущее состояние таймера");
                    String message;
                    if (alarmOn.equals("Y")) {
                        sql = sqlAlarmOff;
                        message = messageOff;
                        applog_.info("Текущее состояние = Y");
                    } else {
                        sql = sqlAlarmOn;
                        message = messageOn;
                        applog_.info("Текущее состояние = N");
                    }

                    applog_.debug(String.format("Выполняем SQL=%s", sql));
                    PreparedStatement stmt1 = connection_.prepareStatement(sql);
                    ResultSet rs1 = stmt1.executeQuery();
                    while (rs1.next()) {
                        applog_.debug(String.format("Результат выполнения запроса=%d", rs1.getInt(1)));
                        if (rs1.getInt(1) > 0) {
                            applog_.info(String.format("Отправляем СМС=%s", message));
                            smsBean.sendHttpGetRequest(message);

                            applog_.info("Выполняем update");
                            if (alarmOn.equals("Y")) {
                                sql = "UPDATE device_monitoring SET alarm_on='N', op_date=sysdate() WHERE id=?";
                            } else {
                                sql = "UPDATE device_monitoring SET alarm_on='Y', op_date=sysdate() WHERE id=?";
                            }

                            PreparedStatement stmt2 = connection_.prepareStatement(sql);
                            stmt2.setLong(1, id);
                            stmt2.executeUpdate();

                            stmt2.close();
                        }
                    }
                    stmt1.close();

                }

            }
            stmt.close();
        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }
    }


    /*********************************************************************************************
     * From CLOB to String
     * @param data Clob value
     * @return string representation of clob
     * @see SmarthouseBean#clobToString(java.sql.Clob)
     *********************************************************************************************/
    private String clobToString(java.sql.Clob data) {
        final StringBuilder sb = new StringBuilder();

        try {
            final Reader reader = data.getCharacterStream();
            final BufferedReader br = new BufferedReader(reader);

            int b;
            while (-1 != (b = br.read())) {
                sb.append((char) b);
            }

            br.close();
        } catch (SQLException e) {
            applog_.error("SQL. Could not convert CLOB to string", e);
            return e.toString();
        } catch (IOException e) {
            applog_.error("IO. Could not convert CLOB to string", e);
            return e.toString();
        }

        return sb.toString();
    }


}
