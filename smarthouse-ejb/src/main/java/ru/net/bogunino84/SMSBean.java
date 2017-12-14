package ru.net.bogunino84;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import java.io.*;
import java.net.Socket;
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
    private final static Logger applog_ = LogManager.getLogger(SMSBean.class);

    public SMSBean() {
    }

    @PostConstruct
    void init() {
        super.initDevice("SMS");

        applog_.trace("Проверяем необходимости отправки тестового сообщения и отправляем, если нужно");
        Connection connection = super.getConnection();
        String sql = "SELECT dp.value_string " +
                "FROM device_properties dp " +
                "JOIN devices dv ON dv.dv_id=dp.dv_id" +
                "  AND dv.dv_abbr=? " +
                "JOIN properties pr ON pr.pr_id=dp.pr_id" +
                "  AND pr.pr_abbr=?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, super.getAbbreviation());
            stmt.setString(2, "SELFTEST_ON_INIT");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                applog_.info("Считываем результаты");

                if (rs.getString("value_string").equals("Y")) {
                    sendHttpGetRequest("SMS%20Init%20OK");
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

    public void sendHttpGetRequest(String message) {
        applog_.trace("Starting SMS...");

        try {
            applog_.trace("Определяем номер телефона, на который будем отправлять SMS");
            Connection connection = super.getConnection();
            String sql = "SELECT dp.value_string " +
                    "FROM device_properties dp " +
                    "JOIN devices dv ON dv.dv_id=dp.dv_id" +
                    "  AND dv.dv_abbr=? " +
                    "JOIN properties pr ON pr.pr_id=dp.pr_id" +
                    "  AND pr.pr_abbr=?";

            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, super.getAbbreviation());
            stmt.setString(2, "EMERGENCY_PHONE");
            ResultSet rs = stmt.executeQuery();
            String emergencyPhone = "";
            while (rs.next()) {
                applog_.info("Считываем результаты");
                emergencyPhone = rs.getString("value_string");
            }
            stmt.close();

            stmt = connection.prepareStatement(sql);
            stmt.setString(1, super.getAbbreviation());
            stmt.setString(2, "IP");
            rs = stmt.executeQuery();
            String ipAddress = "";
            while (rs.next()) {
                applog_.info("Считываем результаты");
                ipAddress = rs.getString("value_string");
            }
            stmt.close();


            applog_.trace("Creating Socket...");
            Socket socket = new Socket(ipAddress, 80);

            applog_.trace("Creating BufferedWriter...");
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
            wr.write(String.format("GET /?command=send&mobile=%s&message=%s\r\n", emergencyPhone, message));

            wr.write("\r\n");
            wr.flush();
            applog_.trace("End sending request");

            applog_.trace("Creating BufferedReader...");
            BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                applog_.debug(line);
            }
            wr.close();
            rd.close();
        } catch (IOException e) {
            applog_.error(e.getLocalizedMessage());
        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }

        applog_.trace("End SMS.");
    }
}
