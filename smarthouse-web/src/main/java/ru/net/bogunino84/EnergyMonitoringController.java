package ru.net.bogunino84;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.Enumeration;

/**
 * Контроллер предназначен для оьслуживания запросов с WEB страницы
 *
 * @see EnergyMonitoringController
 */
@WebServlet(name = "EnergyMonitoringController", urlPatterns = "/EnergyMonitoringController")
public class EnergyMonitoringController extends HttpServlet {

    private final static Logger applog_ = LogManager.getLogger(PageNavigateController.class);

    @Resource(lookup = "java:jboss/SMARTDB", type = DataSource.class)
    private DataSource dataSource_;


    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

    }

    @Lock(LockType.READ)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        applog_.trace("Вызвали EnergyMonitoringController");

        Enumeration en = request.getParameterNames();
        while (en.hasMoreElements()) {
            applog_.trace("Получим имя переданного параметра");
            String name = (String) en.nextElement();
            applog_.debug(String.format("Название элемента формы= %s", name));

            if (name.equals("action")) {
                String[] values = request.getParameterValues(name);
                applog_.debug(String.format("value=%s", values[0]));

                try {

                    Connection connection_ = dataSource_.getConnection();



                    switch (values[0]) {
                        case "CELL_UNET_LIVE":
                            try {


                                applog_.info("CELL_UNET_LIVE");


                                String sql = "SELECT ret FROM dm_unet_current_json_vw";
                                PreparedStatement stmt = connection_.prepareStatement(sql);
                                ResultSet r = stmt.executeQuery();
                                applog_.trace("Выполнили SQL записываем результаты");
                                String result = "";
                                while (r.next()) {
                                    applog_.debug(String.format("ret=%s", r.getString("ret")));
                                    result = r.getString("ret");
                                }
                                stmt.close();

                                applog_.trace("Создаем JSON ответ");
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.setHeader("Cache-Control", "no-cache");
                                response.getWriter().write(result);
                                applog_.trace("Выход...");


                            } catch (SQLException e) {
                                applog_.error(e.getLocalizedMessage());
                            }
                            break;
                        case "CELL_PNET_LIVE":
                            try {


                                applog_.info("CELL_PNET_LIVE");


                                String sql = "SELECT ret FROM dm_pnetl_current_json_vw";
                                PreparedStatement stmt = connection_.prepareStatement(sql);
                                ResultSet r = stmt.executeQuery();
                                applog_.trace("Выполнили SQL записываем результаты");
                                String result = "";
                                while (r.next()) {
                                    applog_.debug(String.format("ret=%s", r.getString("ret")));
                                    result = r.getString("ret");
                                }
                                stmt.close();


                                applog_.trace("Создаем JSON ответ");
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.setHeader("Cache-Control", "no-cache");
                                response.getWriter().write(result);
                                applog_.trace("Выход...");


                            } catch (SQLException e) {
                                applog_.error(e.getLocalizedMessage());
                            }
                            break;
                        case "CELL_UACCMED_LIVE":
                            try {


                                applog_.info("CELL_UACCMED_LIVE");


                                String sql = "SELECT ret FROM dm_uaccmed_current_json_vw";
                                PreparedStatement stmt = connection_.prepareStatement(sql);
                                ResultSet r = stmt.executeQuery();
                                applog_.trace("Выполнили SQL записываем результаты");
                                String result = "";
                                while (r.next()) {
                                    applog_.debug(String.format("ret=%s", r.getString("ret")));
                                    result = r.getString("ret");
                                }
                                stmt.close();


                                applog_.trace("Создаем JSON ответ");
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.setHeader("Cache-Control", "no-cache");
                                response.getWriter().write(result);
                                applog_.trace("Выход...");


                            } catch (SQLException e) {
                                applog_.error(e.getLocalizedMessage());
                            }
                            break;
                        case "CELL_ACCTEMP_LIVE":
                            try {


                                applog_.info("CELL_ACCTEMP_LIVE");


                                String sql = "SELECT ret FROM dm_acctemp_current_json_vw";
                                PreparedStatement stmt = connection_.prepareStatement(sql);
                                ResultSet r = stmt.executeQuery();
                                applog_.trace("Выполнили SQL записываем результаты");
                                String result = "";
                                while (r.next()) {
                                    applog_.debug(String.format("ret=%s", r.getString("ret")));
                                    result = r.getString("ret");
                                }
                                stmt.close();


                                applog_.trace("Создаем JSON ответ");
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.setHeader("Cache-Control", "no-cache");
                                response.getWriter().write(result);
                                applog_.trace("Выход...");


                            } catch (SQLException e) {
                                applog_.error(e.getLocalizedMessage());
                            }
                            break;
                        case "CELL_TOPTEMP_LIVE":
                            try {


                                applog_.info("CELL_TOPTEMP_LIVE");


                                String sql = "SELECT ret FROM dm_toptemp_current_json_vw";
                                PreparedStatement stmt = connection_.prepareStatement(sql);
                                ResultSet r = stmt.executeQuery();
                                applog_.trace("Выполнили SQL записываем результаты");
                                String result = "";
                                while (r.next()) {
                                    applog_.debug(String.format("ret=%s", r.getString("ret")));
                                    result = r.getString("ret");
                                }
                                stmt.close();


                                applog_.trace("Создаем JSON ответ");
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.setHeader("Cache-Control", "no-cache");
                                response.getWriter().write(result);
                                applog_.trace("Выход...");


                            } catch (SQLException e) {
                                applog_.error(e.getLocalizedMessage());
                            }
                            break;
                        case "CELL_TRADTEMP_LIVE":
                            try {


                                applog_.info("CELL_TRADTEMP_LIVE");


                                String sql = "SELECT ret FROM dm_tradtemp_current_json_vw";
                                PreparedStatement stmt = connection_.prepareStatement(sql);
                                ResultSet r = stmt.executeQuery();
                                applog_.trace("Выполнили SQL записываем результаты");
                                String result = "";
                                while (r.next()) {
                                    applog_.debug(String.format("ret=%s", r.getString("ret")));
                                    result = r.getString("ret");
                                }
                                stmt.close();


                                applog_.trace("Создаем JSON ответ");
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.setHeader("Cache-Control", "no-cache");
                                response.getWriter().write(result);
                                applog_.trace("Выход...");


                            } catch (SQLException e) {
                                applog_.error(e.getLocalizedMessage());
                            }
                            break;
                        case "DM_DEVICE_INFO":
                            try {


                                applog_.info("DM_DEVICE_INFO");


                                String sql = "SELECT ret FROM dm_device_info_json_vw";
                                PreparedStatement stmt = connection_.prepareStatement(sql);
                                ResultSet r = stmt.executeQuery();
                                applog_.trace("Выполнили SQL записываем результаты");
                                String result = "";
                                while (r.next()) {
                                    applog_.debug(String.format("ret=%s", r.getString("ret")));
                                    result = r.getString("ret");
                                }
                                stmt.close();


                                applog_.trace("Создаем JSON ответ");
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.setHeader("Cache-Control", "no-cache");
                                response.getWriter().write(result);
                                applog_.trace("Выход...");


                            } catch (SQLException e) {
                                applog_.error(e.getLocalizedMessage());
                            }
                            break;
                        case "SMS_DEVICE_INFO":
                            try {


                                applog_.info("SMS_DEVICE_INFO");


                                String sql = "SELECT ret FROM sms_device_info_json_vw";
                                PreparedStatement stmt = connection_.prepareStatement(sql);
                                ResultSet r = stmt.executeQuery();
                                applog_.trace("Выполнили SQL записываем результаты");
                                String result = "";
                                while (r.next()) {
                                    applog_.debug(String.format("ret=%s", r.getString("ret")));
                                    result = r.getString("ret");
                                }
                                stmt.close();


                                applog_.trace("Создаем JSON ответ");
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.setHeader("Cache-Control", "no-cache");
                                response.getWriter().write(result);
                                applog_.trace("Выход...");


                            } catch (SQLException e) {
                                applog_.error(e.getLocalizedMessage());
                            }
                            break;
                        case "DEVICE_ACTIVE":
                            try {


                                applog_.info("DEVICE_ACTIVE");


                                String sql = "SELECT ret FROM device_active_json_vw";
                                PreparedStatement stmt = connection_.prepareStatement(sql);
                                ResultSet r = stmt.executeQuery();
                                applog_.trace("Выполнили SQL записываем результаты");
                                String result = "";
                                while (r.next()) {
                                    applog_.debug(String.format("ret=%s", r.getString("ret")));
                                    result = r.getString("ret");
                                }
                                stmt.close();


                                applog_.trace("Создаем JSON ответ");
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.setHeader("Cache-Control", "no-cache");
                                response.getWriter().write(result);
                                applog_.trace("Выход...");


                            } catch (SQLException e) {
                                applog_.error(e.getLocalizedMessage());
                            }
                            break;
                        case "DEVICE_ENABLE":
                            try {
                                applog_.info("DEVICE_ENABLE");


                                String sql = "SELECT ret FROM device_enable_json_vw";
                                PreparedStatement stmt = connection_.prepareStatement(sql);
                                ResultSet r = stmt.executeQuery();
                                applog_.trace("Выполнили SQL записываем результаты");
                                String result = "";
                                while (r.next()) {
                                    applog_.debug(String.format("ret=%s", r.getString("ret")));
                                    result = r.getString("ret");
                                }
                                stmt.close();


                                applog_.trace("Создаем JSON ответ");
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.setHeader("Cache-Control", "no-cache");
                                response.getWriter().write(result);
                                applog_.trace("Выход...");


                            } catch (SQLException e) {
                                applog_.error(e.getLocalizedMessage());
                            }
                            break;
                        case "STAT":
                            try {

                                applog_.info("STAT");
                                applog_.trace("Читаем следующее значение");
                                if (en.hasMoreElements()) {
                                    name = (String) en.nextElement();
                                    applog_.trace("Следующее значение прочитано");
                                    applog_.debug(String.format("Название элемента формы= %s", name));
                                    if (name.equals("fd")) {
                                        values = request.getParameterValues(name);
                                        String fd = values[0];

                                        name = (String) en.nextElement();
                                        applog_.debug(String.format("Название элемента формы= %s", name));
                                        if (name.equals("td")) {
                                            values = request.getParameterValues(name);
                                            String td = values[0];

                                            name = (String) en.nextElement();
                                            applog_.debug(String.format("Название элемента формы= %s", name));
                                            if (name.equals("type")) {
                                                values = request.getParameterValues(name);
                                                String type = values[0];

                                                applog_.debug(String.format("fd=%s, td=%s, type=%s", fd, td, type));


                                                String sql = String.format("SELECT stat_pkg.get_clob_data('%s', to_date('%s','dd.mm.yyyy'), to_date('%s','dd.mm.yyyy')+1-1/24/3600) ret FROM dual", type, fd, td);

                                                PreparedStatement stmt = connection_.prepareStatement(sql);
                                                ResultSet r = stmt.executeQuery();
                                                applog_.trace("Выполнили SQL записываем результаты");
                                                String result = "";
                                                Clob clobResult = null;
                                                while (r.next()) {
                                                    //applog_.debug(String.format("ret=%s", r.getString("ret")));
                                                    clobResult = r.getClob("ret");
                                                }
                                                stmt.close();

                                                long length = 0;
                                                if (clobResult != null) {
                                                    length = clobResult.length();
                                                }
                                                if (clobResult != null) {
                                                    result = clobResult.getSubString(1, (int) length);
                                                }

                                                applog_.trace("Создаем JSON ответ");
                                                response.setContentType("application/json");
                                                response.setCharacterEncoding("UTF-8");
                                                response.setHeader("Cache-Control", "no-cache");
                                                response.getWriter().write(result);
                                                applog_.trace("Выход...");


                                            }
                                        }
                                    }
                                } else {
                                    applog_.trace("Следующих элементов нет.");
                                }
                            } catch (SQLException e) {
                                applog_.error(e.getLocalizedMessage());
                            }
                            break;
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
