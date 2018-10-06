package ru.net.bogunino84;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Контроллер для выполнения навигации между страницами
 *
 * @see PageNavigateController
 */

@WebServlet(name = "PageNavigateController", urlPatterns = "/PageNavigateController")
public class PageNavigateController extends HttpServlet {

    private final static Logger applog_ = LoggerFactory.getLogger(PageNavigateController.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String userPath = request.getServletPath();
        applog_.info(String.format("Метод POST был отправлен на страницу %s", userPath));

        if ("/PageNavigateController".equals(userPath)) {
            Enumeration en = request.getParameterNames();

            while (en.hasMoreElements()) {

                applog_.trace("Получим имя переданного параметра");
                String name = (String) en.nextElement();
                applog_.debug(String.format("Название элемента формы= %s", name));

                if (name.equals("toLoginPageBtn")) {
                    applog_.trace("Нажата кнопка  Next");
                    if (request.isUserInRole("smarthouseuser") || request.isUserInRole("smarthouseadmin")) {
                        request.getRequestDispatcher("/members/home.html").forward(request, response);
                    } else {
                        request.getRequestDispatcher("/login.html").forward(request, response);
                    }
                }

                if (name.equals("toRootPageBtn")) {
                    applog_.trace("Нажата кнопка  Назад");
                    request.getRequestDispatcher("/index.html").forward(request, response);
                }

                if (name.equals("toDemoPageBtn")) {
                    applog_.trace("Нажата кнопка  Демо");
                    request.getRequestDispatcher("/demo.html").forward(request, response);
                }

                if (name.equals("toDashboardPageBtn")) {
                    applog_.trace("Нажата кнопка  Старт");
                    if (request.isUserInRole("smarthouseuser") || request.isUserInRole("smarthouseadmin")) {
                        request.getRequestDispatcher("/members/home.html").forward(request, response);
                    } else {
                        request.getRequestDispatcher("/login.html").forward(request, response);
                    }
                }

                if (name.equals("toLoginCompletePageBtn")) {
                    applog_.trace("Нажата кнопка  Вход");

                    String userName = request.getParameter("username");
                    String password = request.getParameter("password");
                    try {

                        applog_.trace("Логинимся...");
                        applog_.debug(String.format("Логин: %s", userName));
                        applog_.debug(String.format("Пароль: %s", password));
                        if (!(request.isUserInRole("smarthouseuser") || request.isUserInRole("smarthouseadmin"))) {

                            request.login(userName, password);

                        }


                        if (request.isUserInRole("smarthouseuser") || request.isUserInRole("smarthouseadmin")) {
                            request.getRequestDispatcher("/members/login_complete.html").forward(request, response);
                        } else{
                            applog_.trace("Вход напрещен. Не та роль");
                            request.logout();
                            request.getRequestDispatcher("/forbidden.html").forward(request, response);
                        }
                    } catch (ServletException ex) {
                        applog_.error("Login Failed with a ServletException.."
                                + ex.getMessage());
                        request.getRequestDispatcher("/forbidden.html").forward(request, response);
                    }

                }

            }
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String paramValue=request.getParameter("action");
        if(paramValue.equals("toLogoff")){
            applog_.trace("Нажата кнопка Выход");
            request.logout();
            request.getRequestDispatcher("/index.html").forward(request, response);
        }
    }
}
