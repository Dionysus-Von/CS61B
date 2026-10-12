public class MergeSort {


    /**
     * @param arr
     *
     * Sort the array arr using merge sort.
     * The merge sort algorithm is as follows:
     * 1. Split the collection to be sorted in half.
     * 2. Recursively call merge sort on each half.
     * 3. Merge the sorted half-lists.
     *
     */
    public static int[] sort(int[] arr) {
        // TODO: Implement merge sort
        if(arr.length<=1){
            return arr;
        }
        int mid = arr.length/2;
        int[] left = java.util.Arrays.copyOfRange(arr, 0, mid);
        int[] right = java.util.Arrays.copyOfRange(arr, mid, arr.length);

        left = sort(left);
        right = sort(right);

        return merge(left, right);
    }

    /**
     * @param a
     * @param b
     *
     * Merge the sorted half-lists.
     *
     * Suggested helper method that will make it easier for you to implement merge sort.
     */
    private static int[] merge(int[] a, int[] b) {
        int[] c = new int[a.length + b.length];
        // TODO: Implement merge
        int i = 0;
        int j = 0;
        int k = 0;


        while (i < a.length && j < b.length) {
            if (a[i] <= b[j]) {
                c[k] = a[i];
                i++;
            } else {
                c[k] = b[j];
                j++;
            }
            k++;
        }


        while (i < a.length) {
            c[k] = a[i];
            i++;
            k++;
        }


        while (j < b.length) {
            c[k] = b[j];
            j++;
            k++;
        }

        return c;
    }
}

