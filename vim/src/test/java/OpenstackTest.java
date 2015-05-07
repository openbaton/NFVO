import org.project.neutrino.vim.implementations.openstack.ResourceManagement;

import java.io.IOException;

/**
 * Created by mpa on 07.05.15.
 */
public class OpenstackTest {

    public static void main(String[] argv) throws IOException{
        ResourceManagement resourceManagement = new ResourceManagement();
        resourceManagement.allocate(null);


    }

}
