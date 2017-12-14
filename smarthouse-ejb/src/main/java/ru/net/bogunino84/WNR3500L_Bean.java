package ru.net.bogunino84;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;

@Stateful(name = "WNR3500L_EJB")
@LocalBean
public class WNR3500L_Bean extends IPDevice {
    public WNR3500L_Bean() {
    }

    @PostConstruct
    void init() {
        super.initDevice("WNR3500L");
    }

    @PreDestroy
    void destroy() {
        super.destroyDevice();
    }
}
