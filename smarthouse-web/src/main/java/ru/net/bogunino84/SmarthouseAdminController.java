package ru.net.bogunino84;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Enumeration;

@WebServlet(name = "SmarthouseAdminController", urlPatterns = "/SmarthouseAdminController")
public class SmarthouseAdminController extends HttpServlet {

    private final static Logger applog_ = LoggerFactory.getLogger(SmarthouseAdminController.class);

    @Resource(lookup = "java:jboss/SMARTDB", type = DataSource.class)
    private DataSource dataSource_;

    private Connection connection_;

    //private @EJB
    //SMSBean smsBean;

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
    public void destroy(){
        super.destroy();
        try {
            connection_.close();

        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

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
                                int propertyId = Integer.parseInt(request.getParameterValues("id")[0]);

                                if (propertyId == 3) {
                                    sql = "UPDATE device_properties SET value_number=? " +
                                            "WHERE dv_dv_id=13 AND pr_pr_id=?";
                                } else {
                                    sql = "UPDATE device_properties SET value_string=? " +
                                            "WHERE dv_dv_id=13 AND pr_pr_id=?";
                                }
                                applog_.trace("Подготовили UPDATE");
                                try {
                                    PreparedStatement stmt = connection_.prepareStatement(sql);
                                    stmt.setString(1, propertyValue);
                                    stmt.setInt(2, propertyId);
                                    stmt.executeUpdate();

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
                                applog_.trace("Sending SMS...");
                                SmsCenter smsCenter=new SmsCenter();
                                smsCenter.sendSMS("SMS%20Center%20OK");
                                //smsBean.sendHttpGetRequest("SMS%20Center%20OK");
                                returnMessage = "Запрос на СМС отправлен";
                                result = true;
                                applog_.trace("End sending request");
                                break;
                        }
                    }
                }
            } else {
                returnMessage = "Недостаточно привилегии";
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }
}
