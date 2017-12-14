/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.net.bogunino84;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Класс для работы с последовательныи портом Доминатора
 *
 * @author Andrei Kosogorov
 * @see DominatorSerialPort
 */

public class DominatorSerialPort {

    /**
     * Логгер
     *
     * @see DominatorSerialPort#applog_
     */
    private final static Logger applog_ = LogManager.getLogger(DominatorSerialPort.class);

    /**
     * Переменная, отвечающая за блокирование последовательного порта
     *
     * @see DominatorSerialPort#dominatorLock_
     */
    private Lock dominatorLock_ = new ReentrantLock();


    /**
     * Процедура уничтожения последовательного порта
     *
     * @see DominatorSerialPort#destroyDominatorPort()
     */
    @SuppressWarnings("WeakerAccess")
    public void destroyDominatorPort() {
        if (serialPort_ != null && serialPort_.isOpened()) {
            try {
                serialPort_.closePort();
            } catch (SerialPortException e) {
                applog_.error(e.getLocalizedMessage());
            }
            applog_.info("Порт закрыли");
        } else {
            applog_.info("Порт не был открыт, поэтому его закрывать не надо");
        }
        serialPort_ = null;

    }


    /**
     * Символ окончания строки 0x0A
     */
    private static final int END_LINE = 0x0A;
    /**
     * 1-й символ замены символа окончания строки 0x0A = 0xDB
     */
    private static final int END_LINE1 = 0xDB;
    /**
     * 2-й символ замены символа окончания строки 0x0A = 0xDC
     */
    private static final int END_LINE2 = 0xDC;
    /**
     * 1-й символ замены управляющего символа 0xDB (END_LINE1) = 0xDB
     */
    private static final int END_LINE1_1 = 0xDB;
    /**
     * 2-й символ замены управляющего символа 0xDB (END_LINE1) = 0xDD
     */
    private static final int END_LINE1_2 = 0xDD;
    /**
     * Ошибка: неправильная контрольная сумма
     */
    private static final int ERR_RS_SUM = 1;
    /**
     * Ошибка: нет эхо при передаче
     */
    private static final int ERR_RS_ECHO = 2;
    /**
     * Ошибка: неправильный код при передачи - не хватает символа или не те
     * символы
     */
    private static final int ERR_RS_CODE = 4;
    /**
     * Ошибка: не было предварительно команды разрешения записи в EEPROM
     */
    private static final int ERR_RS_NO_EEWR = 0x10;
    /**
     * Ошибка: используется резерв адресного пространства, пока эти пространства
     * не реализованы
     */
    private static final int ERR_RS_RESERVE = 0x20;
    /**
     * Максимальное количество итераций
     */
    private static final int MAX_ITERATION_NUMBER = 560;
    /**
     * Максимальная длина страницы
     */
    private static final int MAX_PAGE_LENGTH = 256;
    /**
     * Максимальный адрес памяти
     */
    private static final int MAX_MEMORY_ADDRESS = 0xFFFF;
    /**
     * Константа, закрепленная за устройством МАП Доминатор
     */
    private static final int TYPE_DOMINATOR = 0x03;
    /**
     * Код команды на запись
     */
    private static final int TO_WRITE = 0x77;
    /**
     * Код команды на чтение
     */
    private static final int TO_READ = 0x72;
    /**
     * Код ответа - успех
     */
    private static final int ANSWER_OK = 0x6F;
    /**
     * Код ответа - ошибка
     */
    private static final int ANSWER_ERR = 0x65;

    /**
     * Ссылка на последовательный порт
     */
    private SerialPort serialPort_;

    /**
     * Получить 1 байт из внутренней памяти устройства. Подтверждение
     * осуществляется эхом
     *
     * @return Возвращает 1 байт прочитанной информации от 0 до 255
     * @throws SerialPortException        Ошибка при работе с последовательным портом
     * @throws SerialPortTimeoutException Ошибка при работе с последовательным портом
     * @see DominatorSerialPort#getOneByte()
     */
    private int getOneByte() throws SerialPortException, SerialPortTimeoutException {

        int buffer[];

        applog_.info("Выполняем процедуру чтения одного байта getOneByte...");

        buffer = serialPort_.readIntArray(1, 5000);
        applog_.info(String.format("Байт прочитали. Значение= %x. Отправляем эхом...", buffer[0]));
        buffer[0] = buffer[0] & 0xFF;
        applog_.info(String.format("Отправляем байт= %x", buffer[0]));
        if (!serialPort_.writeInt(buffer[0])) {
            throw new SerialPortException(serialPort_.getPortName(), "serialPort_.writeInt", "Ошибка отправки байта");
        }
        applog_.info(String.format("Успешно отправили байт= %x", buffer[0]));
        return buffer[0];

    }

    /**
     * Отправить 1 байт во внутреннюю память устройства. Подтверждение
     * осуществляется эхом
     *
     * @param data Байт данных
     * @throws SerialPortException        Ошибка при работе с последовательным портом
     * @throws SerialPortTimeoutException Ошибка при работе с последовательным портом
     * @see DominatorSerialPort#putOneByte(int)
     */
    private void putOneByte(int data) throws SerialPortException, SerialPortTimeoutException {

        int buffer[];

        applog_.info("Выполняем процедуру записи одного байта putOneByte...");
        applog_.info("Преобразуем к одному байту.");

        data = data & 0xFF;

        applog_.info(String.format("Отправляем байт= %x", data));
        if (!serialPort_.writeInt(data)) {
            throw new SerialPortException(serialPort_.getPortName(), "serialPort_.writeInt", "Ошибка отправки байта");
        }

        applog_.info("Читаем эхо...");
        buffer = serialPort_.readIntArray(1, 5000);

        applog_.info("Преобразуем к одному байту.");
        buffer[0] = buffer[0] & 0xFF;
        applog_.info(String.format("Прочитали байт= %x", buffer[0]));

        if (buffer[0] != data) {
            applog_.error("Странная ошибка чтения эхо. Сбросим последовательный порт (закроем/откроем) и взорвем ошибку");
            initDominatorPort();
            throw new SerialPortException(serialPort_.getPortName(), "serialPort_.writeInt", "Ошибка отправки байта");
        }

    }


    /**
     * Процедура поиска последовательного порта для подключения Доминатора
     *
     * @see DominatorSerialPort#initDominatorPort()
     */
    @SuppressWarnings("WeakerAccess")
    public boolean initDominatorPort() {
        applog_.info("Начали искать последовательный порт...");
        String[] portNames = SerialPortList.getPortNames();
        boolean result = false;
        try {

            applog_.info("Если были какие-то проблемы с ранее открытым портом, то пробуем закрыть его.");
            destroyDominatorPort();

            applog_.info("Переходим к сканированию портов");
            applog_.info(String.format("Количество найденных портов - %d", portNames.length));

            for (String portName : portNames) {
                applog_.info(String.format("Нашли порт с именем= %s", portName));
                if (portName.startsWith("/dev/ttyUSB")) {
                    applog_.info("Этот порт подходит по маске. Начинаем проверять подсоединенное устройство...");


                    serialPort_ = new SerialPort(portName);

                    applog_.info("Открываем последовательный порт");
                    serialPort_.openPort();
                    serialPort_.setParams(SerialPort.BAUDRATE_19200,
                            SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);

                    applog_.info(String.format("Порт доминатора %s подключен", portName));
                    result = checkDominatorPort();


                }
            }
        } catch (SerialPortException e) {
            applog_.error(e.getLocalizedMessage());
        }


        if (!result) {
            applog_.info("Порт не открыли.");
        }

        return result;
    }

    String getPortName(){
        return serialPort_.getPortName();
    }

    /**
     * Процедура, которрая время от времени должна вызываться и проверять доступность порта Доминатора
     * Если порт не доступен, то он должен быть снова инициализирован
     * Если порт ранее был доступен, и по каким-то причинам сейчас нет, то мы должны этот порт постараться закрыть
     *
     * @return Истина, если порт Доминатора подтвержден. В противном случае ложь
     * @see DominatorSerialPort#checkDominatorPort(
     */
    @SuppressWarnings("WeakerAccess")
    public boolean checkDominatorPort() {
        boolean result = false;

        //needToCheckDominatorPort_ = false;

        if (serialPort_.isOpened()) {
            int data[] = new int[MAX_PAGE_LENGTH];
            int numBytes = readFromMemory(data, 0x00, 1);
            applog_.info(String.format("Прочитали %d байт", numBytes));

            if (numBytes > 0) {
                // Самый первый байт - это контрольное число. Поэтому читаем со 2-го байта
                if (data[1] == TYPE_DOMINATOR) {
                    applog_.info("Нашли порт, к которому подключен Доминатор.");

                    result = true;
                } else {
                    applog_.info("Нашли порт, но это не порт Доминатор");
                }
            } else {
                applog_.info("Количество прочитанных байт равно 0");
            }
        }


        return result;
    }

    /**
     * Процедура отправки команды
     *
     * @param command Команда: чтение или запись
     * @param address Адрес памяти
     * @param length  Длина страницы
     * @return Контролная сумма
     * @throws SerialPortException        Ошибка работы с портом
     * @throws SerialPortTimeoutException Ошибка работы с портом
     * @see DominatorSerialPort#sendCommand(int, int, int)
     */
    private int sendCommand(int command, int address, int length) throws SerialPortException, SerialPortTimeoutException {
        //Контрольная сумма
        int checksum = 0;
        applog_.info(String.format("checksum= %x", checksum));

        applog_.info(String.format("Входный параметы: command= %x, address= %x, length= %x", command, address, length));

        applog_.info("У длины страницы вычитаем 1");
        length--;

        applog_.info("Накапливаем массив данных, элементы которого потом будем последовательно отправлять");
        int[] workingData = new int[4];
        applog_.info("Посылаем команду на чтение/запись данных");
        workingData[0] = command;
        applog_.info("Посылаем количество байт");
        length &= 0xFF;
        workingData[1] = length;
        applog_.info("Посылаем верхний байт памяти");
        workingData[2] = address >> 8;
        applog_.info("Посылаем нижний байт памяти");
        workingData[3] = address & 0xFF;

        applog_.debug(String.format("Значения массива данных для отправки= %x:%x:%x:%x", workingData[0], workingData[1], workingData[2], workingData[3]));
        applog_.info("Массив накопили. Теперь будем отправлять данные");
        applog_.debug(String.format("Отправляем команду: %x:%x:%x:%x", workingData[0], workingData[1],
                workingData[2], workingData[3]));

        for (int currentValue : workingData) {
            applog_.info(String.format("Посылаем байт= %x", currentValue));
            if (currentValue == command) {
                putOneByte(currentValue);
                // Накапливаем контрольную сумму
                checksum += command;
                checksum &= 0xFF;
                applog_.info(String.format("checksum= %x", checksum));
            } else {
                checksum = sendCurrentValue(checksum, currentValue);
                applog_.info(String.format("checksum= %x", checksum));
            }
        }

        return checksum;


    }

    /**
     * Процедура отправки контрольной суммы
     *
     * @param checksum Контрольная сумма
     * @return Контрольная сумма
     * @throws SerialPortException        Ошибка работы с портом
     * @throws SerialPortTimeoutException Ошибка работы с портом
     * @see DominatorSerialPort#sendChecksum(int)
     */
    private int sendChecksum(int checksum) throws SerialPortException, SerialPortTimeoutException {
        checksum = 0xFF - checksum;
        checksum++;
        checksum &= 0xFF;
        applog_.info(String.format("Значение контрольной суммы = %x", checksum));

        putOneByte(checksum);
        if (checksum != END_LINE) {
            applog_.info(String.format("Так как величина контрольной суммы не равно символу окончания строки %x, то отправляем дополнительно символ окончания строки", END_LINE));
            putOneByte(END_LINE);
        }

        return checksum;
    }

    /**
     * Процедура отправки одного байта
     *
     * @param checksum     Контрольная сумма
     * @param currentValue Текущее значение
     * @return Контрольная сумма
     * @throws SerialPortException        Ошибка работы с портом
     * @throws SerialPortTimeoutException Ошибка работы с портом
     * @see DominatorSerialPort#sendCurrentValue(int, int)
     */
    private int sendCurrentValue(int checksum, int currentValue) throws SerialPortException, SerialPortTimeoutException {


        switch (currentValue) {
            case END_LINE:
                putOneByte(END_LINE1);
                checksum += END_LINE1;
                checksum &= 0xFF;
                putOneByte(END_LINE2);
                checksum += END_LINE2;
                checksum &= 0xFF;
                break;
            case END_LINE1:
                putOneByte(END_LINE1_1);
                checksum += END_LINE1_1;
                checksum &= 0xFF;
                putOneByte(END_LINE1_2);
                checksum += END_LINE1_2;
                checksum &= 0xFF;
                break;
            default:
                putOneByte(currentValue);
                checksum += currentValue;
                checksum &= 0xFF;
                break;
        }

        return checksum;
    }

    /**
     * Процедура четния возвращаемых данных
     *
     * @param data Массив считанных данных
     * @return Количество считанных байт
     * @throws SerialPortException        Ошибка работы с портом
     * @throws SerialPortTimeoutException Ошибка работы с портом
     * @see DominatorSerialPort#readAnswer(int[])
     */
    private int readAnswer(int data[]) throws SerialPortException, SerialPortTimeoutException {
        // Обыкновенный счетчик
        int i;
        int checksum = 0;
        int oldChecksum = 0;
        int oneByte = 0;

        applog_.info(String.format("checksum= %x", checksum));


        // Переменная, которая отвечает за количество байт в массиве
        // Маскирующие символы не увеличивают количество байт
        int numBytes = 0;
        // В цикле получаем ответ
        // В случае превышения максимального количества итераций взрыввем exception
        applog_.info("В цикле получаем ответ");

        for (i = 0; i < MAX_ITERATION_NUMBER; i++) {
            oneByte = getOneByte();

            applog_.debug(String.format("Значение oneByte=%x", oneByte));

            // Если встречается символ 0xDB,
            // то это либо сам символ 0xDB и тогда следом должен идти сомвол 0xDD,
            // либо это символ 0x0A и тогда следом должен идти символ 0xDC
            // В массив записывается преобразованный символ без управляющих символов
            // Но контрольная сумма учитывает управляющие символы
            // Если это управляющий символ, то читаем следующий байт
            if (oneByte == END_LINE1) {
                applog_.info("Полученный байт равен символу END_LINE1");
                // Обязаны накопить контрольную сумму
                //noinspection UnusedAssignment
                oldChecksum = checksum;
                checksum += oneByte;
                checksum &= 0xFF;
                applog_.info(String.format("checksum= %x", checksum));
                // Сохраняем старое значение
                int oldByte = oneByte;
                i++;
                // Читаем следующий байт
                oneByte = getOneByte();

                switch (oneByte) {
                    case END_LINE2:
                        oldChecksum = checksum;
                        checksum += oneByte;
                        checksum &= 0xFF;
                        applog_.info(String.format("checksum= %x", checksum));

                        oneByte = END_LINE;
                        numBytes++;
                        data[numBytes - 1] = oneByte;

                        break;
                    case END_LINE1_2:
                        oldChecksum = checksum;
                        checksum += oneByte;
                        checksum &= 0xFF;
                        applog_.info(String.format("checksum= %x", checksum));

                        oneByte = END_LINE1;
                        numBytes++;
                        data[numBytes - 1] = oneByte;

                        break;
                    default:
                        numBytes++;
                        data[numBytes - 1] = oldByte;

                        oldChecksum = checksum;
                        checksum += oneByte;
                        checksum &= 0xFF;
                        applog_.info(String.format("checksum= %x", checksum));

                        numBytes++;
                        data[numBytes - 1] = oneByte;


                        break;
                }

            } else if (oneByte == END_LINE) {
                applog_.info("Получили символ окончания строки. Выходим из цикла...");
                break;
            } else {

                oldChecksum = checksum;
                checksum += oneByte;
                checksum &= 0xFF;
                applog_.info(String.format("checksum= %x", checksum));

                numBytes++;
                data[numBytes - 1] = oneByte;
            }

        }

        if (oneByte == END_LINE) {
            // Проверяем контрольную сумму
            if (checksum != END_LINE) {
                checksum = oldChecksum;
                applog_.info(String.format("Т.к. контрольная сумма не равно 0x0A, то мы зря сложили предыдущее значение. Это было значение контрольной суммы, поэтому присвоили старое значение checksum= %x", checksum));
            }

            applog_.info(String.format("checksum= %x", checksum));
            checksum = 0xFF - checksum;
            checksum &= 0xFF;
            checksum++;
            checksum &= 0xFF;


            applog_.info(String.format("Контрольная сумма = %x", checksum));

            applog_.info(String.format("data[numBytes - 1] = %x", data[numBytes - 1]));

            applog_.info(String.format("data[numBytes] = %x", data[numBytes]));

            // TODO: 12.08.17 Надо проверить правильность определения контрольных сумм
//            if ((checksum != 0x0A) && (checksum != data[numBytes - 1])) {
//
//                throw new SerialPortException(serialPort_.getPortName(), "readAnswer", "Ошибка в контрольной сумме");
//                applog_.error("Ошибка в контрольной сумме");
//            }
//
//            if ((checksum == 0x0A) && (checksum != data[numBytes])) {
//                throw new SerialPortException(serialPort_.getPortName(), "readAnswer", "Ошибка в контрольной сумме");
//                applog_.error("Ошибка в контрольной сумме");
//            }

            applog_.info("Проверяем результат. Читаем самый первый символ. В случае успеха должно быть 0x6F. В случае ошибки получим 0x65.");

            switch (data[0]) {
                case ANSWER_OK:
                    applog_.info("Успешное чтение. Вернулись данные, а не ошибка");
                    break;
                case ANSWER_ERR:
                    applog_.error("Данные вернулись с ошибкой");
                    switch (data[0]) {
                        case ERR_RS_SUM:
                            applog_.error("Код ошибки: неправильная контрольная сумма");
                            throw new SerialPortException(serialPort_.getPortName(), "readAnswer", "Неправильная контрольная сумма");
                        case ERR_RS_ECHO:
                            applog_.error("Код ошибки: нет эхопередачи");
                            throw new SerialPortException(serialPort_.getPortName(), "readAnswer", "Нет эхопередачи");
                        case ERR_RS_CODE:
                            applog_.error("Код ошибки: неправильный код при передаче (не хватает символа или не те символы)");
                            throw new SerialPortException(serialPort_.getPortName(), "readAnswer", "Неправильный код при передаче (не хватает символа или не те символы)");
                        case ERR_RS_NO_EEWR:
                            applog_.error("Код ошибки: не было предварительной команды разрешения на запись");
                            throw new SerialPortException(serialPort_.getPortName(), "readAnswer", "Не было предварительной команды разрешения на запись");
                        case ERR_RS_RESERVE:
                            applog_.error("Код ошибки: зарезервировано");
                            throw new SerialPortException(serialPort_.getPortName(), "readAnswer", "Нет эхопередачи");
                    }
                    break;
            }

        } else {
            throw new SerialPortException(serialPort_.getPortName(), "readFromMemory", "Слишком много итераций в цикле");
        }

        return numBytes;
    }

    /**
     * @param data    Массив выходных данных
     * @param address Адрес ячейки в десятичном формате
     * @param length  Длинна ячейки в байтах
     * @return Количество прочитанных байтов
     * @see DominatorSerialPort#readFromMemory(int[], int, int)
     */

    @SuppressWarnings("WeakerAccess")
    public int readFromMemory(int[] data, int address, int length) {

        /*

            Чтение страницы (от 1 до 256 байт) данных начиная с адреса A=A1A0
            Посылка начинается с сивола ‘r’ (0x72), далее идет байт длины страницы
            P (0 – 1 байт, 1- 2 байт, … , 255 – 256 байт), два байта адреса A,
            байт контрольной суммы S и символ ‘\n’ – конец посылки (если контрольная
            сумма сама не равна ‘\n’).
            rPA1A0S\n (код ‘r’=0x72)
            и ждем ответа – читаем от МАП

         */
        dominatorLock_.lock();

        //applog_.info("Получили команду на проверку последовательного порта. Надо проверить");
        //if (needToCheckDominatorPort_) {
        //    checkDominatorPort();
        //}

        //Контрольная сумма
        int checksum;

        int result = 0;

        applog_.info("Начали читать данные из внутренней памяти Доминатора");
        applog_.info(
                String.format("Значения входных параметров: address=%x, length=%d", address, length));

        try {
            if (serialPort_.isOpened()) {
                if (length <= MAX_PAGE_LENGTH) {
                    if (address <= MAX_MEMORY_ADDRESS) {
                        applog_.info("Все проверки входных данных пройдены.");


                        checksum = sendCommand(TO_READ, address, length);

                        applog_.info("После отправки всех данных преобразовываем и отправляем контрольную сумму");

                        sendChecksum(checksum);

                        applog_.info("Все отправили, начинаем принимать ответ");

                        result = readAnswer(data);

                    } else {
                        applog_.error(String.format("Неразрешенный адрес памяти. Разрешено 0 - %x, запрошено %x",
                                MAX_MEMORY_ADDRESS, address));
                    }
                } else {
                    applog_.error(String.format("Превышено максимально возможное количество байтов для чтения. Разрешено %d, запрошено %d",
                            MAX_PAGE_LENGTH, length));
                }
            } else {
                applog_.error("Последовательный порт не найден или не открыт");
            }
        } catch (SerialPortException | SerialPortTimeoutException e) {
            applog_.error(e.getLocalizedMessage());
        }

        dominatorLock_.unlock();

        return result;
    }

    @SuppressWarnings("unused")
    public void writeToMemory(int address, int[] data) {

        /*
            Посылка Команды / Запись страницы P данных начиная с адреса A=A1A0
            Есть общая команда записи:
            Посылка начинается с сивола ‘w’ (0x77), далее идет байт длинны
            страницы P (0 – 1 байт, 255 – 256 байт), два байта адреса A, P байт данных,
            байт контрольной суммы S и символ ‘\n’ – конец посылки (если контрольная сумма сама не равна ‘\n’).
            wPA1A0D0D1D2 ….. DpS\n
            и ждем ответа – читаем от МАП

         */

        dominatorLock_.lock();


        //Длина страницы определяется количеством элементов в массиве
        int length = data.length;

        if (serialPort_.isOpened()) {
            if (length <= MAX_PAGE_LENGTH) {

                if (address <= MAX_MEMORY_ADDRESS) {

                    try {

                        applog_.info("Все проверки входных данных пройдены.");
                        // Контрольная сумма
                        int checksum = sendCommand(TO_WRITE, address, length);

                        applog_.info("Отправляем сами данные");
                        applog_.info(String.format("Отправляем %d байт", data.length));

                        for (int currentValue : data) {
                            applog_.info(String.format("Посылаем байт= %x", currentValue));
                            currentValue = currentValue & 0xFF;

                            checksum = sendCurrentValue(checksum, currentValue);
                        }

                        applog_.info("Преобразовываем контрольную сумму");

                        sendChecksum(checksum);

                        applog_.info("Начинаем принимать ответ");

                        readAnswer(data);

                    } catch (SerialPortException | SerialPortTimeoutException e) {
                        applog_.error(e.getLocalizedMessage());
                    }
                } else {
                    applog_.error("Превышен максимально возможный адрес памяти");
                }

            } else {
                applog_.error("Превышено максимально возможное количество байт, которые можно записать");
            }
        } else {
            applog_.error("Последовательный порт не найден или не открыт");
        }

        dominatorLock_.unlock();
    }


}
