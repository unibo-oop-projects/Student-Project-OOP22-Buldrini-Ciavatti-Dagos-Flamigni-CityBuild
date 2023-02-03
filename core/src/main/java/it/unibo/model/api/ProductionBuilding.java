package it.unibo.model.api;

import java.util.Map;

/**This interface is used to Model any building that produces resources */
public interface ProductionBuilding {

    /** */
    public int getResource();

    /** */
    public Map<Resource, Integer> getCostUpgrade();
    
    /** */
    public void upgrade(final Map<Resource, Integer> resourcesForUpgrade);

}