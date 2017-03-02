package pl.allegro.tech.build.axion.release.util;

import static org.junit.Assert.*
import java.util.List

import org.junit.After
import org.junit.Before
import org.junit.Test

class VersionComparatorTest {
	
	static private final String[] VERSION_LIST_ORDERED = [
		"0.0.1",
		"0.0.2",
		"0.2.0-alpha",
		"0.2.0-SNAPSHOT",
		"0.2.0",
		"0.2.1-alpha",
		"0.2.1-beta",
		"0.2.1-SNAPSHOT",
		"0.2.1-RC1",
		"0.2.1-RC2",
		"0.2.1",
		"0.2.2-alpha",
		"0.2.3-alpha",
		"1.0.0-alpha",
		"1.0.0"
	] as String[];
	
	static private String[] versionListRandom;

	@Before
	public void setUp() throws Exception {
		versionListRandom = [
			"0.2.1-RC1",
			"0.2.1-RC2",
			"0.2.1",
			"0.2.2-alpha",
			"0.2.3-alpha",
			"1.0.0-alpha",
			"1.0.0",
			"0.0.1",
			"0.0.2",
			"0.2.1-beta",
			"0.2.0-alpha",
			"0.2.0-SNAPSHOT",
			"0.2.0",
			"0.2.1-alpha",
			"0.2.1-SNAPSHOT"
		] as String[];
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testCompare() {
		Arrays.sort(versionListRandom, new VersionComparator());
		println "Result: $versionListRandom"
		for (int idx = 0; idx < VERSION_LIST_ORDERED.size(); idx++) {
			assertEquals(VERSION_LIST_ORDERED[idx], versionListRandom[idx]);
		}
	}

	@Test
	public void testIsSnapshot() {
		boolean isTested = false;
		for (String currentVersion : VERSION_LIST_ORDERED) {
			if (currentVersion.contains("-")) {
				assertTrue(VersionComparator.isSnapshot(currentVersion));
				isTested = true;
			}
		}
		assertTrue(isTested);
	}

	@Test
	public void testRemoveSnapshot() {
		boolean isTested = false;
		for (String currentVersion : VERSION_LIST_ORDERED) {
			if (currentVersion.contains("-")) {
				String result = VersionComparator.removeSnapshot(currentVersion);
				assertFalse("Found a snapshot in " + result, result.contains("-"));
				isTested = true;
			}
		}
		assertTrue(isTested);
	}

}
