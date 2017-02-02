package com.shopify.graphql.support;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IntegrationTest {
    @Test
    public void testStringFieldQuery() throws Exception {
        String queryString = Generated.query(query -> query.version()).toString();
        assertEquals("{version}", queryString);
    }

    @Test
    public void testRequiredArgQuery() throws Exception {
        String queryString = Generated.query(query -> query.string("user:1:name")).toString();
        assertEquals("{string(key:\"user:1:name\")}", queryString);
    }

    @Test
    public void testOptionalArgQuery() throws Exception {
        String queryString = Generated.query(query -> query.keys(10, args -> args.after("cursor"))).toString();
        assertEquals("{keys(first:10,after:\"cursor\")}", queryString);
    }

    @Test
    public void testInterfaceQuery() throws Exception {
        String queryString = Generated.query(query ->
            query.entry("user:1", entry -> entry
                .ttl()
                .onStringEntry(strEntry -> strEntry.value())
            )
        ).toString();
        assertEquals("{entry(key:\"user:1\"){__typename,ttl,... on StringEntry{value}}}", queryString);
    }

    @Test
    public void testEnumInput() throws Exception {
        String queryString = Generated.query(query -> query
            .keys(10, args -> args.type(Generated.KeyType.INTEGER))
        ).toString();
        assertEquals("{keys(first:10,type:INTEGER)}", queryString);
    }

    @Test
    public void testMutation() throws Exception {
        String queryString = Generated.mutation(mutation -> mutation.setString("foo", "bar")).toString();
        assertEquals("mutation{set_string(key:\"foo\",value:\"bar\")}", queryString);
    }

    @Test
    public void testInputObject() throws Exception {
        String queryString = Generated.mutation(mutation -> mutation
            .setInteger(new Generated.SetIntegerInput("answer", 42).setNegate(true))
        ).toString();
        assertEquals("mutation{set_integer(input:{key:\"answer\",value:42,negate:true})}", queryString);
    }

    @Test
    public void testScalarInput() throws Exception {
        LocalDateTime ttl = LocalDateTime.of(2017, 1, 31, 10, 9, 48);
        String queryString = Generated.mutation(mutation -> mutation
            .setInteger(new Generated.SetIntegerInput("answer", 42).setTtl(ttl))
        ).toString();
        assertEquals("mutation{set_integer(input:{key:\"answer\",value:42,ttl:\"2017-01-31T10:09:48\"})}", queryString);
    }

    @Test
    public void testStringFieldResponse() throws Exception {
        String json = "{\"data\":{\"version\":\"1.2.3\"}}";
        Generated.QueryRoot data = Generated.QueryResponse.fromJson(json).getData();
        assertEquals("1.2.3", data.getVersion());
    }

    @Test
    public void testStringListResponse() throws Exception {
        String json = "{\"data\":{\"keys\":[\"one\", \"two\"]}}";
        Generated.QueryRoot data = Generated.QueryResponse.fromJson(json).getData();
        assertEquals(Arrays.asList("one", "two"), data.getKeys());
    }

    @Test
    public void testEnumFieldResponse() throws Exception {
        String json = "{\"data\":{\"type\":\"STRING\"}}";
        Generated.QueryRoot data = Generated.QueryResponse.fromJson(json).getData();
        assertEquals(Generated.KeyType.STRING, data.getType());
    }

    @Test
    public void testUnknownEnumResponse() throws Exception {
        String json = "{\"data\":{\"type\":\"FUTURE\"}}";
        Generated.QueryRoot data = Generated.QueryResponse.fromJson(json).getData();
        assertEquals(Generated.KeyType.UNKNOWN_VALUE, data.getType());
    }

    @Test
    public void testScalarFieldResponse() throws Exception {
        String json = "{\"data\":{\"ttl\":\"2017-01-31T10:09:48\"}}";
        Generated.QueryRoot data = Generated.QueryResponse.fromJson(json).getData();
        assertEquals(LocalDateTime.of(2017, 1, 31, 10, 9, 48), data.getTtl());
    }

    @Test
    public void testInterfaceResponse() throws Exception {
        String json = "{\"data\":{\"entry\":{\"__typename\":\"IntegerEntry\",\"value\":42}}}";
        Generated.Entry entry = Generated.QueryResponse.fromJson(json).getData().getEntry();
        assertEquals("IntegerEntry", entry.getGraphQlTypeName());
        assertTrue(entry instanceof Generated.IntegerEntry);
        assertEquals(42, ((Generated.IntegerEntry) entry).getValue().intValue());
    }

    @Test
    public void testInterfaceUnknownTypeResponse() throws Exception {
        String json = "{\"data\":{\"entry\":{\"__typename\":\"FutureEntry\",\"key\":\"foo\"}}}";
        Generated.Entry entry = Generated.QueryResponse.fromJson(json).getData().getEntry();
        assertEquals("FutureEntry", entry.getGraphQlTypeName());
        assertTrue(entry instanceof Generated.UnknownEntry);
        assertEquals("foo", entry.getKey());
    }

    @Test
    public void testMutationResponse() throws Exception {
        String json = "{\"data\":{\"set_string\":true}}";
        Generated.Mutation data = Generated.MutationResponse.fromJson(json).getData();
        assertEquals(true, data.getSetString().booleanValue());
    }
}