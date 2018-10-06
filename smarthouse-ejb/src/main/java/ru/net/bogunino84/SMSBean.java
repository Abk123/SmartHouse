package ru.net.bogunino84;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Stateful(name = "SMS_EJB")
@LocalBean
public class SMSBean extends IPDevice {

    /**
     * Логгер
     *
     * @see SMSBean#applog_
     */
    private final static Logger applog_ = LoggerFactory.getLogger(SMSBean.class);

    public SMSBean() {
    }

    @PostConstruct
    void init() {
        super.initDevice("SMS");

        applog_.trace("Проверяем необходимости отправки тестового сообщения и отправляем, если нужно");
        Connection connection = super.getConnection();
        String sql = "SELECT dp.value_string " +
                "FROM device_properties dp " +
                "JOIN devices dv ON dv.dv_id=dp.dv_dv_id" +
                "  AND dv.dv_abbr=? " +
                "JOIN properties pr ON pr.pr_id=dp.pr_pr_id" +
                "  AND pr.pr_abbr=?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, super.getAbbreviation());
            stmt.setString(2, "SELFTEST_ON_INIT");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                applog_.info("Считываем результаты");

                if (rs.getString("value_string").equals("Y")) {
                    SmsCenter smsCenter=new SmsCenter();
                    smsCenter.sendSMS("SMS%20Init%20OK");
                }
            }


            stmt.close();
        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }


    }

    @PreDestroy
    void destroy() {
        super.destroyDevice();
    }


}
