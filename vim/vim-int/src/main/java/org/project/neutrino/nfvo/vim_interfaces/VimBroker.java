package org.project.neutrino.nfvo.vim_interfaces;

/**
 * Created by lto on 20/05/15.
 */
public interface VimBroker<T> {
    T getVim(String type);
}
