/*
 * Copyright (c) 2021. Bernard Bou.
 */

package org.oewntk.model;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toMap;

public class MapFactory
{
	private static final boolean LOG_DUPLICATE_VALUES = true;

	// G E N E R I C   M A P   F A C T O R Y

	private static class KeepMergingSupplier<V> implements Supplier<BinaryOperator<V>>
	{
		@Override
		public BinaryOperator<V> get()
		{
			return (existing, replacement) -> {
				if (existing.equals(replacement))
				{
					if (LOG_DUPLICATE_VALUES)
					{
						Tracing.psInfo.printf("[W] Duplicate values %s and %s, keeping first%n", existing, replacement);
					}
					//throw new IllegalArgumentException(existing + "," + replacement);
				}
				return existing;
			};
		}
	}

	private static class ReplaceMergingSupplier<V> implements Supplier<BinaryOperator<V>>
	{
		@Override
		public BinaryOperator<V> get()
		{
			return (existing, replacement) -> {
				if (existing.equals(replacement))
				{
					if (LOG_DUPLICATE_VALUES)
					{
						Tracing.psInfo.printf("[W] Duplicate values %s and %s, replacing first%n", existing, replacement);
					}
					//throw new IllegalArgumentException(existing + "," + replacement);
				}
				return replacement;
			};
		}
	}

	public static <K, V> Map<K, V> map(final Collection<V> things, final Function<V, K> groupingFunction)
	{
		var mergingFunction = new KeepMergingSupplier<V>().get();
		return map(things, groupingFunction, mergingFunction);
	}

	public static <K, V> Map<K, V> map(final Collection<V> things, final Function<V, K> groupingFunction, final BinaryOperator<V> mergingFunction)
	{
		return things.stream() //
				.collect(toMap(groupingFunction, //
						Function.identity(), //
						mergingFunction, //
						TreeMap::new));
	}

	// G E N E R I C   M A P   F A C T O R Y

	public static Map<String, Sense> sensesById(final Collection<Sense> senses)
	{
		BinaryOperator<Sense> mergingFunction = (existing, replacement)-> {

			Sense merged = replacement.getLex().isCased() ? (existing.getLex().isCased() ? existing : replacement) : existing;
			if (existing.equals(replacement))
			{
				if (LOG_DUPLICATE_VALUES)
				{
					Tracing.psInfo.printf("[W] Duplicate values %s and %s, merging to %s%n", existing, replacement, merged);
				}
			}
			return merged;
		};
		return map(senses, Sense::getSensekey, mergingFunction);
	}

	public static Map<String, Synset> synsetsById(final Collection<Synset> synsets)
	{
		return map(synsets, Synset::getSynsetId);
	}
}
