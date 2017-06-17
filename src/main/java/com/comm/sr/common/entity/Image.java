package com.comm.sr.common.entity;

import java.io.Serializable;

/**
 * Created by jasstion on 21/04/2017.
 */
public class Image implements Serializable {

    private String imageId=null;

    public Image() {
        super();
    }

    @Override
    public String toString() {
        return "Image{" +
                "imageId='" + imageId + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    private String url=null;

    public Image(String imageId, String url) {
        this.imageId = imageId;
        this.url = url;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Image image = (Image) o;

        if (imageId != null ? !imageId.equals(image.imageId) : image.imageId != null) return false;
        return url != null ? url.equals(image.url) : image.url == null;
    }

    @Override
    public int hashCode() {
        int result = imageId != null ? imageId.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}
