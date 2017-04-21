/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comm.sr.common.distance;

import org.apache.commons.math3.ml.distance.DistanceMeasure;

/**
 *
 * @author jasstion
 */
public  class Chi2DistanceMeasure implements DistanceMeasure{

        @Override
        public double compute(double[] a, double[] b) {
            double result=0d;
//            d=0.5* np.sum([((a-b)**2)/(a+b+eps)
//                   for (a, b) in zip(histA, histB)])
            for (int i = 0; i <a.length; i++) {
                double a_=a[i];
                double b_=b[i];
                result+=Math.pow((a_-b_),2)/(a_+b_+0.000000001); 
                
            }
            return result*0.5;
        }
        
    }