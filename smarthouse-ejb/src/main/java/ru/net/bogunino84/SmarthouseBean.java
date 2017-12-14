package ru.net.bogunino84;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.sql.DataSource;
import java.sql.Connection;
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
            connection_.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            applog_.info(String.format("Соединение с базой данных выполнено. Схема БД: %s", connection_.getSchema()));
        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }

        createTimersForIpDevices();

    }

    /**
     * Процедура создания таймеров для IP устройств Умного Дома
     *
     * @see SmarthouseBean#createTimersForIpDevices()
     */
    private void createTimersForIpDevices(){
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
     * @see SmarthouseBean#fireTimer(Timer)
     * @param timer Ссылка на таймер класса Timer
     */
    @SuppressWarnings("unused")
    @Timeout
    private void fireTimer(Timer timer) {
        applog_.info("Вызвано событие таймера");
        if (timer.getInfo() == hpIloBean.getTimerName()) {
            applog_.info("Необходимо обработать устройство HP_ILO");
            hpIloBean.requestDevice();
        }
        if (timer.getInfo() == wnr3500lBean.getTimerName()) {
            applog_.info("Необходимо обработать устройство WNR3500L");
            wnr3500lBean.requestDevice();
        }
        if (timer.getInfo() == ps18108gBean.getTimerName()) {
            applog_.info("Необходимо обработать устройство PS1810_8G");
            ps18108gBean.requestDevice();
        }
        if (timer.getInfo() == wn2500rpBean.getTimerName()) {
            applog_.info("Необходимо обработать устройство WN2500RP");
            wn2500rpBean.requestDevice();
        }
        if (timer.getInfo() == tlwa850reBean.getTimerName()) {
            applog_.info("Необходимо обработать устройство TL_WA850RE");
            tlwa850reBean.requestDevice();
        }
        if (timer.getInfo() == smsBean.getTimerName()) {
            applog_.info("Необходимо обработать устройство SMS");
            smsBean.requestDevice();
        }
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

}
