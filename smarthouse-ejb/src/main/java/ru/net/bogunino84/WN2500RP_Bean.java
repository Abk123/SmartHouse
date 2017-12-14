package ru.net.bogunino84;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;

@Stateful(name = "WN2500RP_EJB")
@LocalBean
public class WN2500RP_Bean extends IPDevice {
    public WN2500RP_Bean() {
    }

    @PostConstruct
    void init() {
        super.initDevice("WN2500RP");
    }

    @PreDestroy
    void destroy() {
        super.destroyDevice();
    }
}
