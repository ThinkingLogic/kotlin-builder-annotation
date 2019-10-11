package com.thinkinglogic.example;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

public class CollectionsDataClassJavaTest {

    @Test
    void builderShouldAllowNullValuesInCollections() {
        // given
        CollectionsDataClassBuilder builder = new CollectionsDataClassBuilder();

        List<String> nullableList = asList(null, null, "foo");
        Set<Long> nullableSet = new HashSet<>(asList(null, null, 1L));

        HashMap<String, LocalDate> nullableMap = new HashMap<>();
        nullableMap.put("Foo", null);


        // when
        CollectionsDataClass result = builder
                .listOfNullableStrings(nullableList)
                .mapOfStringToNullableDates(nullableMap)
                .setOfNullableLongs(nullableSet)
                .collectionOfDates(emptySet())
                .listOfStrings(emptyList())
                .setOfLongs(emptySet())
                .hashSet(new HashSet<>())
                .treeMap(new TreeMap<>())
                .build();

        // then
        assertThat(result.getListOfNullableStrings()).isEqualTo(nullableList);
        assertThat(result.getMapOfStringToNullableDates()).isEqualTo(nullableMap);
        assertThat(result.getSetOfNullableLongs()).isEqualTo(nullableSet);
    }
}
