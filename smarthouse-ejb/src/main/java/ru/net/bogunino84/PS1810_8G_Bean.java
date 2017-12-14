package ru.net.bogunino84;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;

@Stateful(name = "PS1810_8G_EJB")
@LocalBean
public class PS1810_8G_Bean extends IPDevice {
    public PS1810_8G_Bean() {
    }

    @PostConstruct
    void init() {
        super.initDevice("PS1810_8G");
    }

    @PreDestroy
    void destroy() {
        super.destroyDevice();
    }
}
