package org.project.openbaton.nfvo.core.interfaces;

import org.project.openbaton.nfvo.catalogue.mano.descriptor.VirtualLinkDescriptor;
import org.project.openbaton.nfvo.catalogue.mano.record.VirtualLinkRecord;

import java.util.List;

/**
 * Created by lto on 11/06/15.
 */
public interface VirtualLinkManagement {
    VirtualLinkDescriptor add(VirtualLinkDescriptor virtualLinkDescriptor);

    VirtualLinkRecord add(VirtualLinkRecord virtualLinkRecord);

    void delete(String id);

    VirtualLinkDescriptor update(VirtualLinkDescriptor virtualLinkDescriptor_new, String id);

    VirtualLinkRecord update(VirtualLinkRecord virtualLinkRecord_new, String id);

    List<VirtualLinkDescriptor> queryDescriptors();

    List<VirtualLinkRecord> queryRecords();

    VirtualLinkRecord queryRecord(String id);

    VirtualLinkDescriptor queryDescriptor(String id);
}
