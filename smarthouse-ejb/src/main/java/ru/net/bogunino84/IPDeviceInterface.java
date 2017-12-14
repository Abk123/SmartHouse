package ru.net.bogunino84;

/**
 * Интерфейс для класса IPDevice
 *
 * @see IPDeviceInterface
 */
public interface IPDeviceInterface {


    /**
     * Проверить статус доступности
     *
     * @see IPDeviceInterface#isEnabled()
     * @return Истина - если устройство доступно, ложь - если нет
     */
    boolean isEnabled();

    /**
     * Проверить статус активности
     *
     * @see IPDeviceInterface#isActive()
     * @return Истина - если устройство активно, ложь - если нет
     */
    boolean isActive();


    /**
     * Сделать устройство доступным
     *
     * @see IPDeviceInterface#setEnabled()
     */
    void setEnabled();


    /**
     * Сделать устройство недоступным
     *
     * @see IPDeviceInterface#setDisabled()
     */
    void setDisabled();

    /**
     * Инициализация объекта
     *
     * @see IPDeviceInterface#initDevice(String)
     * @param abbreviation Аббревиатура устройства
     */
    void initDevice(String abbreviation);

    /**
     * Метод уничтожения объекта
     *
     * @see IPDeviceInterface#destroyDevice()
     */
    void destroyDevice();


    /**
     * Обновить информацию об устройстве
     *
     * @see IPDeviceInterface#requestDevice()
     */
    void requestDevice();
}
