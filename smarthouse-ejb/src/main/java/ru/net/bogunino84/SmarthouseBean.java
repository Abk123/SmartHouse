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
@Startup
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

    @Resource(mappedName = "jdbc/SMARTDB", type = DataSource.class)
    private DataSource dataSource_;

    @SuppressWarnings("Duplicates")
    @PostConstruct
    private void init() {

        applog_.info("***************   Идет инициализация класса SmarthouseBean   ******************");
        applog_.info(String.format("Уровень логирования= %s", applog_.getLevel().toString()));

        applog_.info("Осуществляем соединение с базой данных");

        try {
            connection_ = dataSource_.getConnection();
            connection_.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
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
                "TM_ID, ABBREVIATION, ATTR_SECOND, \n" +
                "   ATTR_MINUTE, ATTR_HOUR, ATTR_DAYOFWEEK, \n" +
                "   ATTR_DAYOFMONTH, ATTR_MONTH, ATTR_YEAR, \n" +
                "   IS_ACTIVE\n" +
                "FROM SMART.TIMER_PROPERTIES\n" +
                "WHERE IS_ACTIVE='Y'";

        try {
            applog_.info("Перешли к выполнению запроса");
            PreparedStatement stmt = connection_.prepareStatement(sql);
            applog_.info("Читаем результаты");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                applog_.info(String.format("Создаем таймер %s", rs.getString("ABBREVIATION")));

                TimerConfig timerConfig = new TimerConfig();

                applog_.trace("Выделили память для TimerConfig");
                timerConfig.setInfo(rs.getString("ABBREVIATION"));
                applog_.trace("Записали Info");
                ScheduleExpression scheduleExpression = new ScheduleExpression();
                applog_.trace("Выделили память для ScheduleExpression");

                if (rs.getString("ATTR_SECOND") != null && (!(rs.getString("ATTR_SECOND").isEmpty()))) {
                    scheduleExpression.second(rs.getString("ATTR_SECOND"));
                    applog_.info(String.format("Записали атрибут ATTR_SECOND: %s", rs.getString("ATTR_SECOND")));
                }
                if (rs.getString("ATTR_MINUTE") != null && (!(rs.getString("ATTR_MINUTE").isEmpty()))) {
                    scheduleExpression.minute(rs.getString("ATTR_MINUTE"));
                    applog_.info(String.format("Записали атрибут ATTR_MINUTE: %s", rs.getString("ATTR_MINUTE")));
                }
                if (rs.getString("ATTR_HOUR") != null && (!(rs.getString("ATTR_HOUR").isEmpty()))) {
                    scheduleExpression.hour(rs.getString("ATTR_HOUR"));
                    applog_.info(String.format("Записали атрибут ATTR_HOUR: %s", rs.getString("ATTR_HOUR")));
                }
                if (rs.getString("ATTR_DAYOFWEEK") != null && (!(rs.getString("ATTR_DAYOFWEEK").isEmpty()))) {
                    scheduleExpression.dayOfWeek(rs.getString("ATTR_DAYOFWEEK"));
                    applog_.info(String.format("Записали атрибут ATTR_DAYOFWEEK: %s", rs.getString("ATTR_DAYOFWEEK")));
                }
                if (rs.getString("ATTR_DAYOFMONTH") != null && (!(rs.getString("ATTR_DAYOFMONTH").isEmpty()))) {
                    scheduleExpression.dayOfMonth(rs.getString("ATTR_DAYOFMONTH"));
                    applog_.info(String.format("Записали атрибут ATTR_DAYOFMONTH: %s", rs.getString("ATTR_DAYOFMONTH")));
                }
                if (rs.getString("ATTR_MONTH") != null && (!(rs.getString("ATTR_MONTH").isEmpty()))) {
                    scheduleExpression.month(rs.getString("ATTR_MONTH"));
                    applog_.info(String.format("Записали атрибут ATTR_MONTH: %s", rs.getString("ATTR_MONTH")));
                }
                if (rs.getString("ATTR_YEAR") != null && (!(rs.getString("ATTR_YEAR").isEmpty()))) {
                    scheduleExpression.year(rs.getString("ATTR_YEAR"));
                    applog_.info(String.format("Записали атрибут ATTR_YEAR: %s", rs.getString("ATTR_YEAR")));
                }

                applog_.info("Создаем таймер");

                timerService.createCalendarTimer(scheduleExpression, timerConfig);

                applog_.info(String.format("Таймер %s создан", rs.getString("ABBREVIATION")));
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

        String sql = "SELECT tp.ABBREVIATION,\n" +
                "       tu.DM_ID,\n" +
                "       tu.DV_ID,\n" +
                "       dvc.DV_ABBR,\n" +
                "       dm.SQL_ALARM_ON,\n" +
                "       dm.SQL_ALARM_OFF,\n" +
                "       dm.ALARM_ON,\n" +
                "       dm.IS_ACTIVE,\n" +
                "       dm.MESSAGE_ON,\n" +
                "       dm.MESSAGE_OFF\n" +
                "  FROM TIMER_PROPERTIES  tp\n" +
                "       JOIN TIMER_USAGES tu ON tu.TM_ID = tp.TM_ID AND tu.IS_ACTIVE = 'Y'\n" +
                "       LEFT JOIN DEVICES dvc ON dvc.DV_ID = tu.DV_ID AND dvc.ENABLED = 'Y'\n" +
                "       LEFT JOIN DEVICE_MONITORING dm\n" +
                "           ON dm.ID = tu.DM_ID AND dm.IS_ACTIVE = 'Y'\n" +
                " WHERE tp.ABBREVIATION = ? AND tp.IS_ACTIVE = 'Y'";

        try {
            applog_.info(String.format("Перешли к выполнению запроса от таймера %s", timer.getInfo().toString()));
            PreparedStatement stmt = connection_.prepareStatement(sql);
            stmt.setString(1, timer.getInfo().toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                applog_.trace("Вошли в цикл");
                // IP устройства
                if (rs.getLong("DV_ID") > 0) {
                    if (rs.getString("DV_ABBR").equals(hpIloBean.getAbbreviation())) {
                        applog_.info("Необходимо обработать устройство HP_ILO");
                        hpIloBean.requestDevice();
                    }
                    if (rs.getString("DV_ABBR").equals(wnr3500lBean.getAbbreviation())) {
                        applog_.info("Необходимо обработать устройство WNR3500L");
                        wnr3500lBean.requestDevice();
                    }
                    if (rs.getString("DV_ABBR").equals(ps18108gBean.getAbbreviation())) {
                        applog_.info("Необходимо обработать устройство PS1810_8G");
                        ps18108gBean.requestDevice();
                    }
                    if (rs.getString("DV_ABBR").equals(wn2500rpBean.getAbbreviation())) {
                        applog_.info("Необходимо обработать устройство WN2500RP");
                        wn2500rpBean.requestDevice();
                    }
                    if (rs.getString("DV_ABBR").equals(tlwa850reBean.getAbbreviation())) {
                        applog_.info("Необходимо обработать устройство TL_WA850RE");
                        tlwa850reBean.requestDevice();
                    }
                    if (rs.getString("DV_ABBR").equals(smsBean.getAbbreviation())) {
                        applog_.info("Необходимо обработать устройство SMS");
                        smsBean.requestDevice();
                    }
                }

                // Мониторинг
                if (rs.getLong("DM_ID") > 0) {
                    applog_.trace("Выполняем мониторинг");
                    String sqlAlarmOn = clobToString(rs.getClob("SQL_ALARM_ON"));
                    String sqlAlarmOff = clobToString(rs.getClob("SQL_ALARM_OFF"));
                    String alarmOn = rs.getString("ALARM_ON");
                    String isActive = rs.getString("IS_ACTIVE");
                    String messageOn = rs.getString("MESSAGE_ON");
                    String messageOff = rs.getString("MESSAGE_OFF");
                    long id = rs.getLong("DM_ID");

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
                                sql = "UPDATE DEVICE_MONITORING SET ALARM_ON='N', OP_DATE=sysdate WHERE ID=?";
                            } else {
                                sql = "UPDATE DEVICE_MONITORING SET ALARM_ON='Y', OP_DATE=sysdate WHERE ID=?";
                            }

                            PreparedStatement stmt2 = connection_.prepareStatement(sql);
                            stmt2.setLong(1, id);
                            stmt2.executeQuery();
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
