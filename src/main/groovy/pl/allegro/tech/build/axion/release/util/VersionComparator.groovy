package pl.allegro.tech.build.axion.release.util

import java.util.Comparator

class VersionComparator implements Comparator<String> {
	
	static private final List<String> SNAPSHOT_DELIMITERS = [ "-" ].asImmutable();
	static private final ArrayList<String> DEFAULT_SNAPSHOT_PRIORITY_LIST = [ "alpha", "beta", "SNAPSHOT", "RC" ].asImmutable();
	
	private ArrayList<String> snapshotPriorityList;
	
	public VersionComparator() {
		snapshotPriorityList = DEFAULT_SNAPSHOT_PRIORITY_LIST;
	}
	
	public VersionComparator(ArrayList<String> snapshotPriorityList) {
		snapshotPriorityList = this.snapshotPriorityList;
	}

	@Override
	public int compare(String o1, String o2) {
        int result = 0
        if (o1 == '*') {
            result = 1
        }
        else if (o2 == '*') {
            result = -1
        }
        else {
            def nums1
            try {
                def tokens = removeSnapshot(o1).split(/\./)
								def snapResult =  removeSnapshot(o1)
                tokens = tokens.findAll { String it -> it.trim() ==~ /\d+/ }
                nums1 = tokens*.toInteger()
            }
            catch (NumberFormatException e) {
                throw new Exception("Cannot compare versions, left side [$o1] is invalid: ${e.message}")
            }
            def nums2
            try {
                def tokens = removeSnapshot(o2).split(/\./)
								tokens = removeSnapshot(o2).split(/\./)
                tokens = tokens.findAll { String it -> it.trim() ==~ /\d+/ }
                nums2 = tokens*.toInteger()
            }
            catch (NumberFormatException e) {
                throw new Exception("Cannot compare versions, right side [$o2] is invalid: ${e.message}")
            }
            boolean bigRight = nums2.size() > nums1.size()
            boolean bigLeft = nums1.size() > nums2.size()
            for (int i in 0..<nums1.size()) {
                if (nums2.size() > i) {
                    result = nums1[i].compareTo(nums2[i])
                    if (result != 0) {
                        break
                    }
                    if (i == (nums1.size()-1) && bigRight) {
                        if (nums2[i+1] != 0)
                            result = -1; break
                    }
                }
                else if (bigLeft) {
                    if (nums1[i] != 0)
                        result = 1; break
                }
            }
        }

        if (result == 0) {
            // Versions are equal, but one may be a snapshot.
            // A snapshot version is considered less than a non snapshot version
            def o1IsSnapshot = isSnapshot(o1)
            def o2IsSnapshot = isSnapshot(o2)

            if (o1IsSnapshot && !o2IsSnapshot) {
                result = -1
            } else if (!o1IsSnapshot && o2IsSnapshot) {
                result = 1
            } else if (o1IsSnapshot && o2IsSnapshot) {
							// Both are snapshots, so compare to other...
							String suffix1 = getSnapshotSuffix(o1);
							String suffix2 = getSnapshotSuffix(o2);
							if (o1.equals(o2)) {
								return 0;
							}
							
							def suffixPos1, suffixPos2;
							for (int i = 0; i < snapshotPriorityList.size(); i++) {
								if (suffix1.contains(snapshotPriorityList.getAt(i)) && !suffixPos1) {
									suffixPos1 = i;
								}
								if (suffix2.contains(snapshotPriorityList.getAt(i)) && !suffixPos2) {
									suffixPos2 = i;
								}
							}
							
							if (suffixPos1 && suffixPos2) {
								if (suffixPos1.equals(suffixPos2)) {
									result = o1.compareToIgnoreCase(o2);
								} else {
									result = suffixPos1.compareTo(suffixPos2);
								}
							} else {
								result = o1.compareToIgnoreCase(o2);
							}
						}
        }

        result
    }
		
		protected static boolean isSnapshot(String version) {
				SNAPSHOT_DELIMITERS.any { String it -> version?.contains(it) }
		}
		
		/**
		 * Removes any suffixes that indicate that the version is a kind of snapshot
		 */
		protected static String removeSnapshot(String version) {
				String delimiter = SNAPSHOT_DELIMITERS.find { String it -> version?.contains(it) }
				if (delimiter) {
						return version.substring(0, version.indexOf(delimiter))
				} else {
						return version
				}
		}
		
		/**
		 * Gets the snapshot suffix (only) or an empty string if one doesn't exist.
		 */
		protected static String getSnapshotSuffix(String version) {
				String delimiter = SNAPSHOT_DELIMITERS.find { String it -> version?.contains(it) }
				if (delimiter) {
						return version.substring(version.indexOf(delimiter))
				} else {
						return "";
				}
		}

}
