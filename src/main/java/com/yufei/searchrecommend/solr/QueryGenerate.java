package com.yufei.searchrecommend.solr;

/**
 * this implementation will be replaced by new query generate framework
 * Created by jasstion on 15/6/30.
 */
@Deprecated
public class QueryGenerate {

    public static String generateSimilarUsersQuery(int age, int city, int height) {
        StringBuffer queryBuffer = new StringBuffer();
        int agespace = 5, heightspace = 10;
        int hage = age + agespace;
        int lowage = age - agespace;
        int hheight = height + heightspace;
        int lheight = height - heightspace;
        queryBuffer.append("age:[" + lowage + " TO " + hage + "]");
        queryBuffer.append(" AND ");
        queryBuffer.append("height:[" + lheight + " TO " + hheight + "]");
        if (city > 0) {
            queryBuffer.append(" AND ");
            queryBuffer.append("city:" + city + "");
        }

        return queryBuffer.toString();
    }

    public static String generateInteredtedUsersQuery(int hage, int lowage, int hheight, int lheight, int[] cities) {
        StringBuffer queryBuffer = new StringBuffer();
        if (hage > 0 && lowage > 0) {
            queryBuffer.append("age:[" + lowage + " TO " + hage + "]");
            queryBuffer.append(" AND ");
        }

        if (hheight > 0 && lheight > 0) {
            queryBuffer.append("height:[" + lheight + " TO " + hheight + "]");
            queryBuffer.append(" AND ");
        }
        if (cities != null) {
            queryBuffer.append("city:(");
            for (int city : cities) {
                queryBuffer.append(city + " OR ");
            }
            int or = queryBuffer.lastIndexOf("OR");
            queryBuffer.replace(or, or + 2, "");
            queryBuffer.append(")");

        }
        if (cities == null) {
            int and = queryBuffer.lastIndexOf("AND");
            queryBuffer.replace(and, and + 3, "");
        }

        return queryBuffer.toString();
    }

   
}
