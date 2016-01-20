package htsjdk.samtools.util;

/**
 * Any class that has a single logical mapping onto the genome should implement Locatable
 * positions should be reported as 1-based and closed at both ends
 *
 */
public interface Locatable {

    /**
     * Gets the contig name for the contig this is mapped to.  May return null if there is no unique mapping.
     * @return name of the contig this is mapped to, potentially null
     */
    String getContig();

    /**
     * @return 1-based start position, undefined if getContig() == null
     */
    int getStart();

    /**
     * @return 1-based closed-ended position, undefined if getContig() == null
     */
    int getEnd();


    /**
   * getSpanningInterval returns interval that covers all of the locations passed in.
   * @param locations the locations to be spanned (on a single contig)
   * @return the minimal span that covers all locations (could be null if no locations are passed in).
   */
   static Locatable getSpanningInterval(Iterable<? extends Locatable> locations) {
       int min = Integer.MAX_VALUE;
       int max = 1;
       String contig = null;
       for (Locatable l : locations) {
           if (contig == null) {
               contig = l.getContig();
           } else if (!l.getContig().equals(contig)) {
               throw new IllegalArgumentException("found different contigs from inputs: " + contig + ","
                    + l.getContig());
           }

           if (l.getStart() < min) {
               min = l.getStart();
           }
           if (l.getEnd() > max) {
               max = l.getEnd();
           }
       }
       if (contig == null) {
           return null;
       }

       final String finalContig = contig;
       final int finalMin = min;
       final int finalMax = max;

       return new Locatable(){
           @Override
           public String getContig() {
               return finalContig;
           }

           @Override
           public int getStart() {
               return finalMin;
           }

           @Override
           public int getEnd() {
               return finalMax;
           }
       };
   }


    /**
    * @return the 0-based start position (from the GA4GH spec).
    */
    default long getGA4GHStart() {return this.getStart() - 1; }

    /**
    * @return the typical end spans are [zero-start,end) (from the GA4GH spec).
    */
    default long getGA4GHEnd() { return this.getEnd(); }

    /**
     * @return number of bases covered by this interval (will always be > 0)
     */
    default int size() {
        return getEnd() - getStart() + 1;
    }

    /**
     * Determines whether this interval overlaps the provided locatable.
     *
     * @param other interval to check
     * @return true if this interval overlaps other, otherwise false
     */
    default boolean overlaps(Locatable other) {
        return overlapsWithMargin(other, 0);
    }

    /**
      * Determines whether this interval comes within "margin" of overlapping the provided locatable.
      * This is the same as plain overlaps if margin=0.
      *
      * @param other interval to check
      * @param margin how many bases may be between the two interval for us to still consider them overlapping.
      * @return true if this interval overlaps other, otherwise false
      */
     default boolean overlapsWithMargin(Locatable other, int margin) {
         if ( other == null || other.getContig() == null ) {
             return false;
         }

         return this.getContig().equals(other.getContig()) && this.getStart() <= other.getEnd() + margin && other.getStart() - margin <= this.getEnd();
     }

    /**
    * Determines whether this interval contains the entire region represented by other
    * (in other words, whether it covers it).
    *
    * @param other interval to check
    * @return true if this interval contains all of the bases spanned by other, otherwise false
    */
   default boolean contains(Locatable other) {
       if ( other == null || other.getContig() == null ) {
           return false;
       }

       return this.getContig().equals(other.getContig()) && this.getStart() <= other.getStart() && this.getEnd() >= other.getEnd();
   }
}
