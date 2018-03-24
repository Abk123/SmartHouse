package ru.net.bogunino84;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.net.Inet4Address;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IPDevice implements IPDeviceInterface {

    /**
     * Логгер
     *
     * @see IPDevice#applog_
     */
    private final static Logger applog_ = LogManager.getLogger(IPDevice.class);

    /**
     * Соединение с базой данных
     *
     * @see IPDevice#connection_
     */
    private Connection connection_;

    /**
     * Возвращает соединение с базой данных
     *
     * @see IPDevice#getConnection()
     * @return Соединение с базой данных
     */
    public Connection getConnection() {
        return connection_;
    }

    /**
     * IP адрес
     */
    private String ipAddress_;

    /**
     * Время обновления информации в секундах (0 или пусто - только один раз)
     *
     * @see IPDevice#updateTime_
     */
    private int updateTime_;

    /**
     * Возвращает время обновления данных в сепкундах
     *
     * @return Время обновления данных в сеакундах (0 или нет - только один раз)
     * @see IPDevice#getUpdateTime()
     */
    public int getUpdateTime() {
        readAttributesFromDatabase();
        return updateTime_;
    }

    /**
     * Аббревиатура устройства
     *
     * @see IPDevice#abbreviation_
     */
    private String abbreviation_;

    /**
     * Название таймера
     *
     * @see IPDevice#timerName_
     */
    private String timerName_;

    /**
     * Возвращает аббревиатуру устройства
     *
     * @see IPDevice#getAbbreviation()
     * @return Аббревиатура устройства
     */
    public String getAbbreviation() {
        return abbreviation_;
    }

    /**
     * Возвращает название таймера
     *
     * @return Название таймера
     */
    public String getTimerName() {
        return timerName_;
    }

    /**
     * ИД устройства
     *
     * @see IPDevice#id_
     */
    private int id_;

    /**
     * Опция активности
     *
     * @see IPDevice#isActive_
     */
    private boolean isActive_;

    /**
     * Опция доступности
     *
     * @see IPDevice#isEnabled_
     */
    private boolean isEnabled_;

    /**
     * Источник данных
     *
     * @see IPDevice#dataSource_
     */
    @Resource(mappedName = "jdbc/SMARTDB", type = DataSource.class)
    private DataSource dataSource_;

    /**
     * Проверить статус доступности
     *
     * @return Истина - если устройство доступно, ложь - если нет
     * @see IPDevice#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return isEnabled_;
    }

    /**
     * Проверить статус активности
     *
     * @return Истина - если устройство активно, ложь - если нет
     * @see IPDevice#isActive()
     */
    @Override
    public boolean isActive() {
        return isActive_;
    }

    /**
     * Сделать устройство доступным
     *
     * @see IPDevice#setEnabled()
     */
    @Override
    public void setEnabled() {
        String sql = "UPDATE DEVICES dv\n" +
                "   SET DV.ENABLED = 'Y'\n" +
                " WHERE DV.DV_ABBR = ?";

        try {
            PreparedStatement stmt = connection_.prepareStatement(sql);
            stmt.setString(1, abbreviation_);
            stmt.executeQuery();
            stmt.close();
            isEnabled_ = true;

        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }

    }

    /**
     * Сделать устройство недоступным
     *
     * @see IPDevice#setDisabled()
     */
    @Override
    public void setDisabled() {
        String sql = "UPDATE DEVICES dv\n" +
                "   SET DV.ENABLED = 'N'\n" +
                " WHERE DV.DV_ABBR = ?";

        try {
            PreparedStatement stmt = connection_.prepareStatement(sql);
            stmt.setString(1, abbreviation_);
            stmt.executeQuery();
            stmt.close();
            isEnabled_ = false;
        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }
    }

    /**
     * Метод, устанавливающий ссылку на соединение с базой данных
     *
     * @see IPDevice#setDatabaseConnection()
     */

    private void setDatabaseConnection() {
        try {
            applog_.info("Осуществляем соединение с базой данных");
            connection_ = dataSource_.getConnection();
            connection_.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            applog_.info("Успешное соединение");
        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }
    }

    /**
     * Метод, закрывающий соединение с базой данных
     *
     * @see IPDevice#closeDatabaseConnection()
     */

    private void closeDatabaseConnection() {
        try {
            applog_.info("Закрываем соединение с базой данных");
            connection_.close();
            applog_.info("Соединение с базой данных закрыто");
        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }
    }

    /**
     * Читаем данные из базы данных
     *
     * @see IPDevice#readAttributesFromDatabase()
     */
    private void readAttributesFromDatabase(){
        applog_.trace("Читаем IP адрес устройства");
        String sql = "  SELECT MAX (CASE WHEN dpr.pr_id = 2 THEN dpr.value_string END) ip_address,\n" +
                "         MAX (CASE WHEN dpr.pr_id = 3 THEN dpr.value_number END) update_time,\n" +
                "         dv.active,\n" +
                "         dv.enabled,\n" +
                "         dv.dv_id\n" +
                "    FROM devices dv JOIN device_properties dpr ON dpr.dv_id = dv.dv_id AND dpr.pr_id IN (2, 3)\n" +
                "   WHERE dv.dv_abbr = ?\n" +
                "GROUP BY dv.active, dv.enabled, dv.dv_id";
        try {
            PreparedStatement stmt = connection_.prepareStatement(sql);
            stmt.setString(1, abbreviation_);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                applog_.info("Считываем результаты");

                ipAddress_ = rs.getString(1);
                updateTime_ = rs.getInt(2);
                isActive_ = rs.getString(3).equals("Y");
                isEnabled_ = rs.getString(4).equals("Y");
                id_ = rs.getInt(5);

                applog_.debug(String.format("ipAddress_=%s", ipAddress_));
                applog_.debug(String.format("updateTime_=%d", updateTime_));
                applog_.debug(String.format("isActive_=%s", isActive_));
                applog_.debug(String.format("isEnabled_=%s", isEnabled_));
                applog_.debug(String.format("id_=%d", id_));
            }
            stmt.close();

        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }
    }

    /**
     * Инициализация объекта
     *
     * @param abbreviation Аббревиатура устройства
     * @see IPDevice#initDevice(String)
     */
    @Override
    public void initDevice(String abbreviation) {

        abbreviation_ = abbreviation;
        timerName_= String.format("%s_TIMER", abbreviation_);
        setDatabaseConnection();
        readAttributesFromDatabase();

    }

    /**
     * Метод уничтожения объекта
     *
     * @see IPDevice#destroyDevice()
     */
    @Override
    public void destroyDevice() {
        applog_.trace("Закрываем соединение с базой данных");
        closeDatabaseConnection();
    }

    /**
     * Обновить информацию об устройстве
     *
     * @see IPDevice#requestDevice()
     */
    @Override
    public void requestDevice() {
        String result;

        applog_.trace("Читаем атрибуты из базы данных");
        readAttributesFromDatabase();
        try {
            applog_.info(String.format("Пингуем устройство %s", abbreviation_));
            if (Inet4Address.getByName(ipAddress_).isReachable(5000)) {
                result = "Y";
            } else {
                result = "N";
                applog_.info(String.format("Device %s is not reachable", abbreviation_));
            }
        } catch (IOException e) {
            applog_.error(e.getLocalizedMessage());
            result = "N";
        }

        applog_.debug(String.format("Результат пинга=%s", result));

        String sql = "UPDATE DEVICES dv\n" +
                "   SET DV.ACTIVE = ?\n" +
                " WHERE DV.DV_ABBR = ?";

        try {
            applog_.info("Сохраняем результаты в БД");
            PreparedStatement stmt = connection_.prepareStatement(sql);
            stmt.setString(1, result);
            stmt.setString(2, abbreviation_);
            stmt.executeQuery();
            isActive_ = result.equals("Y");
            stmt.close();

            sql = "UPDATE DEVICE_PROPERTIES pr SET pr.VALUE_DATE=sysdate WHERE pr.DV_ID=? AND pr.PR_ID=1";
            stmt = connection_.prepareStatement(sql);
            stmt.setInt(1, id_);
            stmt.executeQuery();
            stmt.close();
            applog_.info("Результаты сохранены.");

        } catch (SQLException e) {
            applog_.error(e.getLocalizedMessage());
        }
    }
}
