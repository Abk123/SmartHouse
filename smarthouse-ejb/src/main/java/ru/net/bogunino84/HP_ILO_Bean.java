package ru.net.bogunino84;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;

@Stateful(name = "HP_ILO_EJB")
@LocalBean
public class HP_ILO_Bean extends IPDevice {
    public HP_ILO_Bean() {
    }

    @PostConstruct
    void init() {
        super.initDevice("HP_ILO");
    }

    @PreDestroy
    void destroy() {
        super.destroyDevice();
    }
}
