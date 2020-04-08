package com.my.project.http.client.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class JsonUtilsTest {

    @Test
    public void testFeatures() {
        // deserialize
        String json = "{\"name\":\"\t\",\"gender\":\"\b\",\"address\":\"\001中文\",\"age\":null,}";
        Person deserialize = JsonUtils.jsonToObject(json, Person.class);
        assertEquals("\t", deserialize.getName());
        assertEquals("\b", deserialize.getGender());
        assertEquals("\001中文", deserialize.getAddress());
        assertEquals(0, deserialize.getAge());
        assertNull(deserialize.getJob());
        // serialize
        Person serialize = new Person();
        serialize.setName("杨咩咩");
        serialize.setGender("male");
        serialize.setAddress("\t\r\n\b\f\001\u0001");
        serialize.setAge(21);
        assertEquals("{\"name\":\"杨咩咩\",\"gender\":\"male\",\"address\":\"\\t\\r\\n\\b\\f\\u0001\\u0001\",\"job\":null,\"age\":21}", JsonUtils.toJsonString(serialize));
    }

    private static class Person {
        private String name;
        private String gender;
        private String address;
        private String job;
        private int age;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getGender() {
            return gender;
        }
        public void setGender(String gender) {
            this.gender = gender;
        }
        public String getAddress() {
            return address;
        }
        public void setAddress(String address) {
            this.address = address;
        }
        public String getJob() {
            return job;
        }
        public void setJob(String job) {
            this.job = job;
        }
        public int getAge() {
            return age;
        }
        public void setAge(int age) {
            this.age = age;
        }
        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", gender='" + gender + '\'' +
                    ", address='" + address + '\'' +
                    ", job='" + job + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}