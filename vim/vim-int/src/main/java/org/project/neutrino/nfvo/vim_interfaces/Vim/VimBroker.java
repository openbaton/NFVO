package org.project.neutrino.nfvo.vim_interfaces.Vim;

import org.project.neutrino.nfvo.catalogue.nfvo.Quota;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.common.exceptions.VimException;

/**
 * Created by lto on 20/05/15.
 */
public interface VimBroker {
    Vim getVim(String type);

    Quota getLeftQuota(VimInstance vimInstance) throws VimException;
}
