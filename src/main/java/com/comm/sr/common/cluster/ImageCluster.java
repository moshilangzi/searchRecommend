package com.comm.sr.common.cluster;

import com.google.common.collect.Lists;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import java.util.List;

/**
 * Created by jasstion on 24/04/2017.
 */
public class ImageCluster {
    public static void main(String[] args){
        KMeansPlusPlusClusterer<ImageScoreWrapper> clusterer = new KMeansPlusPlusClusterer<ImageScoreWrapper>(5, 100);
        List<ImageScoreWrapper> imageScoreWrappers= Lists.newArrayList();
        double[] va0={6.272104d};
        double[] va1={11.751347d};
        double[] va2={11.888083d};
        double[] va3={11.888332d};
        double[] va4={12.078166d};
        double[] va5={12.25026d};
        double[] va6={12.368413d};
        double[] va7={12.436117d};
        double[] va8={12.599823d};
        double[] va9={12.699447d};


        imageScoreWrappers.add(new ImageScoreWrapper("1",va0));
        imageScoreWrappers.add(new ImageScoreWrapper("2",va1));
        imageScoreWrappers.add(new ImageScoreWrapper("3",va2));
        imageScoreWrappers.add(new ImageScoreWrapper("4",va3));
        imageScoreWrappers.add(new ImageScoreWrapper("5",va4));
        imageScoreWrappers.add(new ImageScoreWrapper("6",va5));
        imageScoreWrappers.add(new ImageScoreWrapper("7",va6));
        imageScoreWrappers.add(new ImageScoreWrapper("8",va7));
        imageScoreWrappers.add(new ImageScoreWrapper("9",va8));
        imageScoreWrappers.add(new ImageScoreWrapper("0",va9));
        List<CentroidCluster<ImageScoreWrapper>> clusterResults = clusterer.cluster(imageScoreWrappers);
        CentroidCluster<ImageScoreWrapper> topCentroidClusterGroup=clusterResults.get(0);
        for (int i=0; i<clusterResults.size(); i++) {
            System.out.println("Cluster " + i);
            for (ImageScoreWrapper locationWrapper : clusterResults.get(i).getPoints()) {
                if (clusterResults.get(i).getCenter().getPoint()[0] < topCentroidClusterGroup.getCenter().getPoint()[0]) {
                    topCentroidClusterGroup = clusterResults.get(i);

                }
                System.out.println(locationWrapper.getImageId());
            }
            System.out.println();
        }

        System.out.println(topCentroidClusterGroup.getPoints().get(0).getImageId());
    }


    public static class ImageScoreWrapper implements Clusterable {
        private double[] score;
        private String imageId;

        public ImageScoreWrapper(String imageId,double[] score) {
           this.score=score;
           this.imageId=imageId;
        }



        public String getImageId() {
            return imageId;
        }

        public void setImageId(String imageId) {
            this.imageId = imageId;
        }

        @Override
        public double[] getPoint() {
            return score;
        }
    }
}
