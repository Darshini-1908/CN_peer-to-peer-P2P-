import java.util.Comparator;

/**
 * This class is used to compare download rates of peers so as to determine preferred neighbors after the unchoking interval.
 */
public class DownloadRateSorter implements Comparator<RemotePeerDetails> {

	private boolean firstInstanceGreater;

	
	//Empty constructor to create a DownloadRateSorter object with the default setting for comparison.
	
	public DownloadRateSorter() {
		this.firstInstanceGreater = true;
	}

	
	//Parameterized constructor to create a DownloadRateSorter object with a specific comparison setting.
	
	public DownloadRateSorter(boolean constructor) {
		this.firstInstanceGreater = constructor;
	}

	/**
	 * Used to compare download rates of two peers based on their download rates.
	 * @param da1 
	 * @param da2 
	 * @return 1 - da1 > da2; -1 - da1 < da2; 0 - da1 = da2
	 */
	public int compare(RemotePeerDetails da1, RemotePeerDetails da2) {
		if (da1 == null && da2 == null)
			return 0;

		if (da1 == null)
			return 1;

		if (da2 == null)
			return -1;

        // Checking if the peer implements Comparable interface for direct comparison
		if (da1 instanceof Comparable) {
			if (firstInstanceGreater) {
				return da1.compareTo(da2);
			} else {
				return da2.compareTo(da1);
			}
		}
		// If not Comparable, compare their string representations
		else {
			if (firstInstanceGreater) {
				return da1.toString().compareTo(da2.toString());
			} else {
				return da2.toString().compareTo(da1.toString());
			}
		}
	}
}
