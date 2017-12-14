package ru.net.bogunino84;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

@WebServlet(name = "SmarthouseAdminController", urlPatterns = "/SmarthouseAdminController")
public class SmarthouseAdminController extends HttpServlet {

    private final static Logger applog_ = LogManager.getLogger(SmarthouseAdminController.class);

    @Resource(mappedName = "jdbc/SMARTDB_WEB", type = DataSource.class)
    private DataSource dataSource_;

    private Connection connection_;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            connection_ = dataSource_.getConnection();
        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }
    }

    @Override
    public void destroy() {
        try {
            connection_.close();
        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }
        super.destroy();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws  IOException {

        String userPath = request.getServletPath();
        applog_.info(String.format("Метод POST был отправлен на страницу %s", userPath));

        boolean result = false;
        String returnMessage = "Неверный метод";
        String jsonResult;

        if ("/SmarthouseAdminController".equals(userPath)) {
            applog_.trace("Вошли в метод.");
            if (request.isUserInRole("smarthouseadmin")) {
                applog_.trace("Роль соответствует smarthouseadmin.");
                Enumeration en = request.getParameterNames();

                while (en.hasMoreElements()) {

                    applog_.trace("Получим имя переданного параметра");
                    String name = (String) en.nextElement();
                    applog_.debug(String.format("Название элемента формы= %s", name));

                    if (name.equals("method")) {
                        applog_.trace("Получим имя метода");
                        String method = request.getParameterValues("method")[0];

                        applog_.debug(String.format("Метод= %s", method));

                        String sql;

                        switch (method) {
                            case "SET_SMS_ATTR":
                                String propertyValue = request.getParameterValues("value")[0].toUpperCase();
                                Integer propertyId = Integer.valueOf(request.getParameterValues("id")[0]);

                                if (propertyId == 3) {
                                    sql = "UPDATE device_properties SET value_number=? " +
                                            "WHERE dv_id=13 AND pr_id=?";
                                } else {
                                    sql = "UPDATE device_properties SET value_string=? " +
                                            "WHERE dv_id=13 AND pr_id=?";
                                }
                                applog_.trace("Подготовили UPDATE");
                                try {
                                    PreparedStatement stmt = connection_.prepareStatement(sql);
                                    stmt.setString(1, propertyValue);
                                    stmt.setInt(2, propertyId);
                                    stmt.executeQuery();
                                    stmt.close();
                                    returnMessage = "Данные сохранены";
                                    applog_.trace("UPDATE выполнен");
                                    result = true;
                                } catch (SQLException e) {
                                    applog_.error(e.getLocalizedMessage());
                                    returnMessage = e.getLocalizedMessage();
                                }
                                break;
                            case "SET_SMS_SELFTEST":
                                String yn = request.getParameterValues("yn")[0].toUpperCase();
                                sql = "UPDATE device_properties SET value_string=? " +
                                        "WHERE dv_id=13 AND pr_id=10";
                                applog_.trace("Подготовили UPDATE");
                                try {
                                    PreparedStatement stmt = connection_.prepareStatement(sql);
                                    stmt.setString(1, yn);
                                    stmt.executeQuery();
                                    stmt.close();
                                    returnMessage = "Данные сохранены";
                                    applog_.trace("UPDATE выполнен");
                                    result = true;
                                } catch (SQLException e) {
                                    applog_.error(e.getLocalizedMessage());
                                    returnMessage = e.getLocalizedMessage();
                                }
                                break;
                            case "SEND_TEST_SMS":
                                applog_.info("Читаем IP адрес устройства");
                                sql = "SELECT ip, emergency_phone FROM sms_device_info_vw";
                                try {
                                    applog_.trace("Готовим SQL");
                                    PreparedStatement stmt = connection_.prepareStatement(sql);
                                    applog_.trace(String.format("Выполняем SQL: %s", sql));
                                    ResultSet rs = stmt.executeQuery();
                                    applog_.trace("Выполнили SQL. Читаем ответ.");
                                    String ip = "";
                                    String phone = "";
                                    while (rs.next()) {
                                        applog_.trace("Вошли в цикл.");
                                        ip = rs.getString(1);
                                        applog_.trace(String.format("Прочитали IP=%s", ip));
                                        phone = rs.getString(2);
                                        applog_.trace(String.format("Прочитали EMERGENCY_PHONE=%s", phone));
                                    }

                                    applog_.trace("Закрываем SQL");

                                    stmt.close();

                                    applog_.trace("Creating Socket...");
                                    Socket socket = new Socket(ip, 80);

                                    applog_.trace("Creating BufferedWriter...");
                                    BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
                                    applog_.trace("Отправляем команду");
                                    wr.write(String.format("GET /?command=send&mobile=%s&message=%s\r\n", phone, "SMS%20Center%20OK"));
                                    wr.write("\r\n");
                                    wr.flush();
                                    applog_.debug(String.format("Команда: GET /?command=send&mobile=%s&message=%s", phone, "SMS%20Center%20OK"));

                                    returnMessage = "Запрос на СМС отправлен";
                                    result = true;
                                    applog_.trace("End sending request");
                                } catch (SQLException e) {
                                    applog_.error(e.getLocalizedMessage());
                                    returnMessage = e.getLocalizedMessage();
                                }
                        }
                    }
                }
            }
        }

        applog_.trace("Создаем JSON ответ");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        if (result) {
            jsonResult = String.format("{\"result\":\"ok\",\"message\":\"%s\"}", returnMessage);
        } else {
            jsonResult = String.format("{\"result\":\"error\",\"message\":\"%s\"}", returnMessage);
        }
        applog_.debug(String.format("Ответ= %s", jsonResult));
        response.getWriter().write(jsonResult);
        applog_.trace("Выход...");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)  {

    }
}
