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
import java.sql.*;

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
            connection_.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            applog_.info(String.format("Соединение с базой данных выполнено. Схема БД: %s", connection_.getSchema()));
        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }

        // включаем мониторинг
        applog_.info("Включаем мониторинг");
        createTimersForIpDevices();
        createTimersForMonitoring();

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
     * Процедура создания таймеров для IP устройств Умного Дома
     *
     * @see SmarthouseBean#createTimersForIpDevices()
     */
    private void createTimersForIpDevices() {
        hpIloBean.requestDevice();

        applog_.info("Создаем таймер HP_ILO");
        TimerConfig hpIloTimerConfig = new TimerConfig();
        hpIloTimerConfig.setInfo(hpIloBean.getTimerName());
        timerService.createIntervalTimer(hpIloBean.getUpdateTime() * 1000,
                hpIloBean.getUpdateTime() * 1000, hpIloTimerConfig);

        wnr3500lBean.requestDevice();
        applog_.info("Создаем таймер WNR3500L");
        TimerConfig wnr3500lTimerConfig = new TimerConfig();
        wnr3500lTimerConfig.setInfo(wnr3500lBean.getTimerName());
        timerService.createIntervalTimer(wnr3500lBean.getUpdateTime() * 1000,
                wnr3500lBean.getUpdateTime() * 1000, wnr3500lTimerConfig);

        ps18108gBean.requestDevice();
        applog_.info("Создаем таймер PS1810_8G");
        TimerConfig ps18108gTimerConfig = new TimerConfig();
        ps18108gTimerConfig.setInfo(ps18108gBean.getTimerName());
        timerService.createIntervalTimer(ps18108gBean.getUpdateTime() * 1000,
                ps18108gBean.getUpdateTime() * 1000, ps18108gTimerConfig);

        wn2500rpBean.requestDevice();
        applog_.info("Создаем таймер WN2500RP");
        TimerConfig wnr2500rpTimerConfig = new TimerConfig();
        wnr2500rpTimerConfig.setInfo(wn2500rpBean.getTimerName());
        timerService.createIntervalTimer(wn2500rpBean.getUpdateTime() * 1000,
                wn2500rpBean.getUpdateTime() * 1000, wnr2500rpTimerConfig);

        tlwa850reBean.requestDevice();
        applog_.info("Создаем таймер TL_WA850RE");
        TimerConfig tlwa850reTimerConfig = new TimerConfig();
        tlwa850reTimerConfig.setInfo(tlwa850reBean.getTimerName());
        timerService.createIntervalTimer(tlwa850reBean.getUpdateTime() * 1000,
                tlwa850reBean.getUpdateTime() * 1000, tlwa850reTimerConfig);

        smsBean.requestDevice();
        applog_.info("Создаем таймер SMS");
        TimerConfig smsTimerConfig = new TimerConfig();
        smsTimerConfig.setInfo(smsBean.getTimerName());
        timerService.createIntervalTimer(smsBean.getUpdateTime() * 1000,
                smsBean.getUpdateTime() * 1000, smsTimerConfig);
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
        // IP устройства
        applog_.info(String.format("Вызвано событие таймера. INFO=%s", timer.getInfo().toString()));
        if (timer.getInfo() == hpIloBean.getTimerName()) {
            applog_.info("Необходимо обработать устройство HP_ILO");
            hpIloBean.requestDevice();
            return;
        }
        if (timer.getInfo() == wnr3500lBean.getTimerName()) {
            applog_.info("Необходимо обработать устройство WNR3500L");
            wnr3500lBean.requestDevice();
            return;
        }
        if (timer.getInfo() == ps18108gBean.getTimerName()) {
            applog_.info("Необходимо обработать устройство PS1810_8G");
            ps18108gBean.requestDevice();
            return;
        }
        if (timer.getInfo() == wn2500rpBean.getTimerName()) {
            applog_.info("Необходимо обработать устройство WN2500RP");
            wn2500rpBean.requestDevice();
            return;
        }
        if (timer.getInfo() == tlwa850reBean.getTimerName()) {
            applog_.info("Необходимо обработать устройство TL_WA850RE");
            tlwa850reBean.requestDevice();
            return;
        }
        if (timer.getInfo() == smsBean.getTimerName()) {
            applog_.info("Необходимо обработать устройство SMS");
            smsBean.requestDevice();
            return;
        }

        // Устройства умного дома
        String sql = "SELECT * FROM device_monitoring WHERE timer_name=?";

        try {
            applog_.info("Перешли к выполнению запроса");
            PreparedStatement stmt = connection_.prepareStatement(sql);
            stmt.setString(1, timer.getInfo().toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String sqlAlarmOn = clobToString(rs.getClob("SQL_ALARM_ON"));
                String sqlAlarmOff = clobToString(rs.getClob("SQL_ALARM_OFF"));
                String alarmOn = rs.getString("ALARM_ON");
                String isActive = rs.getString("IS_ACTIVE");
                String messageOn = rs.getString("MESSAGE_ON");
                String messageOff = rs.getString("MESSAGE_OFF");
                long id = rs.getLong("ID");

                // В зависимости от состояния таймера выполним запрос
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
                        if (isActive.equals("Y")) {
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
            stmt.close();
        } catch (SQLException e) {
            if (e.getErrorCode() != -1403) {
                applog_.error(e.getLocalizedMessage());
            }
        }

    }

    /**
     * Процедура создания таймеров для мониторинга
     *
     * @see SmarthouseBean#createTimersForMonitoring()
     */
    private void createTimersForMonitoring() {
        applog_.info("Вошли в процедуру создания таймеров для мониторинга");
        String sql = "SELECT * FROM device_monitoring";

        try {
            PreparedStatement stmt = connection_.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                applog_.info(String.format("Создаем таймер %s", rs.getString("TIMER_NAME")));

                TimerConfig timerConfig = new TimerConfig();
                timerConfig.setInfo(rs.getString("TIMER_NAME"));
                // интервал надо из минут перевести в миллисекунды
                timerService.createIntervalTimer(rs.getInt("TIME_INTERVAL") * 60 * 1000,
                        rs.getInt("TIME_INTERVAL") * 60 * 1000, timerConfig);
            }
            stmt.close();
        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }
        applog_.info("Вышли из процедуры создания таймеров для мониторинга");
    }

    /*********************************************************************************************
     * From CLOB to String
     * @param data Clob value
     * @return string representation of clob
     * @see SmarthouseBean#clobToString(Clob)
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
