/* (c) 2014 - 2015 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.catalog;


/**
 * Interface for publishable entities contained in a Layer Group.
 *
 * @author Davide Savazzi - geo-solutions.it
 */
public interface PublishedInfo {

    /** Returns the name. */
    String getName();

    /** Sets the name. */
    void setName(String name);

    @Deprecated
    public String getPrefixedName();

    /**
     * The derived prefixed name.
     *
     * <p>If a workspace is set this method returns:
     *
     * <pre>
     *   getWorkspace().getName() + ":" + getName();
     * </pre>
     *
     * Otherwise it simply returns:
     *
     * <pre>getName()</pre>
     */
    String prefixedName();

    /** Returns the title. */
    String getTitle();

    /** Sets the title. */
    void setTitle(String title);

    /** Returns the abstract. */
    String getAbstract();

    /** Sets the abstract. */
    void setAbstract(String abstractTxt);

    /** The type of the layer. */
    PublishedType getType();
}
