package ru.net.bogunino84;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;

@Stateful(name = "TL_WA850RE_EJB")
@LocalBean
public class TL_WA850RE_Bean extends IPDevice {
    public TL_WA850RE_Bean() {
    }

    @PostConstruct
    void init() {
        super.initDevice("TL_WA850RE");
    }

    @PreDestroy
    void destroy() {
        super.destroyDevice();
    }
}
