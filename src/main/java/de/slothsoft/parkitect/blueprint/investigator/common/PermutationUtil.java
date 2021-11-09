package de.slothsoft.parkitect.blueprint.investigator.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Help for getting permutations of an int array.
 */

public final class PermutationUtil {

	public static void main(String[] args) {
		System.out.println(permute(new int[]{1, 2, 3, 4}).stream().map(array -> Arrays.toString(array))
				.collect(Collectors.joining("\n")));
	}

	public static List<int[]> permute(int[] intArray) {
		return permute(intArray, 0);
	}

	private static List<int[]> permute(int[] intArray, int start) {
		final List<int[]> result = new ArrayList<>();
		for (int i = start; i < intArray.length; i++) {
			final int temp = intArray[start];
			intArray[start] = intArray[i];
			intArray[i] = temp;
			result.addAll(permute(intArray, start + 1));
			intArray[i] = intArray[start];
			intArray[start] = temp;
		}
		if (start == intArray.length - 1) {
			result.add(Arrays.copyOf(intArray, intArray.length));
		}
		return result;
	}

	private PermutationUtil() {
		// hide me
	}

}
