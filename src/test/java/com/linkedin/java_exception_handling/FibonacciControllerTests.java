package com.linkedin.java_exception_handling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(FibonacciController.class)
public class FibonacciControllerTests extends AbstractTest{
  @Override
  @BeforeEach
  public void setUp() {
    super.setUp();
  }

  @Test
  @Tag("happy")
  public void findFibonacciNumber() throws Exception {
    Map<String, String> tests = new HashMap<>();
    tests.put("3", "2");
    tests.put("0", "0");
    tests.put("-1", "-1");

    for (Map.Entry entry : tests.entrySet()) {
      String uri = String.format("/fibonacci/findNumber?n=%s", entry.getKey());
      MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE)).
          andReturn();

      int status = mvcResult.getResponse().getStatus();
      assertEquals(200, status);
      String content = mvcResult.getResponse().getContentAsString();
      assertEquals(content, entry.getValue());
    }
  }

  @Test
  @Tag("unhappy")
  public void findFibonacciNumberException() throws Exception {
    String uri = "/fibonacci/findNumber?n=a";
    MvcResult mvcResult =
        mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

    int status = mvcResult.getResponse().getStatus();
    assertEquals(400, status);
  }

  @Test
  @Tag("happy")
  public void generateFibonacciSequence() throws Exception {
    String uri = "/fibonacci/createSequence?n=3";
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

    String expected = "fibonacci.txt";
    int status = mvcResult.getResponse().getStatus();
    assertEquals(200, status);

    assertEquals(expected, mvcResult.getResponse().getContentAsString());
  }

  @Test
  @Tag("unhappy")
  public void retrieveFibonacciSequence() throws Exception {
    String uri = "/fibonacci/getSequence";
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

    int status = mvcResult.getResponse().getStatus();
    assertEquals(status, 400);
  }

  @Test
  @Tag("unhappy")
  public void retrieveFibonacciSequenceException() throws Exception {
    String uri = "/fibonacci/getSequence?filename=not_found.txt";
//    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
//
//    int status = mvcResult.getResponse().getStatus();
//    assertEquals(500, status);

    Throwable exception = assertThrows(IOException.class, () -> mvc.perform(MockMvcRequestBuilders.get(uri)));
    assertEquals("not_found.txt (No such file or directory)", exception.getMessage());
  }

  @Test
  @Tag("happy")
  public void retrieveFibonacciSequenceWithExceptionHandling() throws Exception {
    String uri = "/fibonacci/getWithExceptionHandling?filename=not_found.txt";
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

    int status = mvcResult.getResponse().getStatus();
    assertEquals(404, status);

    String body = "File not available. Please check the request and try again. not_found.txt (No such file or directory)";
    assertEquals(body, mvcResult.getResponse().getContentAsString());
  }

  @Test
  @Tag("happy")
  public void generateFibonacciSequenceWithExceptionHandling() throws Exception {
    List<ArrayList<String>> testCases = new ArrayList<>();
    testCases.add(new ArrayList<>(Arrays.asList("a", "400", "Invalid input. Please provide a valid number")));
    testCases.add(new ArrayList<>(Arrays.asList("npe", "418", "NPE")));

    String uri = "/fibonacci/createSequenceWithExceptionHandling?n=";
    String testURI;
    MvcResult mvcResult;
    int status;
    for(ArrayList<String> testCase : testCases) {
      testURI = uri + testCase.get(0);
      mvcResult = mvc.perform(MockMvcRequestBuilders.post(testURI).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

      status = mvcResult.getResponse().getStatus();
      assertEquals(testCase.get(1), String.valueOf(status));

      assertEquals(testCase.get(2), mvcResult.getResponse().getContentAsString());
    }
  }

  @Test
  @Tag("happy")
  public void findFibonacciNumberWithException() throws Exception {
    List<Integer> testCases = new ArrayList<>(Arrays.asList(9, 13));
    String uri = "/fibonacci/findNumberWithException";

    String testURI;
    MvcResult mvcResult;
    int status;
    for (Integer testCase : testCases) {
      testURI = uri + "?n=" + testCase;
      mvcResult = mvc.perform(MockMvcRequestBuilders.get(testURI).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

      status = mvcResult.getResponse().getStatus();
      assertEquals(400, status);

      assertEquals(String.format("Requested position %d is too large. Please try again.", testCase),
          mvcResult.getResponse().getContentAsString());
    }
  }

  @Test
  @Tag("happy")
  public void getRatio() throws Exception {
    Map<Integer, Double> testCases = new HashMap<>();
    testCases.put(-1, 0.5);
    testCases.put(0, -0.0);
    testCases.put(2, 1.0);
    testCases.put(3, 2.0);
    testCases.put(4, 1.5);
    testCases.put(8, 1.6153846153846154);

    String uri = "/fibonacci/findRatio";
    String testURI;
    MvcResult mvcResult;
    int status;
    for (Map.Entry<Integer, Double> entry : testCases.entrySet()) {

      testURI = uri + "?n=" + entry.getKey();
      mvcResult = mvc.perform(MockMvcRequestBuilders.get(testURI).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

      status = mvcResult.getResponse().getStatus();
      assertEquals(200, status);

      assertEquals(String.valueOf(entry.getValue()), mvcResult.getResponse().getContentAsString());
    }

    mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri + "?n=1").accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

    status = mvcResult.getResponse().getStatus();
    assertEquals(400, status);

    assertEquals("Division by Zero, produced by the fibonacci of 0", mvcResult.getResponse().getContentAsString());
  }
}
