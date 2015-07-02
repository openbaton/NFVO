package org.project.openbaton.nfvo.vim_interfaces.vim;

import org.project.openbaton.nfvo.catalogue.nfvo.Quota;
import org.project.openbaton.nfvo.catalogue.nfvo.VimInstance;
import org.project.openbaton.nfvo.common.exceptions.VimException;

/**
 * Created by lto on 20/05/15.
 */
public interface VimBroker {
    Vim getVim(String type);

    Quota getLeftQuota(VimInstance vimInstance) throws VimException;
}
