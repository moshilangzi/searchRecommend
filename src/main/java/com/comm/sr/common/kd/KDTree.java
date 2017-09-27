package com.comm.sr.common.kd;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.yufei.utils.FileUtil;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * KDTree is a class supporting KD-tree insertion, deletion, equality
 * search, range search, and nearest neighbor(s) using double-precision
 * floating-point keys.  Splitting dimension is chosen naively, by
 * depth modulo K.  Semantics are as follows:
 * <p>
 * <UL>
 * <LI> Two different keys containing identical numbers should retrieve the
 * same value from a given KD-tree.  Therefore keys are cloned when a
 * node is inserted.
 * <BR><BR>
 * <LI> As with Hashtables, values inserted into a KD-tree are <I>not</I>
 * cloned.  Modifying a value between insertion and retrieval will
 * therefore modify the value stored in the tree.
 * </UL>
 * <p>
 * Implements the Nearest Neighbor algorithm (Table 6.4) of
 * <p>
 * <PRE>
 * &*064;techreport{AndrewMooreNearestNeighbor,
 * author  = {Andrew Moore},
 * title   = {An introductory tutorial on kd-trees},
 * institution = {Robotics Institute, Carnegie Mellon University},
 * year    = {1991},
 * number  = {Technical Report No. 209, Computer Laboratory,
 * University of Cambridge},
 * address = {Pittsburgh, PA}
 * }
 * </PRE>
 * <p>
 * Copyright (C) Simon D. Levy and Bjoern Heckel 2014
 * <p>
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code.  If not, see <http:*www.gnu.org/licenses/>.
 * You should also have received a copy of the Parrot Parrot AR.Drone
 * Development License and Parrot AR.Drone copyright notice and disclaimer
 * and If not, see
 * <https:*projects.ardrone.org/attachments/277/ParrotLicense.txt>
 * and
 * <https:*projects.ardrone.org/attachments/278/ParrotCopyrightAndDisclaimer.txt>.
 */
public class KDTree<T> implements Serializable {
    // number of milliseconds
    final long m_timeout;

    // K = number of dimensions
    final private int m_K;

    // root of KD-tree
    private KDNode<T> m_root;

    // count of nodes
    private int m_count;

    /**
     * Creates a KD-tree with specified number of dimensions.
     *
     * @param k number of dimensions
     */

    public KDTree(int k) {
        this(k, 0);
    }

    public KDTree(int k, long timeout) {
        this.m_timeout = timeout;
        m_K = k;
        m_root = null;
    }


    /**
     * Insert a node in a KD-tree.  Uses algorithm translated from 352.ins.c of
     * <p>
     * <PRE>
     * &*064;Book{GonnetBaezaYates1991,
     * author =    {G.H. Gonnet and R. Baeza-Yates},
     * title =     {Handbook of Algorithms and Data Structures},
     * publisher = {Addison-Wesley},
     * year =      {1991}
     * }
     * </PRE>
     *
     * @param key   key for KD-tree node
     * @param value value at that key
     * @throws KeySizeException      if key.length mismatches K
     * @throws KeyDuplicateException if key already in tree
     */
    public void insert(double[] key, T value)
            throws KeySizeException, KeyDuplicateException {
        this.edit(key, new Editor.Inserter<T>(value));
    }

    /**
     * Edit a node in a KD-tree
     *
     * @param key    key for KD-tree node
     * @param editor object to edit the value at that key
     * @throws KeySizeException      if key.length mismatches K
     * @throws KeyDuplicateException if key already in tree
     */

    public void edit(double[] key, Editor<T> editor)
            throws KeySizeException, KeyDuplicateException {

        if (key.length != m_K) {
            throw new KeySizeException();
        }

        synchronized (this) {
            // the first insert has to be synchronized
            if (null == m_root) {
                m_root = KDNode.create(new HPoint(key), editor);
                m_count = m_root.deleted ? 0 : 1;
                return;
            }
        }

        m_count += KDNode.edit(new HPoint(key), editor, m_root, 0, m_K);
    }

    /**
     * Find  KD-tree node whose key is identical to key.  Uses algorithm
     * translated from 352.srch.c of Gonnet & Baeza-Yates.
     *
     * @param key key for KD-tree node
     * @return object at key, or null if not found
     * @throws KeySizeException if key.length mismatches K
     */
    public T search(double[] key) throws KeySizeException {

        if (key.length != m_K) {
            throw new KeySizeException();
        }

        KDNode<T> kd = KDNode.srch(new HPoint(key), m_root, m_K);

        return (kd == null ? null : kd.v);
    }


    public void delete(double[] key)
            throws KeySizeException, KeyMissingException {
        delete(key, false);
    }

    /**
     * Delete a node from a KD-tree.  Instead of actually deleting node and
     * rebuilding tree, marks node as deleted.  Hence, it is up to the caller
     * to rebuild the tree as needed for efficiency.
     *
     * @param key      key for KD-tree node
     * @param optional if false and node not found, throw an exception
     * @throws KeySizeException    if key.length mismatches K
     * @throws KeyMissingException if no node in tree has key
     */
    public void delete(double[] key, boolean optional)
            throws KeySizeException, KeyMissingException {

        if (key.length != m_K) {
            throw new KeySizeException();
        }
        KDNode<T> t = KDNode.srch(new HPoint(key), m_root, m_K);
        if (t == null) {
            if (optional == false) {
                throw new KeyMissingException();
            }
        } else {
            if (KDNode.del(t)) {
                m_count--;
            }
        }
    }

    /**
     * Find KD-tree node whose key is nearest neighbor to
     * key.
     *
     * @param key key for KD-tree node
     * @return object at node nearest to key, or null on failure
     * @throws KeySizeException if key.length mismatches K
     */
    public T nearest(double[] key) throws KeySizeException {

        List<T> nbrs = nearest(key, 1, null);
        return nbrs.get(0);
    }

    /**
     * Find KD-tree nodes whose keys are <i>n</i> nearest neighbors to
     * key.
     *
     * @param key key for KD-tree node
     * @param n   number of nodes to return
     * @return objects at nodes nearest to key, or null on failure
     * @throws KeySizeException if key.length mismatches K
     */
    public List<T> nearest(double[] key, int n)
            throws KeySizeException, IllegalArgumentException {
        return nearest(key, n, null);
    }

    /**
     * Find KD-tree nodes whose keys are within a given Euclidean distance of
     * a given key.
     *
     * @param key key for KD-tree node
     * @param dist   Euclidean distance
     * @return objects at nodes with distance of key, or null on failure
     * @throws KeySizeException if key.length mismatches K
     */
    public List<T> nearestEuclidean(double[] key, double dist)
            throws KeySizeException {
        return nearestDistance(key, dist, new EuclideanDistance());
    }


    /**
     * Find KD-tree nodes whose keys are within a given Hamming distance of
     * a given key.
     *
     * @param key key for KD-tree node
     * @param dist   Hamming distance
     * @return objects at nodes with distance of key, or null on failure
     * @throws KeySizeException if key.length mismatches K
     */
    public List<T> nearestHamming(double[] key, double dist)
            throws KeySizeException {

        return nearestDistance(key, dist, new HammingDistance());
    }


    /**
     * Find KD-tree nodes whose keys are <I>n</I> nearest neighbors to
     * key. Uses algorithm above.  Neighbors are returned in ascending
     * order of distance to key.
     *
     * @param key     key for KD-tree node
     * @param n       how many neighbors to find
     * @param checker an optional object to filter matches
     * @return objects at node nearest to key, or null on failure
     * @throws KeySizeException         if key.length mismatches K
     * @throws IllegalArgumentException if <I>n</I> is negative or
     *                                  exceeds tree size
     */
    public List<T> nearest(double[] key, int n, Checker<T> checker)
            throws KeySizeException, IllegalArgumentException {

        if (n <= 0) {
            return new LinkedList<T>();
        }

        NearestNeighborList<KDNode<T>> nnl = getnbrs(key, n, checker);

        n = nnl.getSize();
        Stack<T> nbrs = new Stack<T>();

        for (int i = 0; i < n; ++i) {
            KDNode<T> kd = nnl.removeHighest();
            nbrs.push(kd.v);
        }

        return nbrs;
    }


    /**
     * Range search in a KD-tree.  Uses algorithm translated from
     * 352.range.c of Gonnet & Baeza-Yates.
     *
     * @param lowk lower-bounds for key
     * @param uppk upper-bounds for key
     * @return array of Objects whose keys fall in range [lowk,uppk]
     * @throws KeySizeException on mismatch among lowk.length, uppk.length, or K
     */
    public List<T> range(double[] lowk, double[] uppk)
            throws KeySizeException {

        if (lowk.length != uppk.length) {
            throw new KeySizeException();
        } else if (lowk.length != m_K) {
            throw new KeySizeException();
        } else {
            List<KDNode<T>> found = new LinkedList<KDNode<T>>();
            KDNode.rsearch(new HPoint(lowk), new HPoint(uppk),
                    m_root, 0, m_K, found);
            List<T> o = new LinkedList<T>();
            for (KDNode<T> node : found) {
                o.add(node.v);
            }
            return o;
        }
    }

    public int size() { /* added by MSL */
        return m_count;
    }

    public String toString() {
        return m_root.toString(0);
    }

    private NearestNeighborList<KDNode<T>> getnbrs(double[] key)
            throws KeySizeException {
        return getnbrs(key, m_count, null);
    }


    private NearestNeighborList<KDNode<T>> getnbrs(double[] key, int n,
                                                   Checker<T> checker) throws KeySizeException {

        if (key.length != m_K) {
            throw new KeySizeException();
        }

        NearestNeighborList<KDNode<T>> nnl = new NearestNeighborList<KDNode<T>>(n);

        // initial call is with infinite hyper-rectangle and max distance
        HRect hr = HRect.infiniteHRect(key.length);
        double max_dist_sqd = Double.MAX_VALUE;
        HPoint keyp = new HPoint(key);

        if (m_count > 0) {
            long timeout = (this.m_timeout > 0) ?
                    (System.currentTimeMillis() + this.m_timeout) :
                    0;
            KDNode.nnbr(m_root, keyp, hr, max_dist_sqd, 0, m_K, nnl, checker, timeout);
        }

        return nnl;

    }

    private List<T> nearestDistance(double[] key, double dist,
                                    DistanceMetric metric) throws KeySizeException {

        NearestNeighborList<KDNode<T>> nnl = getnbrs(key);
        int n = nnl.getSize();
        Stack<T> nbrs = new Stack<T>();

        for (int i = 0; i < n; ++i) {
            KDNode<T> kd = nnl.removeHighest();
            HPoint p = kd.k;
            if (metric.distance(kd.k.coord, key) < dist) {
                nbrs.push(kd.v);
            }
        }

        return nbrs;
    }
public static void main(String[] args) throws Exception {
//
//        KDTree<Integer> kdTree=new KDTree<Integer>(5);
//        double[] p1={1,1,1,1,1};
//    double[] p2={1,2,3,4,5};
//    double[] p3={1,3,6,7,8};
//    kdTree.insert(p1,1);
//    kdTree.insert(p2,2);
//    kdTree.insert(p3,3);
//    double[] t1={1,0,1,7,8};
//    kdTree.nearest(t1,2).forEach(v -> System.out.println(v));
//    System.out.println(kdTree.nearest(t1));

      String path="/data/mlib_data/imageClusterCenterToGroupMappingFile_all_40_6.csv";
      List<String> lines= FileUtil.readLines(path);
      int dimension=2048;
    final KDTree<String> kdTree=new KDTree<String>(dimension);
      lines.forEach(new Consumer<String>() {
          @Override
          public void accept(String line) {
             String[] strs= line.split("@");
             String groupId=strs[0];
             String vec=strs[1].replace("[","").replace("]","");

              double[] vecs = Lists.newArrayList(vec.split(",")).parallelStream()
                      .mapToDouble(va -> Double.parseDouble(va)).toArray();

              try {
                  kdTree.insert(vecs,groupId);
              } catch (Exception e) {
                  e.printStackTrace();
              }

          }
      });
    Stopwatch stopwatch=Stopwatch.createStarted();
      String t1=
              "0.47,0.95,0.13,0.11,0.46,0.12,0.01,0.07,0.07,0.72,0.1,0.13,0.2,0.21,0.53,0.3,0.24,0.36,0.4,0.15,0.28,0.47,0.19,0.07,0.1,0.63,0.05,0.04,0.45,0.09,0.02,0.14,0.32,0.03,0.81,0.19,0.11,0.16,0.14,0.11,0.45,0.43,0.38,0.1,0.1,0.19,0.12,0.14,0.3,0.36,0.1,0.34,0.2,0.15,0.12,0.2,0.32,0.01,0.45,0.17,0.28,0.22,0.03,0.1,0.16,0.2,0.27,0.01,0.12,0.14,0.21,0.18,0.25,0.1,0.11,0.13,0.97,0.2,0.15,0.5,0.73,0.04,0.16,0.57,0.11,0.07,0.41,0.48,0.07,0.08,0.42,0.69,0.18,0.24,0.11,0.08,0.25,0.24,1.33,0.05,0.34,0.12,0.11,0.08,0.15,0.01,0.08,0.13,0.12,0.79,0.49,0.87,0.25,0.22,0.31,0.03,0.11,0.37,0.12,0.52,0.44,0.27,0.48,0.34,0.26,0.17,0.5,0.21,0.28,0.95,0.57,0.37,0.31,0.07,0.51,0.12,0.43,0.09,0.0,0.18,0.54,0.02,0.28,0.38,0.9,0.03,0.76,0.43,0.22,0.17,0.21,0.57,1.19,0.4,0.17,0.52,0.01,0.05,0.11,0.14,0.08,0.84,0.82,0.24,0.1,0.34,0.04,0.11,0.49,0.22,0.08,0.7,0.49,0.11,0.79,0.66,0.26,0.3,0.15,0.27,0.27,0.14,0.28,0.12,0.92,0.18,0.79,0.77,0.22,0.46,0.46,0.44,0.59,0.62,0.14,0.02,0.61,0.13,0.09,0.14,0.4,0.12,0.24,0.06,0.18,0.02,0.22,0.67,0.37,0.33,0.09,0.08,0.12,0.06,0.32,0.11,0.17,0.28,0.82,0.08,0.12,0.09,0.7,0.46,0.08,0.96,0.86,0.06,0.71,0.16,0.44,0.32,0.03,0.02,0.28,0.32,0.07,0.76,0.59,0.07,0.28,0.19,0.06,0.43,0.04,0.78,0.05,0.07,0.16,0.28,0.03,0.07,0.32,0.22,0.32,0.04,0.17,0.26,0.06,0.41,0.33,0.23,0.12,0.06,0.08,0.1,0.21,0.17,0.34,0.07,0.1,0.31,0.12,0.03,0.13,0.03,0.11,0.03,0.12,0.94,0.38,0.26,0.36,0.07,0.22,0.2,0.33,0.19,0.36,0.33,0.44,0.04,0.06,0.15,0.35,0.23,0.24,0.11,1.1,0.1,0.04,0.22,0.1,0.12,0.03,0.28,0.14,0.27,0.08,0.03,0.29,0.11,0.06,0.17,0.36,0.28,0.19,0.15,0.22,0.06,0.27,0.14,0.03,0.18,0.16,0.13,0.1,0.06,0.03,0.4,0.06,0.16,0.07,0.1,0.35,0.12,0.23,0.07,0.23,0.09,0.61,0.06,0.05,0.42,0.29,0.17,0.11,0.23,0.28,1.24,0.17,0.04,0.09,0.14,0.01,0.12,0.3,0.13,0.21,0.3,0.12,0.28,0.13,0.04,0.15,0.12,0.3,0.05,0.2,0.13,0.31,0.16,0.12,0.19,0.08,0.08,0.08,0.03,0.02,0.06,0.07,0.12,0.08,0.07,0.56,0.08,0.15,0.01,0.31,0.05,0.36,0.06,0.12,0.18,0.02,0.02,0.01,0.06,0.12,0.11,0.33,0.08,0.14,0.39,0.16,0.12,0.11,0.27,0.12,0.13,0.58,0.06,0.12,0.12,0.1,0.08,0.28,0.06,0.28,0.03,0.34,0.3,0.03,0.21,0.02,0.09,0.18,0.11,0.34,0.03,0.29,0.21,0.07,0.18,0.17,0.19,0.05,0.02,0.88,0.3,0.14,0.15,0.12,0.15,0.2,0.01,0.14,0.0,0.08,0.52,0.36,0.13,0.04,0.09,0.18,0.18,0.56,0.05,0.22,0.2,0.15,0.01,0.19,0.1,0.0,0.02,0.74,1.36,0.41,0.1,0.46,0.87,0.09,0.06,0.04,0.06,0.13,0.34,0.23,0.48,0.05,0.45,0.31,0.48,0.02,0.24,0.27,0.13,0.65,0.04,0.18,0.31,0.0,0.06,0.06,0.22,0.04,0.2,0.13,0.15,0.12,0.23,0.39,0.13,0.37,0.3,0.17,0.2,0.09,0.11,0.16,0.03,0.18,0.24,0.34,0.17,0.75,0.28,0.19,0.14,0.14,0.18,0.25,0.39,0.13,0.25,0.22,0.14,0.16,0.29,0.14,0.08,0.31,0.07,0.24,0.58,0.26,0.74,0.06,0.14,0.56,0.09,0.16,0.01,0.23,0.41,0.29,0.16,0.18,0.1,0.18,0.09,0.22,0.14,0.21,0.07,0.15,0.13,0.32,0.53,0.39,0.17,0.08,0.11,0.14,0.05,0.04,0.1,0.31,0.08,0.03,0.26,0.2,0.07,0.05,0.05,0.1,0.01,0.4,0.02,0.03,0.21,0.27,0.77,0.03,0.36,0.16,0.05,0.3,0.24,0.12,0.08,0.09,0.13,0.27,0.19,0.2,0.09,0.1,0.23,0.14,0.02,0.74,0.11,0.09,0.24,0.06,0.06,0.29,0.06,0.15,0.05,0.18,0.26,0.31,0.28,0.01,0.04,0.16,0.03,0.4,0.17,0.13,0.2,0.69,0.38,0.57,0.13,0.06,0.3,0.19,0.1,0.15,0.19,0.09,0.14,0.07,0.09,0.13,0.09,0.04,0.15,0.17,0.28,0.07,0.17,0.12,0.09,0.2,0.08,0.07,0.11,0.27,1.08,0.15,0.06,0.04,0.33,0.47,0.04,0.32,0.23,0.05,0.18,0.44,0.23,0.11,0.08,0.63,0.15,1.67,0.01,2.33,0.35,0.18,0.08,0.02,0.06,0.02,0.32,0.09,0.1,0.38,0.07,0.14,0.01,0.39,0.38,0.21,0.01,0.04,0.56,0.28,0.25,0.27,0.33,0.08,0.02,0.19,0.06,0.29,0.04,0.05,0.27,0.14,0.26,0.35,0.06,0.18,0.17,0.01,0.22,0.11,0.25,0.2,0.11,0.02,0.09,0.41,0.06,0.33,0.04,0.02,0.11,0.08,0.1,0.25,0.05,0.28,0.06,0.06,0.29,0.07,0.18,0.13,0.06,0.15,0.07,0.08,0.13,0.43,0.11,0.23,0.3,0.26,0.2,0.23,0.3,0.13,0.76,0.15,0.34,0.18,0.19,0.12,0.07,0.06,0.09,0.0,0.12,0.33,0.02,0.37,0.33,0.07,0.14,1.34,0.09,0.18,0.14,0.09,0.22,0.25,0.21,0.33,0.31,0.22,0.07,0.13,0.65,0.12,0.27,0.1,0.25,0.17,0.38,0.05,0.16,0.13,0.13,0.17,0.11,0.01,0.08,0.35,0.14,0.11,0.16,0.3,0.42,0.06,0.05,0.09,0.1,0.26,0.85,0.04,0.31,0.1,0.11,0.14,0.66,0.18,0.1,0.67,0.06,0.17,0.14,0.07,0.18,0.07,0.31,0.13,0.17,0.35,0.03,0.01,0.04,0.18,0.21,0.16,0.3,0.03,0.38,0.06,0.21,0.54,0.31,0.18,0.29,0.0,0.19,0.1,0.21,0.26,0.82,0.92,0.24,0.13,0.0,0.28,0.15,0.12,0.08,0.04,0.24,0.05,0.33,0.05,0.11,0.21,0.01,0.28,0.25,0.32,0.05,0.3,0.6,0.18,0.16,0.34,0.03,0.17,0.11,0.06,0.14,0.57,0.39,0.39,0.3,0.08,0.05,0.1,0.23,0.48,0.2,0.05,0.09,0.07,0.01,0.06,0.08,0.0,0.47,0.01,0.16,0.02,0.56,0.04,0.04,0.13,0.12,0.2,0.04,0.22,0.1,0.13,0.65,0.31,0.6,0.1,0.35,0.15,0.16,0.01,0.16,0.58,0.03,0.19,0.07,0.04,0.09,0.07,0.05,0.16,0.29,0.12,0.16,0.02,0.27,0.28,0.2,0.13,0.01,0.52,0.03,0.13,0.29,0.04,0.14,0.04,0.06,0.04,0.23,0.05,0.08,0.17,0.07,0.08,0.22,0.04,0.11,0.37,0.52,0.36,0.12,0.25,0.32,0.37,0.29,0.17,0.33,0.09,0.05,0.34,0.19,0.14,0.24,0.53,0.14,0.53,0.12,0.44,0.21,0.0,0.0,0.04,0.37,0.28,0.47,0.3,0.13,0.14,0.33,0.02,0.03,0.22,0.09,0.1,0.11,0.03,0.47,0.31,0.12,0.23,0.01,0.31,0.19,0.1,0.13,0.16,0.27,0.05,0.2,0.37,0.57,0.34,0.26,0.18,0.18,0.3,0.77,0.09,0.14,0.04,0.07,0.17,1.54,0.31,0.05,0.16,0.05,0.11,0.06,0.02,0.13,0.11,0.21,0.13,0.17,0.54,0.29,0.13,0.35,0.46,0.13,0.35,0.08,0.22,0.19,0.66,0.37,0.05,0.26,0.03,0.07,0.37,0.1,0.26,0.17,0.63,0.41,0.14,0.27,0.11,0.27,0.26,0.1,0.71,0.12,0.74,0.66,0.53,0.1,0.42,0.88,0.07,0.02,0.17,0.47,0.21,0.19,0.2,0.24,0.28,0.22,0.15,0.03,0.01,0.11,0.08,0.02,0.02,0.11,0.2,0.22,0.07,0.42,0.04,0.0,0.12,0.0,0.24,0.18,0.08,0.44,0.31,0.14,0.42,0.03,0.07,0.09,0.03,0.04,0.48,0.27,0.03,0.14,0.56,0.03,0.09,0.05,0.55,0.03,0.1,0.03,0.55,0.0,0.05,0.06,0.53,0.12,0.06,0.14,0.27,0.52,0.19,0.24,0.36,0.02,0.17,1.19,0.0,0.15,0.13,0.29,0.11,0.32,0.03,0.2,0.53,0.01,0.89,0.24,0.07,0.05,0.02,1.12,0.62,0.03,0.01,0.13,0.47,0.01,0.01,0.09,0.01,0.06,0.12,0.71,0.16,0.05,0.06,0.1,0.14,0.27,0.12,0.22,0.11,0.05,0.0,0.06,0.04,0.22,0.03,0.08,0.22,0.08,0.19,0.03,0.26,0.47,1.29,0.06,0.14,0.17,0.2,0.01,0.14,0.92,0.0,0.1,0.22,0.09,0.27,0.03,0.23,0.03,0.07,0.04,0.02,0.04,0.0,0.32,0.27,0.15,0.37,0.38,1.45,0.21,1.47,0.0,0.06,0.12,0.2,0.12,0.78,0.34,0.03,0.11,0.38,0.59,0.69,0.1,0.03,0.31,0.16,0.03,0.01,0.07,0.11,0.05,0.25,0.03,0.0,0.34,0.27,0.61,0.33,0.03,0.41,0.43,0.21,0.24,0.04,0.09,0.23,0.07,0.38,0.27,0.21,0.07,0.09,0.03,0.14,0.73,0.03,0.09,0.54,0.08,0.04,0.24,0.04,1.22,0.07,0.39,0.32,0.08,0.12,0.1,0.13,0.01,0.02,0.03,0.07,0.0,0.01,0.09,0.26,0.01,0.02,0.29,0.01,0.19,0.43,0.43,0.02,0.02,0.33,0.3,0.33,0.02,0.53,0.03,0.0,0.66,0.26,0.05,0.0,1.0,0.83,0.21,0.41,0.02,0.77,0.04,0.02,0.07,1.31,0.02,0.04,0.16,0.4,0.63,1.0,0.0,0.26,0.07,0.17,0.81,0.12,0.28,0.01,0.55,0.02,0.01,0.46,0.05,0.03,0.01,0.34,0.56,0.2,0.03,0.0,0.02,0.23,0.08,0.9,0.0,0.3,0.03,0.02,0.2,0.26,0.33,0.3,0.0,0.02,0.19,0.29,0.0,0.29,0.02,0.04,0.1,0.01,0.05,0.57,0.21,0.36,0.26,0.13,0.2,0.23,0.03,0.31,0.46,0.47,0.08,0.1,0.0,0.06,0.47,0.49,0.07,0.04,0.15,0.13,0.35,0.49,0.8,0.31,0.74,0.25,0.82,0.03,0.31,0.03,0.05,0.55,0.05,0.39,0.11,1.08,0.03,0.09,0.51,0.1,0.05,0.07,0.08,0.11,0.18,0.19,0.02,0.44,0.29,0.09,0.38,0.15,0.01,0.21,0.01,0.12,0.01,0.94,0.28,0.48,0.01,0.21,0.16,0.16,0.13,0.11,0.1,0.07,0.76,0.13,0.45,0.01,0.38,0.05,0.36,0.26,0.03,0.08,0.21,0.39,0.07,0.03,0.0,0.03,0.13,0.14,0.21,0.73,0.33,0.73,1.25,0.0,0.14,0.46,0.01,0.0,0.05,0.0,0.08,0.14,0.22,0.43,0.11,0.07,0.0,0.27,0.55,0.08,0.76,0.24,0.16,0.02,0.39,0.2,0.03,0.55,0.23,0.33,0.14,0.69,0.06,1.63,0.03,0.09,0.26,0.46,0.15,0.23,0.04,0.0,0.04,1.26,0.09,0.4,1.02,0.05,0.48,0.66,0.37,0.01,0.21,0.08,0.62,0.11,0.95,0.18,0.0,0.03,0.21,0.0,0.21,1.0,0.16,0.42,0.12,0.26,0.09,0.01,0.91,0.13,0.35,0.08,0.31,0.4,0.23,0.16,0.69,0.06,0.75,0.41,0.78,0.22,0.71,0.2,0.46,0.36,0.0,0.17,0.02,0.54,0.07,0.11,0.09,0.05,0.77,0.2,0.08,0.24,0.16,0.18,0.53,0.02,0.04,0.0,0.0,0.06,0.19,0.21,0.11,0.01,0.03,0.0,0.65,0.0,0.02,0.0,0.0,0.08,0.18,0.23,0.13,0.32,0.05,1.21,0.55,0.17,0.15,0.15,0.01,0.49,0.05,0.15,0.36,0.17,0.26,0.02,0.44,0.47,0.14,0.52,0.11,0.53,1.26,0.06,0.19,0.07,0.32,0.19,0.39,0.14,0.01,0.15,0.31,0.04,0.18,0.01,0.51,0.19,1.15,0.01,0.42,0.06,0.02,0.11,0.19,0.07,0.19,0.14,0.18,0.01,0.04,0.04,0.04,0.2,0.12,1.08,0.01,0.36,0.54,0.21,0.35,0.14,0.02,0.49,0.32,0.68,0.03,0.16,0.18,0.12,0.18,0.9,0.07,0.01,0.25,0.0,0.0,0.17,0.38,0.0,0.21,0.12,0.16,0.04,0.07,0.03,0.01,1.33,1.08,0.03,0.02,0.05,0.04,0.29,0.14,0.46,0.02,0.21,0.01,0.0,0.16,0.24,0.26,0.24,0.27,0.23,0.11,0.58,0.0,0.17,0.43,0.18,0.64,0.15,0.02,0.26,0.16,0.06,0.05,0.18,0.11,0.56,0.0,0.62,0.15,0.31,0.24,0.0,0.31,0.39,0.16,0.25,0.02,0.34,0.13,0.16,0.0,0.22,0.1,0.14,0.45,0.01,0.13,0.02,0.27,0.02,0.05,1.53,0.05,0.15,0.25,0.4,0.06,0.03,0.37,0.04,0.44,0.3,0.01,0.09,0.1,0.01,0.0,0.47,0.48,0.09,0.68,0.01,0.23,0.13,0.68,0.63,0.04,0.01,0.41,0.97,0.3,0.19,0.01,0.02,0.28,0.07,0.14,0.01,0.02,0.22,0.37,0.16,0.5,1.36,0.23,0.06,0.04,0.13,0.05,0.16,0.08,0.17,0.0,0.02,0.02,0.1,0.95,0.21,0.09,0.17,0.01,0.07,0.33,0.05,0.01,0.18,0.42,0.13,0.18,0.58,0.65,0.01,0.3,0.05,0.34,0.14,0.02,0.0,0.0,0.43,0.84,0.02,0.39,0.09,0.21,0.07,0.01,0.11,0.05,0.18,0.53,0.1,0.23,0.59,0.4,0.06,0.28,0.39,0.28,0.02,0.14,0.04,0.05,1.26,0.93,0.02,0.01,0.02,0.07,0.02,0.18,0.95,0.81,0.16,0.0,0.01,0.24,0.08,0.73,0.15,0.01,0.02,0.02,0.56,0.46,0.01,0.43,0.01,0.26,0.12,0.1,0.03,0.07,0.01,0.64,0.87,0.01,0.24,0.43,0.14,0.0,0.39,0.32,0.13,0.91,0.3,0.12,0.08,0.29,0.02,0.21,0.23,0.32,0.34,1.25,0.01,1.16,0.15,0.16,0.3,0.04,1.24,0.14,0.27,0.26,0.13,0.2,0.13,0.04,0.11,0.06,0.77,0.3,0.33,0.74,0.35,0.04,0.16,0.23,0.29,0.0,0.66,0.16,0.09,0.88,0.21,1.05,0.02,0.08,0.22,0.07,0.25,0.13,0.0,0.16,0.05,0.18,0.6,0.1,0.24,0.5,0.41,0.02,0.27,0.28,0.03,0.05,0.11,0.17,0.3,0.17,0.33,0.03,0.27,0.03,0.04,0.42,0.04,0.14,0.31,0.29,0.19,0.25,0.01,0.12,0.2,0.05,0.14,0.09,0.17,0.22,0.94,0.17,0.49,0.58,1.04,0.26,0.35,0.07,0.0,0.72,0.82,0.11,0.08,0.7,0.0,1.1,0.46,0.24,0.05,0.14,0.05,0.33,0.1,0.09,0.37,0.21,0.15,0.05,0.07,0.99,0.17,0.19,0.32,0.14,0.31,0.05,0.13,0.08,0.03,0.4,0.73,0.12,0.15,0.24,0.0,0.02,0.4,0.15,0.13,0.05,0.41,0.44,0.06,0.28,0.46,0.52,0.0,0.27,0.3,0.74,0.19,0.39,1.26,0.27,0.85,0.89,0.73,0.08,0.09,0.5,0.06,0.21,0.77,0.27,0.09,0.58,0.01,1.69,0.82,0.03,0.03,0.51,0.02,0.03,0.11,0.32,0.2,1.53,1.47,0.03,0.18,0.14,0.32,1.46,0.29,0.12,0.02,0.18,0.4,0.41,0.31,0.03";
      System.out.println(kdTree.size());
      double[] t1s=Lists.newArrayList(t1.split(",")).parallelStream()
              .mapToDouble(va -> Double.parseDouble(va)).toArray();

    System.out.println(kdTree.nearest(t1s));
    stopwatch.stop();
    System.out.println(stopwatch.elapsed(TimeUnit.MILLISECONDS));
    kdTree.nearest(t1s,5).forEach(v -> System.out.println(v));
    System.out.println("*********************directly compute against*********************");








}

}

