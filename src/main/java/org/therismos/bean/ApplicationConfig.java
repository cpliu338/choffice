package org.therismos.bean;

import java.util.Set;

/**
 *
 * @author cp_liu
 */
@jakarta.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends jakarta.ws.rs.core.Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(org.therismos.bean.FilesResource.class);
        resources.add(org.therismos.bean.Jobs.class);
        resources.add(org.therismos.bean.NonSqlDatasource.class);
        resources.add(org.therismos.bean.ReconcileResource.class);
        resources.add(org.therismos.bean.SmbResource.class);
    }
    
}
