package com.linkedin.java_exception_handling;

public class FibonacciInputException extends Exception{
  public FibonacciInputException(String message) {
    super(message);
  }
}
