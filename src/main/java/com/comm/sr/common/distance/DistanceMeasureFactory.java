/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.sr.common.distance;

import java.util.Map;

import org.apache.commons.math3.ml.distance.*;

import com.google.common.collect.Maps;

/**
 *
 * @author jasstion
 */
public class DistanceMeasureFactory {

    private final static Map<String, DistanceMeasure> distanceMeasureMap = Maps.newHashMap();

    static {
        distanceMeasureMap.put("chi2", new Chi2DistanceMeasure());
        distanceMeasureMap.put("euclidean", new EuclideanDistance());
        distanceMeasureMap.put("canberra", new CanberraDistance());
        distanceMeasureMap.put("manhattan", new ManhattanDistance());
        distanceMeasureMap.put("chebyshev", new ChebyshevDistance());

    }

    /**
     *
     * @param distanceMeasureName
     * @return
     */
    public static DistanceMeasure createDistanceMeasure(String distanceMeasureName) {
        DistanceMeasure distanceMeasure = null;
        distanceMeasure=distanceMeasureMap.get(distanceMeasureName);
        return distanceMeasure;
    }

}
