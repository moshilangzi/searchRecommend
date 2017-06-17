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
              "0.78, -0.27, -0.46, -0.28, -0.13, -0.22, 0.02, -0.17, -0.04, 0.41, -0.27, -0.30, 0.26, -0.07, 0.22, -0.07, -0.06, 0.11, -0.42, 0.14, -0.04, -0.07, 0.14, 0.02, -0.24, -0.22, -0.24, 0.06, -0.21, 0.10, -0.17, 0.10, 0.39, -0.13, -0.08, -0.34, 0.55, 0.19, -0.08, -0.06, -0.28, -0.06, 0.21, -0.13, -0.39, 0.32, 0.36, -0.06, -0.21, 0.11, -0.08, -0.10, -0.16, 0.25, 0.55, -0.46, 0.53, 0.06, -0.17, 0.07, -0.07, -0.34, 0.28, -0.06, 0.14, 0.39, -0.22, -0.12, -0.32, -0.12, -0.45, 0.06, 0.05, -0.10, -0.47, -0.16, -0.24, 0.10, 0.30, 0.09, 0.34, 0.14, -0.01, 0.56, -0.50, -0.21, -0.29, -0.12, -0.30, -0.14, -0.18, 0.01, -0.29, 0.08, 0.05, 0.21, -0.13, 0.07, -0.26, -0.01, 0.23, -0.16, -0.02, -0.09, 0.39, -0.19, -0.16, 0.14, -0.33, -0.19, 0.05, 0.00, 0.00, 0.31, -0.01, -0.15, -0.39, -0.18, -0.20, 0.16, -0.18, 0.46, 0.27, 0.10, -0.30, -0.01, 0.56, -0.03, -0.35, 0.45, -0.16, 0.33, -0.24, -0.50, -0.18, -0.35, 0.37, -0.36, -0.02, -0.32, -0.09, 0.24, -0.32, -0.07, -0.25, 0.00, 0.03, -0.22, -0.20, 0.28, 0.18, -0.28, -0.51, -0.19, -0.07, -0.31, -0.41, -0.10, -0.29, -0.25, 0.10, -0.12, -0.29, -0.03, -0.28, -0.11, 0.05, -0.27, -0.33, 0.10, -0.11, 0.63, -0.14, -0.58, -0.11, -0.17, 0.34, -0.15, 0.30, -0.18, -0.17, -0.29, 0.27, -0.33, -0.62, 0.83, -0.27, 0.14, -0.20, -0.16, -0.30, 0.20, 0.16, -0.09, -0.27, 0.25, -0.56, -0.35, -0.13, -0.01, 0.25, 0.18, -0.10, -0.14, -0.07, 0.09, -0.14, 0.01, -0.23, -0.23, -0.41, 0.27, -0.49, -0.09, 0.35, -0.39, 0.01, 0.11, 0.17, 0.15, -0.02, 0.54, 0.26, -0.01, -0.04, -0.22, -0.01, -0.24, 0.12, -0.19, -0.29, -0.50, -0.42, -0.07, -0.30, -0.36, -0.17, 0.09, -0.59, -0.28, -0.21, -0.18, -0.37, -0.05, -0.42, -0.08, -0.27, -0.20, 0.54, -0.12, -0.17, -0.13, 0.09, -0.13, -0.20, -0.14, -0.01, -0.31, -0.25, -0.19, 0.06, 0.12, 0.11, -0.26, 0.03, -0.19, -0.18, 0.03, -0.24, 0.20, -0.20, -0.21, -0.25, 0.19, -0.34, -0.20, 0.26, -0.46, -0.46, 0.08, 0.05, -0.32, -0.11, 0.07, -0.17, -0.49, -0.60, -0.27, -0.26, -0.33, -0.14, -0.25, -0.77, -0.21, -0.32, 0.12, 0.44, -0.14, -0.31, 0.38, -0.45, -0.16, 0.04, 0.15, -0.24, -0.27, -0.34, -0.02, -0.15, -0.01, 0.06, -0.36, -0.25, 0.46, 0.10, -0.35, -0.36, -0.27, -0.45, 0.04, -0.13, -0.23, -0.32, 0.90, -0.25, 0.08, -0.13, -0.58, -0.44, 0.89, -0.23, 0.59, -0.41, 0.33, -0.46, 0.65, 0.88, 0.43, 0.41, -0.54, 1.28, 0.14, -0.17, 0.42, 0.52, -0.35, -0.11, 0.01, -0.06, 0.71, -0.23, 0.23, -0.13, -0.02, 0.05, -0.27, 0.06, 0.02, 0.76, 0.83, -0.06, -0.59, 0.49, 0.54, 0.26, -0.30, -0.23, -0.20, -0.10, -0.36, -0.09, 0.09, -0.45, 0.82, -0.17, -0.26, 0.34, 0.19, -0.21, -0.08, -0.09, -0.25, 0.05, 0.07, -0.06, -0.14, 0.31, 0.25, 0.62, -0.40, -0.33, 0.13, 0.48, -0.08, 0.67, 0.76, -0.06, 1.41, 0.21, -0.10, 0.88, 0.08, 0.54, 0.21, -0.27, -0.06, -0.06, 0.40, 0.18, 0.18, -0.29, -0.41, 0.26, 0.23, -0.15, -0.37, 0.10, 0.45, 0.19, 0.01, 0.09, -0.08, 0.41, -0.50, 0.43, -0.15, -0.97, 0.28, 1.11, 0.56, 0.23, -0.17, 0.48, -0.02, 0.71, 0.48, -0.46, -0.15, -0.03, -0.02, 0.72, 0.73, -0.04, -0.64, -0.44, 0.11, -0.32, -0.06, -0.06, 0.61, -0.06, 0.02, 0.81, 0.06, -0.30, 0.02, 0.85, 0.01, -0.28, 0.99, -0.22, -0.43, 0.49, 0.14, -0.23, -0.23, -0.82, 0.06, 0.00, -0.17, -0.21, 0.23, 0.53, 0.30, 0.27, 0.31, -0.87, -0.53, -0.25, -0.02, 0.12, -0.05, 0.69, -0.26, -0.15, -0.15, -0.12, -0.16, 0.14, 0.34, -0.20, -0.71, 0.39, 0.16, -0.33, 0.07, -0.18, -0.32, 0.34, -0.10, -0.02, -0.14, -0.17, -0.25, -0.18, -0.34, 0.31, -0.35, 0.01, -0.27, 0.02, -0.13, 0.50, 0.50, 0.21, -0.30, 0.03, -0.30, 0.10, 0.48, -0.21, -0.14, -0.45, -0.14, -0.00, 0.34, 1.17, -0.48, 0.15, -0.44, -0.17, -0.26, -0.25, -0.52, -0.02, -0.24, 0.27, -0.01, 0.29, -0.05, -0.69, 0.00, -0.16, 0.44, -0.02, 0.82, 0.31, -0.16, -0.33, 0.39, -0.24, 0.90, -0.02, 0.12, -0.08, 0.13, 0.50, 1.00, 0.45, 0.21, -0.02, -0.45, -0.23, 0.52, -0.35, 0.20, 0.18, 0.08, 0.03, 0.04, -0.14, -0.19, -0.17, 0.70, -0.15, 0.10, 0.50, -0.04, 0.42, -0.47, 1.61, -0.52, 0.49, 0.14, 0.12, -0.26, -0.31, 0.48, -0.41, -0.55, 0.90, -0.18, 0.23, 0.38, -0.16, -0.05, -0.01, -0.05, 0.03, -0.17, -0.31, -0.00, -0.00, 0.09, -0.28, 0.17, -0.22, 0.25, 0.27, 0.41, 0.03, 0.01, 0.01, 0.64, 0.59, 0.42, 0.11, -0.01, -0.03, 0.46, -0.39, 0.07, -0.31, -0.92, -0.62, 0.02, -0.28, 0.23, 0.61, 0.03, -0.07, 0.22, 0.01, -0.43, -0.03, 0.13, 0.29, 0.39, 0.12, 0.39, 0.56, 0.03, -0.04, 0.41, 0.21, 0.20, 0.11, -0.35, 0.50, -0.32, -0.14, -0.21, 0.37, 0.16, -0.06, -0.12, -0.50, 0.71, -0.62, 0.76, -0.36, -0.25, -0.22, 0.77, 0.41, 0.48, -0.39, 0.00, 0.39, -0.18, 0.54, 0.34, -0.20, -0.46, -0.58, -0.16, -0.36, 0.26, -0.45, 1.34, -0.30, 0.77, 0.60, -0.65, 0.21, 0.23, 0.93, -0.44, -0.14, -0.05, 0.00, 0.07, 0.07, -0.30, -0.05, 1.02, 0.12, 0.25, 0.91, -0.09, 0.32, 0.84, 0.88, 0.38, -0.18, 0.51, -0.05, -0.19, -0.38, -0.64, 0.68, -0.36, 0.12, 0.30, 0.03, 0.48, -0.54, 0.93, 0.36, 0.62, 0.43, 0.00, 0.41, 0.17, 0.56, -0.39, 0.04, 0.23, -0.10, 0.12, 0.81, 0.19, 0.34, 0.62, 0.32, 0.07, 0.46, -0.22, 0.23, 0.24, -0.30, -0.05, -0.28, 0.02, 0.68, 0.29, 0.51, -0.45, -0.04, -0.04, 0.47, -0.35, -0.16, 0.18, 0.48, 0.36, -0.18, 0.06, 0.52, 0.37, -0.05, 0.10, 0.38, -0.35, 0.30, 0.00, -0.42, 0.93, -0.79, -0.51, 0.80, -0.42, 0.26, 0.26, 0.77, -0.06, 0.40, -0.22, -0.39, -0.10, -0.03, -0.60, -0.13, -0.04, 0.14, -0.01, -0.57, 0.02, -0.16, -0.23, -0.01, -0.27, -0.12, 0.18, 0.14, -0.33, 0.41, -0.38, -0.27, -0.55, 0.01, -0.41, 1.00, 0.45, -0.51, -0.77, 0.18, 0.10, 0.30, -0.28, -0.37, -0.07, 0.04, 0.54, -0.18, 0.13, -0.50, -0.15, -0.28, 1.03, -0.26, -0.46, 0.35, 0.09, 0.06, 0.13, 0.80, -0.39, 0.88, 0.16, -0.26, 0.17, -0.14, 0.26, -0.83, -0.29, -0.07, -0.44, -0.03, 0.38, 0.21, -0.22, 0.01, -0.07, 0.29, 0.47, 0.08, -0.10, 0.36, 0.33, 0.15, 0.14, 0.86, -0.01, 0.11, -0.29, -0.13, 0.04, 0.58, 0.56, 0.38, 0.14, -0.03, 0.40, 0.58, -0.19, 0.04, -0.27, -0.28, -0.26, -0.52, -0.06, -0.13, -1.35, -0.61, 0.12, -0.47, 0.03, -0.21, -0.08, 0.62, -0.20, 0.05, 0.26, 0.27, -0.14, -0.24, 0.20, -0.03, -0.03, 0.36, 0.96, 0.08, 1.87, -0.37, 0.44, 0.65, 0.01, -0.05, 0.53, -0.30, -0.41, -0.06, 1.40, 0.29, -0.02, -0.12, 0.61, 0.12, -0.12, -0.26, -0.33, -0.16, -0.12, 0.13, -0.32, -0.01, -0.32, 0.39, 0.02, 0.49, 0.38, 0.86, -0.45, -0.29, -0.38, 0.66, -0.22, 0.23, 0.05, 0.17, 0.15, -0.47, 0.23, -0.00, 0.66, 0.13, -0.46, -0.10, -0.15, -0.10, 0.18, -0.04, -0.17, 0.61, -0.44, -0.15, 0.02, 0.14, 0.59, -0.40, -0.30, 0.64, 0.29, 0.75, 0.38, -0.16, -0.32, -0.44, -0.16, 0.48, -0.08, -0.47, -0.23, 0.37, -0.18, 0.11, 0.87, -0.34, -0.04, -0.13, -0.04, -0.01, 0.18, -0.31, 0.90, 0.30, 0.73, -0.30, 0.17, 1.10, -0.20, -0.60, 0.12, -0.26, 0.98, -0.29, 0.16, -0.01, 0.15, 0.07, -0.28, 0.32, 0.04, -0.32, -0.28, 0.36, 0.29, 0.61, -0.24, -0.73, 0.31, -0.31, 0.37, -0.05, -0.17, -0.53, 0.13, -0.28, -0.49, 0.41, -0.12, 1.10, -0.28, 0.50, -0.01, -0.12, 0.65, -0.40, 0.59, -0.02, 0.26, -0.22, -0.31, -0.07, 0.88, 0.20, 0.61, 0.05, 0.15, 0.06, 1.66, -0.57, -0.19, 0.01, 0.16, 0.22, -0.98, -0.24, -0.74, 0.45, -0.07, -0.20, -0.02, -0.52, -0.43, -0.22, -0.30, -0.13, -0.18, 0.32, -0.18, -0.08, -0.49, 0.69, -0.28, -0.04, 0.22, 0.45, 0.04, 0.44, 0.30, -0.05, -0.24, 0.01, 0.43, 0.05, 0.29, 0.17, -0.39, 0.62, 0.48, 0.01, 0.06, 0.13, -0.07, 0.75, -0.38, -0.06, 0.38, 0.05, 0.05, -0.38, -0.33, 0.23, 0.57, 0.60, -0.26, -0.10, 0.28, 0.64, 2.24, 1.01, 0.61, 0.90, -0.50, -0.01, 1.27, 0.87, -0.57, 0.07, -0.29, -0.18, 0.39, 0.71, 0.50, -0.54, -0.86, 0.29, -0.15, 0.56, -0.13, -0.27, -0.14, 0.76, -0.40, 0.52, -0.52, 0.37, 1.62, 1.46, -0.41, -0.08, -0.18, -0.64, -1.28, -0.05, -0.51, 0.02, -0.17, 0.27, 0.86, 0.08, 0.31, 0.60, -0.23, 0.80, 0.20, 0.41, 0.49, -0.02, 0.23, -0.11, 0.25, 0.03, 0.17, 1.14, 1.90, -1.00, 0.47, -0.23, 2.46, 0.48, -0.04, 2.71, 1.71, 1.98, 0.86, 0.65, 0.30, 0.01, 0.48, 0.41, 1.49, -0.89, 1.71, -0.58, 0.87, -0.45, -0.31, 0.26, 0.25, -0.13, 0.77, -0.04, 0.74, 0.39, -0.37, 0.06, 1.74, 0.74, 0.16, 0.07, 0.72, 1.11, -0.35, 1.24, -0.15, 0.71, -0.56, -0.29, -0.59, 1.77, 0.61, -0.05, -0.30, -1.54, -0.47, -0.65, 0.72, 0.41, 0.14, 0.99, -0.02, 6.38, -0.31, 6.90, 0.30, 0.32, 0.01, 1.06, 0.29, -0.18, 0.16, 0.01, 0.41, -0.11, 0.07, 0.41, -1.48, -0.67, 0.41, -0.41, -0.25, -0.30, -0.38, -0.53, -0.74, 0.57, -0.53, 0.38, -0.07, -0.02, -0.34, -0.21, 0.60, -0.41, 0.22, -0.56, -0.35, 0.46, -0.48, -0.64, 0.19, -0.06, -0.41, -0.90, 1.02, -0.48, 0.02, 0.86, -0.35, 1.42, 1.23, -0.22, -0.71, -0.26, 0.52, -0.05, -0.22, -0.11, -0.02, -0.54, -0.28, 1.52, -0.47, -0.47, 0.06, 0.62, -0.12, -0.46, -0.97, -0.71, 0.03, 1.99, 1.42, -0.04, -0.33, 0.96, 0.13, 1.44, 1.53, -0.21, -0.73, 0.82, -0.51, -0.04, -0.59, -0.53, 0.09, 0.02, 1.11, -0.24, 0.38, 0.00, 0.72, -0.50, 0.09, -0.48, -0.57, 0.71, 0.29, -0.50, -0.31, -0.42, -0.78, -0.03, 0.66, -0.55, -0.66, 0.02, -0.53, 0.54, -0.26, 0.55, -0.36, 0.77, 0.16, 0.15, 0.40, 0.81, 0.24, -0.77, -0.24, -0.59, 1.99, -0.07, -0.82, -0.08, 0.01, -0.18, 0.23, 0.19, 0.82, -0.07, -0.16, -0.23, 0.38, -0.40, -0.68, 0.21, 0.03, -0.59, 0.19, -0.53, -0.48, 1.40, -0.46, -0.50, -0.02, -0.43, 0.84, -0.04, -0.20, -0.30, 0.41, 1.30, 0.37, 0.04, -0.03, -0.12, 1.23, -0.82, 0.28, -0.38, -0.38, 1.55, -0.34, 0.12, 0.13, -0.45, -0.21, 0.46, -0.11, 0.47, 0.75, -0.07, 0.16, -0.55, -0.24, -0.01, -0.09, -0.64, -0.51, -0.61, -0.49, 0.64, -0.47, -0.35, -0.46, -0.34, 1.14, 0.41, -0.13, -0.75, 1.20, 0.14, 0.53, 0.22, 2.28, -0.21, 0.14, -0.20, -0.19, 1.41, -0.22, -0.52, 1.67, -0.64, -0.42, 0.45, -0.28, -0.02, -0.57, -0.26, -0.23, 0.81, 0.49, 1.76, 0.02, 1.54, -0.63, 0.19, 0.21, -0.39, -0.02, 0.42, -0.38, -0.26, 0.14, -0.68, 1.89, 0.41, -0.41, 0.71, 0.91, -0.02, -0.13, -0.24, 0.20, 0.09, -0.61, 1.41, -0.15, -0.06, 1.03, -0.61, -0.80, -0.51, -0.39, -0.55, 2.10, 0.89, 0.35, 1.08, -0.06, -0.45, -0.27, 0.84, -0.41, 1.14, 0.26, -0.07, 0.61, -0.39, 1.27, 0.17, -0.03, 0.84, 0.94, 0.13, -0.95, -0.02, -0.10, -0.15, 0.07, -0.33, 0.31, 0.24, -0.30, 0.58, -0.17, -0.68, -0.47, -0.51, 0.45, 0.31, -0.78, 1.82, -0.61, 0.06, 1.18, -0.53, -0.50, -0.30, -0.01, 0.91, 0.07, -0.31, -0.34, -0.47, -0.49, 0.09, 0.10, 1.01, 0.17, -0.39, 0.01, -0.37, 0.08, -0.27, 0.36, 0.53, -0.51, -0.40, 1.27, 1.05, 0.17, -0.32, -0.33, -0.63, 0.75, 0.09, -0.49, -0.50, 0.34, 3.67, 0.25, -0.23, 2.59, 0.72, -0.28, 0.16, -0.28, 0.23, 0.80, -0.31, 0.01, 0.49, -1.88, -0.33, -0.45, 1.85, 0.84, -0.45, -0.34, 0.47, -0.43, -0.25, -0.51, 1.62, -0.44, -0.81, 0.07, 0.55, -0.42, -0.76, 1.72, 1.55, 0.99, 0.44, -0.39, 0.23, -0.22, 0.49, -0.25, -0.52, 1.58, -0.46, -0.56, 1.68, -0.44, 0.80, 0.03, -0.53, -0.03, 0.79, 0.59, 1.44, -0.41, -0.52, 0.64, 0.92, 0.58, 0.26, -0.13, 0.51, -0.20, 0.62, -0.46, 0.30, 2.59, 0.68, 0.53, 0.58, 1.93, 0.27, 0.79, 0.42, -0.35, -0.01, 0.02, 1.28, 0.78, 1.32, -0.27, -0.66, 0.31, -0.06, -0.58, -0.52, -0.74, -0.28, 1.13, -0.35, 0.07, -0.43, 0.05, -0.02, 0.58, 0.43, 0.73, -0.20, 0.42, -0.15, -0.45, -0.40, 0.67, -0.44, 0.29, -0.37, -0.34, -0.31, -0.50, 1.60, 0.55, -0.28, 1.40, -0.14, 0.91, -0.19, -0.29, 0.13, -0.46, 0.16, -0.36, -0.78, -0.06, 0.57, 0.20, 1.06, -0.00, -0.22, 0.08, 0.13, 1.00, 0.65, 0.88, -0.12, 1.03, -0.35, 0.73, -0.10, -0.09, -0.02, 1.73, 0.01, 0.07, 0.14, 0.24, -0.21, -0.20, -0.17, 0.16, -0.45, -0.17, 1.02, 0.18, -0.59, -0.73, 0.08, -0.11, 0.11, 1.47, -0.74, -0.45, 1.03, 0.48, 0.15, -0.23, -0.44, 1.69, -0.20, -0.44, 1.57, 0.59, -0.20, 0.89, 0.76, 0.34, 5.11, -0.41, -0.45, 0.18, 0.69, 8.74, -0.30, -0.37, 0.36, -0.06, 0.05, 0.24, -0.52, 0.46, -0.43, -0.47, 1.55, 0.82, -0.43, 1.49, 1.67, 0.61, -0.14, 10.55, 0.73, 0.76, -0.99, -0.34, -0.56, 0.42, -0.48, -0.47, 0.21, -0.17, -0.34, 0.59, -0.37, -0.50, -0.45, 0.77, 0.11, -0.12, 0.49, 0.23, 1.58, 0.30, -0.00, 0.76, -0.36, 0.37, -0.26, 0.70, 0.69, 0.10, -0.53, 0.10, 0.25, 0.22, -0.04, 1.72, 0.95, -0.51, -0.47, -0.49, 0.01, 0.01, 0.34, 1.87, -0.65, 1.96, -0.19, 0.11, -0.40, 0.73, 0.68, -0.87, 0.41, -0.74, -0.26, 1.40, 0.40, -0.27, 1.73, -0.43, -0.39, 0.44, -0.08, -0.26, 0.13, 1.87, -0.43, -0.43, -0.44, 0.68, -0.03, 0.01, 0.63, 0.08, -1.63, 2.03, 0.11, -0.53, -0.13, 0.36, 0.12, 0.54, -0.26, 0.12, 0.62, -0.58, 0.27, -0.14, -0.32, -0.50, 0.00, -0.09, 1.57, -0.16, -0.23, 1.57, 1.73, -0.52, -0.07, -0.63, 0.47, -0.14, -0.58, 0.00, 0.16, 1.43, -0.24, -0.56, 0.20, -0.47, 1.59, -0.56, -0.54, -0.32, 0.17, -0.16, -0.45, 0.37, -0.50, 0.03, -0.09, -0.47, -0.01, -0.40, 0.40, -0.45, 0.69, 0.13, -1.14, -0.27, 0.66, 0.85, 0.59, 0.04, 0.05, -0.23, -0.25, -0.60, 0.23, -0.14, -0.10, -0.10, -0.08, -0.19, -0.41, 0.30, -0.15, 0.33, -0.05, -0.18, 0.01, -0.14, -0.14, 0.26, -0.17, 0.03, 0.04, -0.34, 0.13, -0.13, -0.18, 0.19, 0.46, -0.08, -0.21, -0.17, 0.71, 0.18, -0.20, -0.05, 0.05, -0.10, 0.59, -0.05, -0.07, -0.10, -0.07, -0.47, 0.06, -0.09, -0.20, 0.12, -0.14, -0.05, -0.28, -0.11, 0.09, -0.11, -0.30, 0.80, -0.14, 0.17, -0.21, -0.16, -0.07, 0.03, 0.05, -0.11, -0.07, -0.12, 0.20, -0.20, 0.07, 0.70, 0.25, -0.07, -0.15, 0.09, -0.70, -0.04, 0.10, 0.01, -0.04, -0.27, -0.17, 0.54, -0.14, 0.62, -0.17, -0.16, 0.10, 0.14, -0.12, -0.13, 0.48, -0.32, -0.37, -0.03, -0.04, -0.08, 0.28, 0.59, 0.07, -0.20, -0.00, -0.14, -0.26, 0.03, 0.12, 0.07, 0.05, -0.07, -0.05, -0.08, -0.12, -0.27, -0.14, 0.29, 0.11, -0.12, 0.66, -0.22, 0.08, 0.01, 0.20, -0.10, -0.06, -0.38, 0.43, -0.01, -0.18, -0.25, 0.07, -0.22, -0.08, -0.29, 0.18, -0.03, -0.13, -0.26, 0.38, 0.24, 0.36, -0.20, 0.78, 0.22, -0.15, 0.43, 0.24, -0.17, 0.41, 0.21, -0.13, -0.10, 0.20, -0.14, -0.06, 0.11, -0.40, -0.18, 0.00, -0.09, -0.26, -0.01, 0.05, -0.26, -0.20, -0.02, -0.11, -0.06, -0.23, -0.11, -0.30, -0.04, -0.02, -0.14, 0.38, -0.07, -0.17, -0.19, 0.12, 0.22, -0.17, -0.08, 0.63, 0.18, -0.12, -0.14, -0.20, -0.16, 0.39, -0.18, 0.16, -0.20";
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

