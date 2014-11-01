
package koth.util;

import koth.game.*;

import java.util.*;

/**
 * This static class contains algorithms and helpers.
 * @see java.util.Collections
 * @see java.util.Arrays
 */
public final class Algorithms {

    private Algorithms() {}

    /**
     * Dummy comparator for comparable objects.
     */
    public static class NaturalComparator<T extends Comparable<? super T>> implements Comparator<T> {

        private static final NaturalComparator<?> instance = new NaturalComparator();

        /**
         * Get singleton instance.
         */
        public static <T extends Comparable<? super T>> NaturalComparator<T> getInstance() {
            @SuppressWarnings("unchecked")
            NaturalComparator<T> i = (NaturalComparator<T>)instance;
            return i;
        }

        protected NaturalComparator() {}

        @Override
        public int compare(T a, T b) {
            return a.compareTo(b);
        }

    }

    /**
     * Reverse the effect of specified comparator.
     */
    public static class ReversedComparator<T> implements Comparator<T> {

        @SuppressWarnings("unchecked")
        private static final ReversedComparator<?> instance = new ReversedComparator(NaturalComparator.getInstance());

        /**
         * Get singleton instance for natural reversed comparator.
         */
        public static <T extends Comparable<? super T>> ReversedComparator<T> getInstance() {
            @SuppressWarnings("unchecked")
            ReversedComparator<T> i = (ReversedComparator<T>)instance;
            return i;
        }

        private final Comparator<? super T> underlying;

        /**
         * Create a reversed version of given comparator.
         */
        public ReversedComparator(Comparator<? super T> underlying) {
            this.underlying = underlying;
        }

        @Override
        public int compare(T a, T b) {
            return underlying.compare(b, a);
        }

    }

    /**
     * Get index of minimum (in case of equalities, the first one is returned).
     */
    public static <T extends Comparable<? super T>> int min(List<T> list) {
        return min(list, NaturalComparator.<T>getInstance());
    }

    /**
     * Get index of minimum (in case of equalities, the first one is returned).
     */
    public static <T> int min(List<T> list, Comparator<? super T> cmp) {
        if (list == null || list.isEmpty())
            return -1;
        if (cmp == null)
            return 0;
        Iterator<T> it = list.iterator();
        T b = it.next();
        int r = 0;
        for (int i = 1; it.hasNext(); ++i) {
            T t = it.next();
            if (cmp.compare(t, b) < 0) {
                b = t;
                r = i;
            }
        }
        return r;
    }


    /**
     * Get index of maximum (in case of equalities, the first one is returned).
     */
    public static <T extends Comparable<? super T>> int max(List<T> list) {
        return max(list, NaturalComparator.<T>getInstance());
    }

    /**
     * Get index of maximum (in case of equalities, the first one is returned).
     */
    public static <T> int max(List<T> list, Comparator<? super T> cmp) {
        return min(list, new ReversedComparator<T>(cmp));
    }

    /**
     * Get indices of sorted list (i.e. indices of elements in original list).
     * <code>list</code> is not modified.
     */
    public static <T extends Comparable<? super T>> List<Integer> sortedIndices(List<T> list) {
        return sortedIndices(list, NaturalComparator.<T>getInstance());
    }

    /**
     * Get indices of sorted list (i.e. indices of elements in original list).
     * <code>list</code> is not modified.
     */
    public static <T> List<Integer> sortedIndices(final List<T> list, final Comparator<? super T> cmp) {
        List<Integer> indices = range(list.size());
        Collections.sort(indices, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return cmp.compare(list.get(a), list.get(b));
            }
        });
        return indices;
    }

    /**
     * Sort all lists according to the first one.
     */
    public static <T extends Comparable<? super T>> List<Integer> sort(List<? extends T> head, List<?>... tails) {
        return sort(NaturalComparator.<T>getInstance(), head, tails);
    }

    /**
     * Sort all lists according to the first one.
     */
    public static <T> List<Integer> sort(Comparator<? super T> cmp, List<? extends T> head, List<?>... tails) {
        List<Integer> indices = sortedIndices(head, cmp);
        reorder(head, indices);
        if (tails != null)
            for (List<?> l : tails)
                if (l != null)
                    reorder(l, indices);
        return indices;
    }

    /**
     * Create a new list with <code>list</code> elements as specified in <code>indices</code>.
     */
    public static <T> List<T> reordered(List<T> list, List<Integer> indices) {
        List<T> tmp = new ArrayList<T>(indices.size());
        for (int i : indices)
            tmp.add(list.get(i));
        return tmp;
    }

    /**
     * Replace list content with <code>list</code> elements as specified in <code>indices</code>.
     */
    public static <T> void reorder(List<T> list, List<Integer> indices) {
        List<T> tmp = reordered(list, indices);
        list.clear();
        list.addAll(tmp);
    }

    /**
     * Create a list of zeros.
     */
    public static List<Integer> zeros(int size) {
        List<Integer> list = new ArrayList<Integer>(size);
        for (int i = 0; i < size; ++i)
            list.add(0);
        return list;
    }

    /**
     * Create a sequence of number from <code>min</code> (inclusive) to <code>max</code> (exclusive).
     */
    public static List<Integer> range(int min, int max) {
        List<Integer> list = new ArrayList<Integer>(Math.max(max - min, 0));
        for (int i = min; i < max; ++i)
            list.add(i);
        return list;
    }

    /**
     * Create a sequence of number from <code>0</code> (inclusive) to <code>max</code> (exclusive).
     */
    public static List<Integer> range(int max) {
        return range(0, max);
    }

    /**
     * Swap two elements in two lists.
     */
    public static <T> void swap(List<T> a, int ai, List<T> b, int bi) {
        T t = a.get(ai);
        a.set(ai, b.get(bi));
        b.set(bi, t);
    }

    /**
     * Swap two elements in a list.
     */
    public static <T> void swap(List<T> a, int ai, int bi) {
        swap(a, ai, a, bi);
    }

    // TODO sublist, repeated, (copy), reverse, distinct, replace, count...

    // TODO permutations, combinations, integer seq (increment)

    // TODO pathing, distances

    /**
     * Compute pairwise shortest distance between all tiles.
     */
    public static Map<Vector, Map<Vector, Integer>> distances(Set<Vector> tiles) {
        // Initialize distances
        Map<Vector, Map<Vector, Integer>> distances = new HashMap<Vector, Map<Vector, Integer>>();
        for (Vector a : tiles) {
            Map<Vector, Integer> m = new HashMap<Vector, Integer>();
            for (Vector b : tiles)
                m.put(b, Integer.MAX_VALUE - 1);
            m.put(a, 0);
            distances.put(a, m);
        }
        // Iterate to compute smallest distance between each location
        for (boolean updated = true; updated;) {
            updated = false;
            for (Vector a : tiles) {
                for (Move m : Move.getNonzeros()) {
                    Vector t = a.add(m);
                    if (tiles.contains(t)) {
                        // For each tile t adjacent to a, check if distance is better than old guess
                        for (Vector b : tiles) {
                            Map<Vector, Integer> dists = distances.get(b);
                            int prev = dists.get(t);
                            int next = dists.get(a) + 1;
                            if (next < prev) {
                                dists.put(t, next);
                                updated = true;
                            }
                        }
                    }
                }
            }
        }
        return distances;
    }

}
