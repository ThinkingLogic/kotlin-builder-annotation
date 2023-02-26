package com.thinkinglogic.example;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

public class CollectionsDataClassJavaTest {

    @Test
    void builderShouldAllowNullValuesInNullableCollections() {
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
//
//    @Test
//    void builderShouldNotAllowNullValuesInNonNullCollections() {
//        // given
//        CollectionsDataClassBuilder builder = new CollectionsDataClassBuilder()
//                .listOfNullableStrings(emptyList())
//                .mapOfStringToNullableDates(emptyMap())
//                .setOfNullableLongs(emptySet());
//
//
//        // when / then
//        assertThatCode(() -> {
//            List<LocalDate> list = new ArrayList<>();
//            list.add(null);
//            builder.collectionOfDates(list);
//        })
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("null")
//                .hasMessageContaining("collectionOfDates");
//
//        assertThatCode(() ->  {
//            List<String> list = new ArrayList<>();
//            list.add(null);
//            builder.listOfStrings(list);
//        } )
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("null")
//                .hasMessageContaining("listOfStrings");
//
//        assertThatCode(() -> {
//            Set<Long> set = new HashSet<>();
//            set.add(null);
//            builder.setOfLongs(set);
//        })
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("null")
//                .hasMessageContaining("setOfLongs");
//
//        assertThatCode(() -> builder.treeMap(new TreeMap(Map.of("foo", (LocalDate) null))))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("null")
//                .hasMessageContaining("treeMap");
//    }
}
