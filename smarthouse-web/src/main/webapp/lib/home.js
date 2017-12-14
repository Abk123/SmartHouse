// Процедура обновления размеров элементов
function resizeComponents() {
    "use strict";
    kendo.resize($('#dm-accum-voltage-chart'));
    kendo.resize($('#dm-accum-temp-chart'));
    kendo.resize($('#dashboard-power-live-chart'));
    kendo.resize($('#dashboard-stat-chart'));
}


$(document).ready(function () {
    "use strict";

    // ******************************** График потребления электроэнергии в режиме реального времени ************
    var powerLiveChartCategoryList = [];  // массив для хранения точек по оси X
    var powerLiveChartData = [];          // массов для хранения точек по оси Y
    var maxPowerLiveChartCategories = 60; // количество точек по оси X
    var accumVoltage = 20;                // напряжение аккумулятора
    var accumTemperature = 0;             // напряжение аккумулятора


    // Функция инициализации массива
    function setArrayDefault(list, nMax) {
        for (var i = 0; i < nMax; i++) {
            list.push("");
        }
    }

    // Добавить в стэк точку по оси Y, сместив все значения влево
    function addChartDataValue(list, nMax, dataValue) {
        list.shift();
        list[nMax - 1] = dataValue;
    }

    setArrayDefault(powerLiveChartCategoryList, maxPowerLiveChartCategories);
    setArrayDefault(powerLiveChartData, maxPowerLiveChartCategories);

    // Получить точку для отображения на графике потребления электроэнергии в режиме реального времени
    function requestPnetLiveValue() {
        jQuery.getJSON("/EnergyMonitoringController", "action=CELL_PNET_LIVE", function (data) {

            //console.info("CELL_PNET_LIVE=" + data.current_value);

            /**
             * @typedef {Object} data
             * @property {string} current_value
             */

            addChartDataValue(powerLiveChartData, maxPowerLiveChartCategories, data.current_value);

        });
    }

    // Получить точку для отображения на графике напряжения аккумулятора
    function requestAccumVoltageValue() {
        jQuery.getJSON("/EnergyMonitoringController", "action=CELL_UACCMED_LIVE", function (data) {

            /**
             * @typedef {Object} data
             * @property {string} current_value
             */

            accumVoltage = data.current_value;

            //console.info("accumVoltage=" + accumVoltage);

        });
    }

    // Получить точку для отображения на графике тпемпературы аккумулятора
    function requestAccumTemperatureValue() {
        jQuery.getJSON("/EnergyMonitoringController", "action=CELL_ACCTEMP_LIVE", function (data) {

            accumTemperature = data.current_value;

            //console.info("accumVoltage=" + accumVoltage);

        });
    }

    // Получить статус активности устройств
    function requestDeviceActiveValue() {
        jQuery.getJSON("/EnergyMonitoringController", "action=DEVICE_ACTIVE", function (data) {

            //console.info("requestDeviceActiveValue: "+ data.dominator);

            /**
             * @typedef {Object} data
             * @property {string} dominator
             * @property {string} wn2500rp
             * @property {string} wnr3500l
             * @property {string} ps1810_8g
             * @property {string} tl_wa850re
             * @property {string} hp_ilo
             * @property {string} sms
             */

            var $dominator_active = $('#dominator-active');
            if (data.dominator === 'Y') {
                $dominator_active.html('<i class="fa fa-2x fa-toggle-on" aria-hidden="true"></i>');
                $dominator_active.removeClass('color-red');
                $dominator_active.addClass('color-green');
            }
            else {
                $dominator_active.html('<i class="fa fa-2x fa-toggle-off" aria-hidden="true"></i>');
                $dominator_active.removeClass('color-green');
                $dominator_active.addClass('color-red');
            }

            var $wn2500rp_active = $('#wn2500rp-active');
            if (data.wn2500rp === 'Y') {
                $wn2500rp_active.html('<i class="fa fa-2x fa-toggle-on" aria-hidden="true"></i>');
                $wn2500rp_active.removeClass('color-red');
                $wn2500rp_active.addClass('color-green');
            }
            else {
                $wn2500rp_active.html('<i class="fa fa-2x fa-toggle-off" aria-hidden="true"></i>');
                $wn2500rp_active.removeClass('color-green');
                $wn2500rp_active.addClass('color-red');
            }

            var $wnr3500l_active = $('#wnr3500l-active');
            if (data.wnr3500l === 'Y') {
                $wnr3500l_active.html('<i class="fa fa-2x fa-toggle-on" aria-hidden="true"></i>');
                $wnr3500l_active.removeClass('color-red');
                $wnr3500l_active.addClass('color-green');
            }
            else {
                $wnr3500l_active.html('<i class="fa fa-2x fa-toggle-off" aria-hidden="true"></i>');
                $wnr3500l_active.removeClass('color-green');
                $wnr3500l_active.addClass('color-red');
            }

            var $ps1810_8g_active = $('#ps1810_8g-active');
            if (data.ps1810_8g === 'Y') {
                $ps1810_8g_active.html('<i class="fa fa-2x fa-toggle-on" aria-hidden="true"></i>');
                $ps1810_8g_active.removeClass('color-red');
                $ps1810_8g_active.addClass('color-green');
            }
            else {
                $ps1810_8g_active.html('<i class="fa fa-2x fa-toggle-off" aria-hidden="true"></i>');
                $ps1810_8g_active.removeClass('color-green');
                $ps1810_8g_active.addClass('color-red');
            }

            var $tl_wa850re_active = $('#tl_wa850re-active');
            if (data.tl_wa850re === 'Y') {
                $tl_wa850re_active.html('<i class="fa fa-2x fa-toggle-on" aria-hidden="true"></i>');
                $tl_wa850re_active.removeClass('color-red');
                $tl_wa850re_active.addClass('color-green');
            }
            else {
                $tl_wa850re_active.html('<i class="fa fa-2x fa-toggle-off" aria-hidden="true"></i>');
                $tl_wa850re_active.removeClass('color-green');
                $tl_wa850re_active.addClass('color-red');
            }

            var $hp_ilo_active = $('#hp_ilo-active');
            if (data.hp_ilo === 'Y') {
                $hp_ilo_active.html('<i class="fa fa-2x fa-toggle-on" aria-hidden="true"></i>');
                $hp_ilo_active.removeClass('color-red');
                $hp_ilo_active.addClass('color-green');
            }
            else {
                $hp_ilo_active.html('<i class="fa fa-2x fa-toggle-off" aria-hidden="true"></i>');
                $hp_ilo_active.removeClass('color-green');
                $hp_ilo_active.addClass('color-red');
            }

            var $sms_active = $('#sms-active');
            var $sms_active_status = $('#sms-active-status');
            if (data.sms === 'Y') {
                $sms_active.html('<i class="fa fa-2x fa-toggle-on" aria-hidden="true"></i>');
                $sms_active.removeClass('color-red');
                $sms_active.addClass('color-green');

                $sms_active_status.html('<i class="fa fa-2x fa-toggle-on" aria-hidden="true"></i>');
                $sms_active_status.removeClass('color-red');
                $sms_active_status.addClass('color-green');
            }
            else {
                $sms_active.html('<i class="fa fa-2x fa-toggle-off" aria-hidden="true"></i>');
                $sms_active.removeClass('color-green');
                $sms_active.addClass('color-red');

                $sms_active_status.html('<i class="fa fa-2x fa-toggle-off" aria-hidden="true"></i>');
                $sms_active_status.removeClass('color-green');
                $sms_active_status.addClass('color-red');
            }

        });
    }


    // Получить статус доступности устройств
    var $dm_enable_status = $('#dm-enable-status');
    var $sms_enable_status = $('#sms-enable-status');
    // noinspection JSUnresolvedFunction
    $dm_enable_status.kendoMobileSwitch({
        onLabel: "ДА",
        offLabel: "НЕТ",
        checked: false
    });
    var $dm_enable_status_obj = $dm_enable_status.data('kendoMobileSwitch');
    // noinspection JSUnresolvedFunction
    $sms_enable_status.kendoMobileSwitch({
        onLabel: "ДА",
        offLabel: "НЕТ",
        checked: false
    });
    var $sms_enable_status_obj = $sms_enable_status.data('kendoMobileSwitch');


    function requestDeviceEnableValue() {
        jQuery.getJSON("/EnergyMonitoringController", "action=DEVICE_ENABLE", function (data) {

            //console.info("requestDeviceActiveValue: "+ data.dominator);

            /**
             * @typedef {Object} data
             * @property {string} dominator
             * @property {string} wn2500rp
             * @property {string} wnr3500l
             * @property {string} ps1810_8g
             * @property {string} tl_wa850re
             * @property {string} hp_ilo
             * @property {string} sms
             */

            //console.info(data.dominator);

            if (data.dominator === 'Y') {
                // noinspection JSUnresolvedFunction
                $dm_enable_status_obj.check(true);
            }
            else {
                // noinspection JSUnresolvedFunction
                $dm_enable_status_obj.check(false);
            }

            if (data.sms === 'Y') {
                // noinspection JSUnresolvedFunction
                $sms_enable_status_obj.check(true);
            }
            else {
                // noinspection JSUnresolvedFunction
                $sms_enable_status_obj.check(false);
            }


        });
    }

    // Начальные присвоения
    $.ajaxSetup({
        async: false
    });

    requestPnetLiveValue();
    requestAccumVoltageValue();
    requestAccumTemperatureValue();
    requestDeviceActiveValue();
    requestDeviceEnableValue();

    $.ajaxSetup({
        async: true
    });


    // Создание графика потребления электроэнергии в режиме реального времени
    function createDashboardPowerLiveChart() {
        // noinspection JSUnresolvedFunction
        $('#dashboard-power-live-chart').kendoChart({
            renderAs: "canvas",
            seriesDefaults: {
                type: "area",
                area: {
                    line: {
                        style: "smooth"
                    }
                },
                color: "#32CD32"
            },
            series: [{
                data: powerLiveChartData
            }],
            valueAxis: {
                labels: {
                    format: "{0}"
                },
                line: {
                    visible: false
                }
            },
            categoryAxis: {
                categories: powerLiveChartCategoryList,
                majorGridLines: {
                    visible: true
                },
                labels: {
                    rotation: "auto"
                }
            },
            tooltip: {
                visible: true,
                format: "{0}"
            },
            transitions: false
        });
    }

    createDashboardPowerLiveChart();

    // Установка времени считывания данных для графика потребления электроэнергии в режиме реального времени
    var pnetLiveInterval = setTimeout(function setPnetLiveRequest() {
        //console.info("PNETL Request!");
        requestPnetLiveValue();
        //createDashboardPowerLiveChart();

        $("#dashboard-power-live-chart").data("kendoChart").refresh();

        pnetLiveInterval = setTimeout(setPnetLiveRequest, 5000);
    }, 5000);


    // *************************************** Другие настройки ************************************************

    // Навигационная панель
    // noinspection JSUnresolvedFunction
    $("#panelbar").kendoPanelBar({
        expandMode: "single"
    });

    // Установка начала недели при отображении календаря на понедельник
    kendo.culture().calendar.firstDay = 1;

    // ************************************ Данные об устройстве Доминатор ************************************
    // Получить данные об устройстве Доминатор
    function requestDmDeviseInfo() {
        jQuery.getJSON("/EnergyMonitoringController", "action=DM_DEVICE_INFO", function (data) {

            //console.info("dt=" + dt.verpow);

            /**
             * @typedef {Object} data
             * @property {string} verpow
             * @property {string} verpo
             * @property {string} pow
             * @property {string} uacc
             * @property {string} verplatpic
             * @property {string} verplatnet
             * @property {string} em_temp_okr
             * @property {string} em_temp_top
             * @property {string} em_temp_trad
             * @property {string} t_net_on
             * @property {string} t_charge
             * @property {string} t_accdisch
             * @property {string} port
             */

            $('#dm-verpow').html(data.verpow);
            $('#dm-verpo').html(data.verpo);
            $('#dm-pow').html(data.pow);
            $('#dm-uacc').html(data.uacc);
            $('#dm-verplatpic').html(data.verplatpic);
            $('#dm-verplatnet').html(data.verplatnet);

            var $dm_temp_okr = $('#dm-temp-okr');
            if (data.em_temp_okr === 'ON') {
                $dm_temp_okr.html('<i class="fa fa-2x fa-toggle-on" aria-hidden="true"></i>');
                $dm_temp_okr.removeClass('color-red');
                $dm_temp_okr.addClass('color-green');
            }
            else {
                $dm_temp_okr.html('<i class="fa fa-2x fa-toggle-off" aria-hidden="true"></i>');
                $dm_temp_okr.removeClass('color-green');
                $dm_temp_okr.addClass('color-red');
            }

            var $dm_temp_top = $('#dm-temp-top');
            if (data.em_temp_top === 'ON') {
                $dm_temp_top.html('<i class="fa fa-2x fa-toggle-on" aria-hidden="true"></i>');
                $dm_temp_top.removeClass('color-red');
                $dm_temp_top.addClass('color-green');
            }
            else {
                $dm_temp_top.html('<i class="fa fa-2x fa-toggle-off" aria-hidden="true"></i>');
                $dm_temp_top.removeClass('color-green');
                $dm_temp_top.addClass('color-red');
            }

            var $dm_temp_trad = $('#dm-temp-trad');
            if (data.em_temp_trad === 'ON') {
                $dm_temp_trad.html('<i class="fa fa-2x fa-toggle-on" aria-hidden="true"></i>');
                $dm_temp_trad.removeClass('color-red');
                $dm_temp_trad.addClass('color-green');
            }
            else {
                $dm_temp_trad.html('<i class="fa fa-2x fa-toggle-off" aria-hidden="true"></i>');
                $dm_temp_trad.removeClass('color-green');
                $dm_temp_trad.addClass('color-red');
            }

            $('#dm-tnet-on').html(data.t_net_on);
            $('#dm-tcharge').html(data.t_charge);
            $('#dm-taccdisch').html(data.t_accdisch);
            $('#dm-port').html(data.port);
        });
    }

    requestDmDeviseInfo();

    // Установка времени считывания данных об устройстве Доминатор
    var dmDeviceInfoInterval = setTimeout(function setDmDeviceInfoRequest() {
        //console.info("ACCUM VALUE Request!");
        requestDmDeviseInfo();

        dmDeviceInfoInterval = setTimeout(setDmDeviceInfoRequest, 60000);
    }, 60000);

    // ************************************  Диалог ***********************************************************
    var dialog = $('#dialog');
    // noinspection JSUnresolvedFunction
    dialog.kendoDialog({
        width: "400px",
        title: "Update",
        closable: false,
        modal: false,
        visible: false,
        content: "",
        actions: [
            {text: "Close", primary: true}
        ]
    });
    var dialog_obj = dialog.data("kendoDialog");

    // ************************************ Данные об устройстве СМС Центр ************************************
    // Получить данные об устройстве СМС Центр

    var disableSmsSelftestEvent = false;

    var $sms_update_time = $('#sms-update-time');
    // noinspection JSUnresolvedFunction
    $sms_update_time.kendoNumericTextBox({
        format: "n0",
        min: 1,
        spinners: false
    });
    var $sms_update_time_obj = $sms_update_time.data("kendoNumericTextBox");

    var $sms_mobile = $('#sms-mobile');
    // noinspection JSUnresolvedFunction
    $sms_mobile.kendoMaskedTextBox({
        mask: "+7(999) 000-0000"
    });
    var $sms_mobile_obj = $sms_mobile.data("kendoMaskedTextBox");

    var $sms_alarm = $('#sms-alarm');
    // noinspection JSUnresolvedFunction
    $sms_alarm.kendoMaskedTextBox({
        mask: "+7(999) 000-0000"
    });
    var $sms_alarm_obj = $sms_alarm.data("kendoMaskedTextBox");

    var $sms_selftest_on_init = $('#sms-selftest-on-init');
    // noinspection JSUnresolvedFunction
    $sms_selftest_on_init.kendoMobileSwitch({
        onLabel: "ДА",
        offLabel: "НЕТ",
        checked: false
    });
    var $sms_selftest_on_init_obj = $sms_selftest_on_init.data('kendoMobileSwitch');

    function requestSmsDeviseInfo() {
        disableSmsSelftestEvent = true;

        jQuery.getJSON("/EnergyMonitoringController", "action=SMS_DEVICE_INFO", function (data) {

            /**
             * @typedef {Object} data
             * @property {string} ip
             * @property {string} update_time
             * @property {string} mobile_phone
             * @property {string} emergency_phone
             * @property {string} selftest_on_init
             */

            //console.info("$sms_update_time_obj=" + $sms_update_time_obj);

            $('#sms-ip').html(data.ip);
            $sms_update_time_obj.value(data.update_time);
            $sms_alarm_obj.value(data.emergency_phone);
            $sms_mobile_obj.value(data.mobile_phone);

            if (data.selftest_on_init === 'Y') {
                // noinspection JSUnresolvedFunction
                $sms_selftest_on_init_obj.check(true);
            }
            else {
                // noinspection JSUnresolvedFunction
                $sms_selftest_on_init_obj.check(false);
            }

        });
        disableSmsSelftestEvent = false;
    }

    requestSmsDeviseInfo();

    // Установка времени считывания данных об устройстве Доминатор
    var smsDeviceInfoInterval = setTimeout(function setSmsDeviceInfoRequest() {
        //console.info("ACCUM VALUE Request!");
        requestSmsDeviseInfo();

        smsDeviceInfoInterval = setTimeout(setSmsDeviceInfoRequest, 60000);
    }, 60000);


    $sms_selftest_on_init_obj.bind('change', function (e) {
        //console.info("smsSelftestOnChange() running...");

        if (!disableSmsSelftestEvent) {

            var checkedStatus;
            if (e.checked) {
                checkedStatus = "Y";
            } else {
                checkedStatus = "N";
            }

            /**
             * @typedef {Object} data
             * @property {string} result
             * @property {string} message
             */

            $.post("/SmarthouseAdminController", {method: "SET_SMS_ATTR", value: checkedStatus, id: 10}, function (data) {
                var result = data.result;
                var message = data.message;
                //console.info("SET_SMS_ATTR result=" + result);
                //console.info("SET_SMS_ATTR message=" + message);

                if (result === "error") {
                    dialog_obj.title("Сохранение данных в БД");
                    dialog_obj.content(message);
                    dialog_obj.open();
                    requestSmsDeviseInfo();
                }
            });
        }
    });
	
	// Отправка тестового сообщения
	$('#sms-send-test-message').click(function () {
        console.info("Click sms-send-test-message");

		$.post("/SmarthouseAdminController", {method: "SEND_TEST_SMS"});
    });

    // ************************************ Статус устройств **************************************************
    // Установка времени считывания данных остатусе устройств
    var statusDeviceInfoInterval = setTimeout(function setStatusDeviceInfoRequest() {
        //console.info("ACCUM VALUE Request!");
        requestDeviceActiveValue();

        statusDeviceInfoInterval = setTimeout(setStatusDeviceInfoRequest, 60000);
    }, 60000);


    // **************************************** Навигация *****************************************************
    var $dashboard_favorites_section = $('#dashboard-favorites-section');
    var $dashboard_settings_section = $('#dashboard-settings-section');
    var $dashboard_home_section = $('#dashboard-home-section');

    $dashboard_favorites_section.hide();
    $dashboard_settings_section.hide();

    var $dashboard_faviorites_btn = $('#dashboard-faviorites-btn');
    var $dashboard_settings_btn = $('#dashboard-settings-btn');
    var $dashboard_home_btn = $('#dashboard-home-btn');

    $dashboard_faviorites_btn.fadeTo(0, 0.4);
    $dashboard_settings_btn.fadeTo(0, 0.4);

    var $charts = $('#charts');
    var $dominator = $('#dominator');
    var $dashboard = $('#dashboard');
    var $sms = $('#sms');

    $charts.hide();
    $dominator.hide();
    $sms.hide();


    $dashboard_home_btn.click(function () {
        $dashboard_favorites_section.fadeOut();
        $dashboard_settings_section.fadeOut();
        $dashboard_home_section.fadeIn();

        $dashboard_faviorites_btn.fadeTo(0, 0.4);
        $dashboard_settings_btn.fadeTo(0, 0.4);
        $dashboard_home_btn.fadeTo(0, 1);

        $("#dashboard-power-live-chart").data("kendoChart").refresh();
        resizeComponents();
    });

    $dashboard_faviorites_btn.click(function () {
        $dashboard_home_section.fadeOut();
        $dashboard_settings_section.fadeOut();
        $dashboard_favorites_section.fadeIn();

        $dashboard_settings_btn.fadeTo(0, 0.4);
        $dashboard_home_btn.fadeTo(0, 0.4);
        $dashboard_faviorites_btn.fadeTo(0, 1);

        resizeComponents();
    });

    $dashboard_settings_btn.click(function () {
        $dashboard_home_section.fadeOut();
        $dashboard_favorites_section.fadeOut();
        $dashboard_settings_section.fadeIn();

        $dashboard_home_btn.fadeTo(0, 0.4);
        $dashboard_faviorites_btn.fadeTo(0, 0.4);
        $dashboard_settings_btn.fadeTo(0, 1);

        resizeComponents();
    });


    $('#dashboard-sidepanel-btn').click(function () {
        $charts.fadeOut();
        $dominator.fadeOut();
        $sms.fadeOut();
        $dashboard.fadeIn();

        $("#dashboard-power-live-chart").data("kendoChart").refresh();
        resizeComponents();
    });

    $('#charts-sidepanel-btn').click(function () {
        $dashboard.fadeOut();
        $dominator.fadeOut();
        $sms.fadeOut();
        $charts.fadeIn();
        resizeComponents();
    });

    $('#dominator-sidepanel-btn').click(function () {
        $dashboard.fadeOut();
        $charts.fadeOut();
        $sms.fadeOut();
        $dominator.fadeIn();
        resizeComponents();
    });

    $('#sms-sidepanel-btn').click(function () {
        $dashboard.fadeOut();
        $charts.fadeOut();
        $dominator.fadeOut();
        $sms.fadeIn();
    });

    $('#exit-sidepanel-btn').click(function () {
        window.location.href = "/PageNavigateController?action=toLogoff";
    });

    // *********************************** График со статистической информацией *******************************
    var dmStatData; // Переменная для хранения статистических данных

    // noinspection JSUnresolvedFunction
    $("#dm-stat-chart-fd").kendoDatePicker({
        format: "dd.MM.yyyy"
    });
    // noinspection JSUnresolvedFunction
    $("#dm-stat-chart-td").kendoDatePicker({
        format: "dd.MM.yyyy"
    });

    var statData = [
        {text: "Потребляемая мощность, Вт", value: "PNETL"},
        {text: "Напряжение сети на входе, В", value: "UNET"},
        {text: "Потребляемый ток из сети, А", value: "INET"},
        {text: "Напряжение на выходе, В", value: "UOUTMED"}
    ];

    // noinspection JSUnresolvedFunction
    $('#dm-stat-chart-cell').kendoComboBox({
        dataTextField: "text",
        dataValueField: "value",
        dataSource: statData,
        height: 100
    });

    // noinspection JSUnresolvedFunction
    $('#stat-chart-run').kendoButton({});

    // Создание графика со статистической информацией
    function createDmStatChart() {
        console.info("Run createDmStatChart");
        // noinspection JSUnresolvedFunction
        $('#dashboard-stat-chart').kendoChart({
            renderAs: "canvas",
            dataSource: {
                data: dmStatData
            },
            legend: {
                visible: true
            },
            seriesDefaults: {
                type: "line",
                labels: {
                    visible: false,
                    format: "{0}",
                    background: "transparent"
                },
                color: "#32CD32"
            },
            series: [{
                field: "stat_value",
                labels: {
                    visible: false
                }
            }],
            valueAxis: {
                labels: {
                    format: "{0}"
                },
                line: {
                    visible: true
                }
            },
            categoryAxis: {
                field: "op_date",
                majorGridLines: {
                    visible: true
                },
                labels: {
                    rotation: "auto"
                }
            },
            tooltip: {
                visible: true,
                format: "{0}"
            }
        });

    }

    // Отображение данных на графике со статистической информацией
    $('#dm-stat-chart-run').click(function () {
        console.info("Click");

        var fd = $('#dm-stat-chart-fd').data("kendoDatePicker").value();
        var td = $('#dm-stat-chart-td').data("kendoDatePicker").value();
        var type = $('#dm-stat-chart-cell').data("kendoComboBox").value();
        console.info(fd);
        console.info(td);
        console.info(type);

        if (fd === null || td === null || type === "") {
            alert("Все параметры должны быть заполнены.");
        }
        else if (fd > td) {
            alert("Начальная дата не должна быть больше конечной.");
        }
        else {
            var data = {
                action: "STAT",
                //noinspection
                fd: kendo.toString(fd, "dd.MM.yyyy"),
                td: kendo.toString(td, "dd.MM.yyyy"),
                type: type
            };


            var $dashboard_stat_chart = $("#dashboard-stat-chart");
            // noinspection JSUnresolvedFunction
            var notificationWidget = $("#notification").kendoNotification({
                position: {
                    top: $dashboard_stat_chart.position().top + $dashboard_stat_chart.height() / 2,
                    left: $(".sidebar").width() + $dashboard_stat_chart.width() / 2 - 40
                }
            }).data("kendoNotification");

            notificationWidget.show("Processing...");

            jQuery.getJSON("/EnergyMonitoringController", data, function (data) {
                dmStatData = data;
                createDmStatChart();
                notificationWidget.hide();

            }).fail(function () {
                notificationWidget.hide();
            });


        }
    });

    // *************************************** Напряжение аккумулятора ****************************************

    // Создание графика напряжения аккумулдятора
    function createAccumVoltageChart() {
        // noinspection JSUnresolvedFunction
        $('#dm-accum-voltage-chart').kendoChart({
            legend: {
                visible: false
            },
            chartArea: {margin: 0, padding: 0, width: $('dm-accum-voltage-chart').attr("width")},
            series: [{
                type: "bullet",
                data: accumVoltage
            }],
            categoryAxis: {
                majorGridLines: {
                    visible: false
                },
                majorTicks: {
                    visible: false
                }
            },
            valueAxis: [{
                plotBands: [{
                    from: 22, to: 23, color: "red", opacity: 0.3
                }, {
                    from: 23, to: 25, color: "yellow", opacity: 0.3
                }, {
                    from: 25, to: 28, color: "green", opacity: 0.3
                }],
                majorGridLines: {
                    visible: false
                },
                min: 22,
                max: 28,
                minorTicks: {
                    visible: true
                }
            }],
            tooltip: {
                visible: true,
                template: "#= value.current #"
            }
        });
    }

    createAccumVoltageChart();

    // Установка времени считывания данных для графика напряжения аккумулятора
    var accumVoltageInterval = setTimeout(function setAccumVoltageRequest() {
        //console.info("ACCUM VALUE Request!");
        requestAccumVoltageValue();
        //console.info("accumVoltage=" + accumVoltage);

        //var chart=$("#dm-accum-voltage-chart").data("kendoChart");
        //chart.redraw();
        createAccumVoltageChart();
        kendo.resize($('#dm-accum-voltage-chart'));

        accumVoltageInterval = setTimeout(setAccumVoltageRequest, 60000);
    }, 60000);


    /************************************* Температура аккумулятора ******************************************/

    // Создание графика температуры аккумулятора
    function createAccumTemperatureChart() {
        // noinspection JSUnresolvedFunction
        $('#dm-accum-temp-chart').kendoChart({
            legend: {
                visible: false
            },
            chartArea: {margin: 0, padding: 0, width: $('dm-accum-voltage-chart').attr("width")},
            series: [{
                type: "bullet",
                data: accumTemperature
            }],
            categoryAxis: {
                majorGridLines: {
                    visible: false
                },
                majorTicks: {
                    visible: false
                }
            },
            valueAxis: [{
                plotBands: [{
                    from: 0, to: 5, color: "red", opacity: 0.3
                }, {
                    from: 5, to: 10, color: "yellow", opacity: 0.3
                }, {
                    from: 10, to: 30, color: "green", opacity: 0.3
                }, {
                    from: 30, to: 35, color: "yellow", opacity: 0.3
                }, {
                    from: 35, to: 50, color: "red", opacity: 0.3
                }],
                majorGridLines: {
                    visible: false
                },
                min: 0,
                max: 50,
                minorTicks: {
                    visible: true
                }
            }],
            tooltip: {
                visible: true,
                template: "#= value.current #"
            }
        });
    }

    createAccumTemperatureChart();

    // Установка времени считывания данных для графика температуры аккумулятора
    var accumTemperatureInterval = setTimeout(function setAccumTemperatureRequest() {
        //console.info("ACCUM VALUE Request!");
        requestAccumTemperatureValue();
        //console.info("accumTemperature=" + accumTemperature);

        //var chart=$("#dm-accum-voltage-chart").data("kendoChart");
        //chart.redraw();
        createAccumTemperatureChart();
        kendo.resize($('#dm-accum-temp-chart'));

        accumTemperatureInterval = setTimeout(setAccumTemperatureRequest, 60000);
    }, 60000);


});

$(window).on("resize", function () {
    "use strict";
    //console.info("resize");
    resizeComponents();

});


