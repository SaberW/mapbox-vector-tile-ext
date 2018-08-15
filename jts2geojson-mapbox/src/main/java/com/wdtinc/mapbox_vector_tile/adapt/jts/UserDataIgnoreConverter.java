package com.wdtinc.mapbox_vector_tile.adapt.jts;

import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;

/**
 * Ignores user data, does not take any action.
 *
 * @see IUserDataConverter
 */
public final class UserDataIgnoreConverter implements IUserDataConverter {
    @Override
    public void addTags(Object userData, MvtLayerProps layerProps,
                        VectorTile.Tile.Feature.Builder featureBuilder) {
    }
}
