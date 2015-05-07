package org.project.neutrino.vim.implementations;

import org.project.neutrino.vim.interfaces.*;

/**
 * Created by mpa on 05.05.15.
 */
public class OpenStack {
    //implements ImageManagement, NetworkForwardingPathManagement, PolicyManagement, ResourceCapacityManagement, ResourceCatalogueMangement, ResourceFaultManagement, ResourceManagement, ResourcePerformanceManagement {
    public class Image implements ImageManagement{
        public void add() {
            imageManagementInterface.add();
        }

        public void delete() {
            imageManagementInterface.delete();
        }

        public void update() {
            imageManagementInterface.update();
        }

        public void query() {
            imageManagementInterface.query();
        }

        public void copy() {
            imageManagementInterface.copy();
        }
    }

    private ImageManagement imageManagementInterface;
    private NetworkForwardingPathManagement networkForwardingPathManagementInterface;
    private PolicyManagement policyManagementInterface;
    private ResourceCapacityManagement resourceCapacityManagementInterface;
    private ResourceCatalogueMangement resourceCatalogueMangementInterface;
    private ResourceFaultManagement resourceFaultManagementInterface;
    private ResourceManagement resourceManagementInterface;
    private ResourcePerformanceManagement resourcePerformanceManagementInterace;

}
