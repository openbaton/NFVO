package org.project.neutrino.nfvo.vim.broker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 20/05/15.
 */
@Service
@Scope
public class VimBroker<T> implements org.project.neutrino.nfvo.vim_interfaces.VimBroker<T> {

    @Autowired
    private ConfigurableApplicationContext context;

    @Override
    public T getVim(String type) {
        switch (type) {
            case "test":
                return (T) context.getBean("testVIM");
            case "openstack":
                return (T) context.getBean("openstackVIM");
            case "amazon":
                return (T) context.getBean("amazonVIM");
            default:
                throw new UnsupportedOperationException();
        }
    }
}
